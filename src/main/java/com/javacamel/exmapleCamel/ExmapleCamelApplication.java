package com.javacamel.exmapleCamel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan( basePackages = "com.javacamel.exmapleCamel.beans")
public class ExmapleCamelApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExmapleCamelApplication.class, args);
	}

}
