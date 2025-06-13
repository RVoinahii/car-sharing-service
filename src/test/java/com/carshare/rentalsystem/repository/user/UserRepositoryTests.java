package com.carshare.rentalsystem.repository.user;

import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createDefaultUserSample;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carshare.rentalsystem.model.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTests {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("""
            existByEmail():
             Should return true when the searched email is exist
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void existByEmail_ExistedEmail_ReturnsTrue() {
        //Given
        String existedEmail = "mail.example@gmail.com";

        //When & Then
        assertTrue(userRepository.existsByEmail(existedEmail), "Email should be exist");
    }

    @Test
    @DisplayName("""
            existByEmail():
             Should return false when the searched email is not exist
            """)
    void existByEmail_NonExistedEmail_ReturnsFalse() {
        //Given
        String nonExistedEmail = "nonexisted@gmail.com";

        //When & Then
        assertFalse(userRepository.existsByEmail(nonExistedEmail), "Email should not exist");
    }

    @Test
    @DisplayName("""
            findByEmail():
             Should return user with the provided email when the searched email exist
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByEmail_ExistedEmail_ReturnsUser() {
        //Given
        String existedEmail = "mail.example@gmail.com";
        User expectedUser = createDefaultUserSample();
        expectedUser.setId(3L);

        //When
        Optional<User> actualUser = userRepository.findByEmail(existedEmail);

        //Then
        assertTrue(actualUser.isPresent(), "User should be present");
        assertTrue(EqualsBuilder.reflectionEquals(
                actualUser.get(), expectedUser, "id", "role"));
    }

    @Test
    @DisplayName("""
            findByEmail():
             Should return empty optional when the searched email not exist
            """)
    void findByEmail_NonExistedEmail_ReturnsOptionalEmpty() {
        //Given
        String nonExistedEmail = "nonexisted@gmail.com";

        //When
        Optional<User> actualUser = userRepository.findByEmail(nonExistedEmail);

        //Then
        assertTrue(actualUser.isEmpty(), "User should be empty");
    }
}
