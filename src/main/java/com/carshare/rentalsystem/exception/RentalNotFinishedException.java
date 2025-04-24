package com.carshare.rentalsystem.exception;

public class RentalNotFinishedException extends RuntimeException {
    public RentalNotFinishedException(String message) {
        super(message);
    }
}
