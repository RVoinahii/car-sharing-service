package com.carshare.rentalsystem;

import com.carshare.rentalsystem.model.Role;
import com.carshare.rentalsystem.repository.role.RoleRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Car Sharing Service",
                version = "0.1.0",
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
    private final RoleRepository roleRepository;

    public static void main(String[] args) {
        SpringApplication.run(CarShareApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Role roleManager = new Role();
            roleManager.setRole(Role.RoleName.MANAGER);

            Role roleCustomer = new Role();
            roleCustomer.setRole(Role.RoleName.CUSTOMER);

            roleRepository.save(roleManager);
            roleRepository.save(roleCustomer);
        };
    }
}
