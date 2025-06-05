package com.carshare.rentalsystem.exception;

public class RentalAccessDeniedException extends RuntimeException {
    public RentalAccessDeniedException(String message) {
        super(message);
    }
}
