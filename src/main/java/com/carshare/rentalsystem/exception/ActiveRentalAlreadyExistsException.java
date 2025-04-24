package com.carshare.rentalsystem.exception;

public class ActiveRentalAlreadyExistsException extends RuntimeException {
    public ActiveRentalAlreadyExistsException(String message) {
        super(message);
    }
}
