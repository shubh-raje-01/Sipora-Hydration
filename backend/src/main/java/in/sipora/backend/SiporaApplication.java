package in.sipora.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * Sipora Hydration — Spring Boot Application Entry Point
 *
 * Modular Monolith architecture.
 * Modules (packages under in.sipora.modules.*) communicate only through
 * their public api/ interfaces. Module boundary violations are caught
 * at test time by ArchUnit (ModuleBoundaryTest).
 *
 * Module dependency graph:
 *   identity     → (no deps)
 *   catalog      → (no deps)
 *   cart         → catalog
 *   ordering     → cart, catalog
 *   payment      → ordering
 *   review       → ordering, identity
 *   ai           → catalog
 *   notification → identity, ordering (event-driven via @TransactionalEventListener)
 *
 * Infrastructure:
 *   PostgreSQL  — primary datastore (Flyway migrations V1–V6)
 *   Redis       — cache + JWT refresh tokens + AI session history
 *   Gemini API  — AI hydration advisor (Vertex AI streaming)
 *   Razorpay    — Indian payment gateway (HMAC-verified webhooks)
 *   JavaMail    — transactional email via Thymeleaf templates
 */
@Slf4j
@SpringBootApplication
public class SiporaApplication {

	private final Environment environment;

	public SiporaApplication(Environment environment) {
		this.environment = environment;
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SiporaApplication.class);
		app.setBannerMode(org.springframework.boot.Banner.Mode.LOG);
		app.run(args);
	}

	/**
	 * Fires after all beans are ready and the application is fully started.
	 * Logs a startup summary — port, active profile, Swagger URL and health endpoint
	 * so developers and ops can instantly verify what's running.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		String[] profiles = environment.getActiveProfiles();
		String   profile  = profiles.length > 0 ? profiles[0] : "default";
		String   port     = environment.getProperty("server.port", "8080");

		log.info("""

                ╔═══════════════════════════════════════════════════════╗
                ║          SIPORA HYDRATION BACKEND — READY             ║
                ╠═══════════════════════════════════════════════════════╣
                ║  Profile  :  {}
                ║  Port     :  {}
                ║  Swagger  :  http://localhost:{}/swagger-ui.html
                ║  Health   :  http://localhost:{}/actuator/health
                ╚═══════════════════════════════════════════════════════╝
                """,
				profile, port, port, port);
	}
}