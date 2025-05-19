package com.carshare.rentalsystem.dto.user.response.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserPreviewResponseDto {
    private Long id;
    private String email;
    private String fullName;
}
