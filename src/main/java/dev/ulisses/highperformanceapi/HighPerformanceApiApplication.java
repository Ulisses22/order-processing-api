package dev.ulisses.highperformanceapi;

import dev.ulisses.highperformanceapi.infrastructure.seed.SeedProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@EnableConfigurationProperties(SeedProperties.class)
public class HighPerformanceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HighPerformanceApiApplication.class, args);
	}

}
