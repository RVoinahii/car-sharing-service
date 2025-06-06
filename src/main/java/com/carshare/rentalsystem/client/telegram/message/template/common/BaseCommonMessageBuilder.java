package com.carshare.rentalsystem.client.telegram.message.template.common;

import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateBuilder;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;

public abstract class BaseCommonMessageBuilder<T> implements MessageTemplateBuilder<T> {
    protected String formatUserInfo(UserResponseDto user) {
        return String.format("""
                               ID: %s
                               Name: %s %s
                               Email: %s
                               Role: %s
                            """,
                user.getId(),
                user.getFirstName(), user.getLastName(),
                user.getEmail(),
                user.getRole());
    }
}
