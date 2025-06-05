package com.carshare.rentalsystem.exception;

public class TooLateToCancelRentalException extends RuntimeException {
    public TooLateToCancelRentalException(String message) {
        super(message);
    }
}
