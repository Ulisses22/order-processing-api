package dev.ulisses.highperformanceapi.infrastructure.config;

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

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${app.url}") String appUrl
    ) {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url(appUrl)
                                .description("Local Environment")
                ))
                .info(new Info()
                        .title("High Performance API")
                        .version("1.0.0")
                        .description("""
                                Order Processing Platform API.

                                REST API for customer, product, inventory,
                                order, payment and shipment processing.

                                Built with Spring Boot 4 following REST
                                and production-oriented API practices.
                                """)
                        .contact(new Contact()
                                .name("High Performance API"))
                        .license(new License()
                                .name("MIT")))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                );
    }
}
