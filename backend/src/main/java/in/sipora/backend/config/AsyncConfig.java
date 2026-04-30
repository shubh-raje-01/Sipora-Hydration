package in.sipora.backend.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * App-level async configuration.
 *
 * @EnableAsync activates Spring's @Async proxying. Methods annotated with
 * @Async("taskExecutor") will run on the pool defined here instead of the
 * HTTP request thread.
 *
 * Primary uses in Sipora:
 *  - notification/EmailService  — fire-and-forget email dispatch
 *  - ai/HydrationAdvisor        — non-blocking Gemini API calls
 *  - catalog/ProductService     — background stock recalculation
 *
 * Two pools are defined:
 *  - "taskExecutor"   — general purpose (bounded, queued)
 *  - "emailExecutor"  — dedicated for email so a slow SMTP server
 *                       cannot starve general async tasks
 *
 * Thread pool sizing guideline (adjust based on profiling):
 *  core = num CPUs
 *  max  = num CPUs * 2   (I/O-bound tasks spend time waiting, so more threads help)
 *  queue = 100            (back-pressure — throws RejectedExecutionException if full)
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${sipora.async.core-pool-size:4}")
    private int corePoolSize;

    @Value("${sipora.async.max-pool-size:8}")
    private int maxPoolSize;

    @Value("${sipora.async.queue-capacity:100}")
    private int queueCapacity;

    // General-purpose async pool (default for @Async with no qualifier)

    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("sipora-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    // Dedicated email pool — use @Async("emailExecutor") ==>

    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("sipora-email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    // Uncaught exception handler — @Async methods swallow exceptions
    // by default; this ensures they are at least logged

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) ->
                log.error(
                        "Uncaught exception in async method [{}.{}] with params {}",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(),
                        Arrays.toString(params),
                        throwable
                );
    }
}