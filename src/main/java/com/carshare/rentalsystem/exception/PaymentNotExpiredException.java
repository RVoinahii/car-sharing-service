package com.carshare.rentalsystem.exception;

public class PaymentNotExpiredException extends RuntimeException {
    public PaymentNotExpiredException(String message) {
        super(message);
    }
}
