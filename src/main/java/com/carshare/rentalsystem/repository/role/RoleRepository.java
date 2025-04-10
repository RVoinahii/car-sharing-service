package com.carshare.rentalsystem.repository.role;

import com.carshare.rentalsystem.model.Role;
import com.carshare.rentalsystem.model.Role.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(RoleName role);
}
