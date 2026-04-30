package in.sipora.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * App-level Redis configuration.
 *
 * Cache names + TTLs are defined here so they are visible in one place.
 * Modules annotate their service methods with @Cacheable("products") etc.
 * The TTL for that cache is controlled here, not scattered in annotations.
 *
 * Cache name constants are package-private so modules reference them
 * via CacheNames.PRODUCTS rather than magic strings.
 *
 * Serialization: JSON via Jackson (not Java serialization) so cache entries
 * are human-readable in Redis CLI and survive class reloads in dev.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    // Cache name constants — reference these from @Cacheable annotations ==>

    public static final class CacheNames {
        public static final String PRODUCTS = "products";
        public static final String PRODUCT_SLUGS = "product-slugs";
        public static final String CATEGORIES = "categories";
        public static final String USER_PROFILE = "user-profile";
        public static final String CART = "cart";
        public static final String PRODUCT_REVIEWS = "product-reviews";
        public static final String AI_PRODUCT_CTX = "ai-product-context";

        private CacheNames() {
        }
    }

    // Connection ==>

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isBlank()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }

    // General-purpose RedisTemplate (String key, JSON value)
    // Used by modules that need raw Redis ops (e.g. refresh token store)

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJacksonJsonRedisSerializer(redisObjectMapper()));
        template.setHashValueSerializer(new GenericJacksonJsonRedisSerializer(redisObjectMapper()));
        template.afterPropertiesSet();
        return template;
    }

    // Spring Cache abstraction — @Cacheable / @CacheEvict ==>

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = defaultCacheConfig();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CacheNames.PRODUCTS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put(CacheNames.PRODUCT_SLUGS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put(CacheNames.CATEGORIES, defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigs.put(CacheNames.USER_PROFILE, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put(CacheNames.CART, defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigs.put(CacheNames.PRODUCT_REVIEWS, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put(CacheNames.AI_PRODUCT_CTX, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    // Helpers ==>

    private RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJacksonJsonRedisSerializer(redisObjectMapper())));
    }

    /**
     * Dedicated ObjectMapper for Redis serialization.
     *
     * activateDefaultTyping is required so Jackson can deserialize polymorphic
     * types from the cache back to their concrete class. This is safe here
     * because we control what goes into the cache.
     *
     * JavaTimeModule ensures Instant, LocalDate etc. serialize to ISO strings,
     * not opaque numeric arrays.
     */

    private ObjectMapper redisObjectMapper() {
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("in.sipora.backend")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .allowIfSubType("java.math")
                .allowIfSubTypeIsArray()
                .build();

        return JsonMapper.builder()
                .findAndAddModules()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(
                        typeValidator,
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY)
                .build();
    }
    

}
