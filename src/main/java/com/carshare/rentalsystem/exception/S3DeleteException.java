package com.carshare.rentalsystem.exception;

public class S3DeleteException extends RuntimeException {
    public S3DeleteException(String message) {
        super(message);
    }
}
