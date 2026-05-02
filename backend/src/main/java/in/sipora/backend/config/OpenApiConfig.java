package in.sipora.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * App-level OpenAPI / Swagger configuration.
 *
 * Accessible at:
 *   /swagger-ui.html — interactive UI
 *   /v3/api-docs — raw JSON spec
 *
 * The JWT bearer scheme is registered globally so the "Authorize" button
 * in Swagger UI applies the token to every endpoint automatically.
 *
 * Both URLs are whitelisted in SecurityConfig.PUBLIC_GET.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Value("${sipora.api.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${sipora.api.version:1.0.0}")
    private String apiVersion;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url(baseUrl).description("Current environment"),
                        new Server().url("http://localhost:8080").description("Local dev")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, jwtSecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Sipora Hydration API")
                .description("""
                        REST API for the Sipora premium hydration brand.
                        
                        Authentication: pass the JWT access token as a Bearer token
                        in the Authorization header: `Authorization: Bearer <token>`
                        
                        Obtain a token via POST /api/v1/auth/login.
                        """)
                .version(apiVersion)
                .contact(new Contact()
                        .name("Sipora Engineering")
                        .email("dev@sipora.in")
                        .url("https://sipora.in"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://sipora.in"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(BEARER_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT access token (without the 'Bearer ' prefix)");
    }
}