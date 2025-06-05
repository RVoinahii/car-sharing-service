package com.carshare.rentalsystem.exception;

public class MaxActiveRentalsExceededException extends RuntimeException {
    public MaxActiveRentalsExceededException(String message) {
        super(message);
    }
}
