package com.carshare.rentalsystem;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Car Sharing Service",
                version = "0.7.5",
                description = """
      A modern, full-featured Car Sharing platform built with Spring Boot. The system provides
      robust capabilities for managing vehicles, rentals, users, reviews and payments through a
      RESTful API.
      
      Key features include:
      - JWT-based authentication and role management
      - Integration with Stripe for secure payment processing
      - A Telegram bot that supports both real-time notifications and interactive command handling
      - Liquibase-powered database migrations and scheduled background tasks
      - Clean, modular architecture following advanced backend best practices
      
      Designed for scalability, maintainability, and seamless integration with external services.
            """,
                contact = @Contact(
                        name = "Roman Voynahiy"
                )
        )
)
@SecurityScheme(
        name = "BearerAuthentication",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT"
)
public class CarShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarShareApplication.class, args);
    }
}
