package dev.ulisses.highperformanceapi.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.url}") String appUrl) {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url(appUrl).description("Local Environment")
                ))
                .info(new Info()
                .title("High Performance API")
                .version("1.0")
                .description("""
                        Order Processing Platform API.
                        Built with Spring Boot 4 following REST best practices.
                        """)
                .license(new License().name("MIT")));
    }

}
