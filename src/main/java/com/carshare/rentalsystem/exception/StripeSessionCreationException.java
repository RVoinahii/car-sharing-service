package com.carshare.rentalsystem.exception;

public class StripeSessionCreationException extends RuntimeException {
    public StripeSessionCreationException(String message, Exception cause) {
        super(message, cause);
    }
}
