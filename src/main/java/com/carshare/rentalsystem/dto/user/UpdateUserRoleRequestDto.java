package com.carshare.rentalsystem.dto.user;

import com.carshare.rentalsystem.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequestDto(@NotNull Role.RoleName role) {
}
