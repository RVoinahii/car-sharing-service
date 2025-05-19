package com.carshare.rentalsystem.dto.user.request.dto;

import com.carshare.rentalsystem.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequestDto(@NotNull Role.RoleName role) {
}
