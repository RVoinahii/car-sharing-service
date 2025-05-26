package com.carshare.rentalsystem.exception;

public class ActiveSessionAlreadyExistsException extends RuntimeException {
    public ActiveSessionAlreadyExistsException(String message) {
        super(message);
    }
}
