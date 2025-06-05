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
                        A feature-rich Car Sharing System built with Spring Boot, offering
                        a modern platform for managing vehicles, rentals, users, and payments.
                        It features a RESTful API, JWT authentication, Stripe payments,
                        and Telegram notifications. The system showcases advanced backend
                        practices like Liquibase migrations, scheduled tasks, and integrations
                        with external services.
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
