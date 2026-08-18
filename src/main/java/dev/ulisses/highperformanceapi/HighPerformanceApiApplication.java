package dev.ulisses.highperformanceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HighPerformanceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HighPerformanceApiApplication.class, args);
	}

}
