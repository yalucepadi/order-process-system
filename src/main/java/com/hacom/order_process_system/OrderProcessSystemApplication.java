package com.hacom.order_process_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class OrderProcessSystemApplication {
	private static final Logger logger = LoggerFactory.getLogger(OrderProcessSystemApplication.class);

	public static void main(String[] args) throws IOException {
		logger.info("Starting Order Processing Application");
		logger.trace("Esto es un log TRACE para pruebas");
		Files.createDirectories(Paths.get("logs"));


		SpringApplication.run(OrderProcessSystemApplication.class, args);
		logger.info("Order Processing Application started successfully");

	}

}
