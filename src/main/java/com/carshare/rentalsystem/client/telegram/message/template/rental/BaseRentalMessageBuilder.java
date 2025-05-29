package com.carshare.rentalsystem.client.telegram.message.template.rental;

import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateBuilder;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserPreviewResponseDto;
import com.carshare.rentalsystem.model.Rental;

public abstract class BaseRentalMessageBuilder<T> implements MessageTemplateBuilder<T> {
    protected String formatUserInfo(UserPreviewResponseDto user) {
        return String.format("""
                               ID: %s
                               Name: %s
                               Email: %s
                            """,
                user.getId(),
                user.getFullName(),
                user.getEmail());
    }

    protected String formatCarInfo(CarPreviewResponseDto car) {
        return String.format("""
                               Brand: %s
                               Model: %s
                               Daily fee: %.2f USD
                            """,
                car.getBrand(),
                car.getModel(),
                car.getDailyFee());
    }

    protected String formatActiveRentalInfo(RentalResponseDto rental) {
        return String.format("""
                               Rental ID: %s
                               Rental Start: %s
                               Rental End: %s
                               Rental Status: %s
                            """,
                rental.getId(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                rental.getStatus());
    }

    protected String formatStatusEmoji(Rental.RentalStatus status) {
        return switch (status) {
            case RESERVED -> "📅";
            case ACTIVE -> "▶";
            case WAITING_FOR_PAYMENT -> "💳";
            case COMPLETED -> "✅";
            case CANCELLED -> "❌";
        };
    }
}
