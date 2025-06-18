package com.carshare.rentalsystem.exception;

public class InvalidPriceRangeFormatException extends RuntimeException {
    public InvalidPriceRangeFormatException(String message) {
        super(message);
    }
}
