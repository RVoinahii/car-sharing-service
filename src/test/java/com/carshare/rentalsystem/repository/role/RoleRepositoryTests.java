package com.carshare.rentalsystem.repository.role;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carshare.rentalsystem.model.Role;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTests {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("""
            findByRole():
             Should return role 'MANAGER' when a valid role name is provided
            """)
    void findByRole_Admin_ShouldReturnRole() {
        //Given
        Role.RoleName admin = Role.RoleName.MANAGER;
        Role expectedRole = new Role();
        expectedRole.setRole(admin);

        //When
        Optional<Role> actualRole = roleRepository.findByRole(admin);

        //Given
        assertTrue(actualRole.isPresent(), "Role 'MANAGER' should be present");
        assertTrue(EqualsBuilder.reflectionEquals(
                actualRole.get(), expectedRole, "id"));
    }

    @Test
    @DisplayName("""
            findByRole():
             Should return role 'CUSTOMER' when a valid role name is provided
            """)
    void findByRole_User_ShouldReturnRole() {
        //Given
        Role.RoleName user = Role.RoleName.CUSTOMER;
        Role expectedRole = new Role();
        expectedRole.setRole(user);

        //When
        Optional<Role> actualRole = roleRepository.findByRole(user);

        //Given
        assertTrue(actualRole.isPresent(), "Role 'CUSTOMER' should be present");
        assertTrue(EqualsBuilder.reflectionEquals(
                actualRole.get(), expectedRole, "id"));
    }
}
