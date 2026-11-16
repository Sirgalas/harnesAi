package ru.sergalas.harnes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HarnesApplication {

	public static void main(String[] args) {
		SpringApplication.run(HarnesApplication.class, args);
	}

}
