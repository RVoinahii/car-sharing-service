package com.carshare.rentalsystem.exception;

public class TooLargeMediaFileException extends RuntimeException {
    public TooLargeMediaFileException(String message) {
        super(message);
    }
}
