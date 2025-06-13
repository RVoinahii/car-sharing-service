package com.carshare.rentalsystem.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomGlobalExceptionHandler {
    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Map<String, Object>> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException exception) {
        return buildErrorResponse(exception, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException exception) {
        return buildErrorResponse(exception, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDeniedException(
            AuthorizationDeniedException exception) {
        return buildErrorResponse(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedExceptions(
            AccessDeniedException exception) {
        return buildErrorResponse(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ActiveSessionAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleActiveRentalAlreadyExistsExceptions(
            ActiveSessionAlreadyExistsException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CarNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleCarNotAvailableExceptions(
            CarNotAvailableException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CipherInitializationException.class)
    public ResponseEntity<Map<String, Object>> handleCipherInitializationExceptions(
            CipherInitializationException exception) {
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DecryptionException.class)
    public ResponseEntity<Map<String, Object>> handleDecryptionExceptions(
            DecryptionException exception) {
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateReviewExceptions(
            DuplicateReviewException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExistsExceptions(
            EmailAlreadyExistsException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EncryptionException.class)
    public ResponseEntity<Map<String, Object>> handleEncryptionExceptions(
            EncryptionException exception) {
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundExceptions(
            EntityNotFoundException exception) {
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidMediaFileException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidMediaFileExceptions(
            InvalidMediaFileException exception) {
        return buildErrorResponse(exception, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(InvalidPaymentTypeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPaymentTypeExceptions(
            InvalidPaymentTypeException exception) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxActiveRentalsExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxActiveRentalsExceededExceptions(
            MaxActiveRentalsExceededException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentNotExpiredException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotExpiredExceptions(
            PaymentNotExpiredException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, Object>> handleRegistrationExceptions(
            RegistrationException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RentalAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleRentalAccessDeniedExceptions(
            RentalAccessDeniedException exception) {
        return buildErrorResponse(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RentalAlreadyReturnedException.class)
    public ResponseEntity<Map<String, Object>> handleRentalAlreadyReturnedExceptions(
            RentalAlreadyReturnedException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RentalNotFinishedException.class)
    public ResponseEntity<Map<String, Object>> handleRentalNotFinishedExceptions(
            RentalNotFinishedException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ReviewAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleReviewAccessDeniedExceptions(
            ReviewAccessDeniedException exception) {
        return buildErrorResponse(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(S3DeleteException.class)
    public ResponseEntity<Map<String, Object>> handleS3DeleteExceptions(
            S3DeleteException exception) {
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(S3UploadException.class)
    public ResponseEntity<Map<String, Object>> handleS3UploadExceptions(
            S3UploadException exception) {
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SpecificationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSpecificationNotFoundExceptions(
            SpecificationNotFoundException exception) {
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StripeSessionCreationException.class)
    public ResponseEntity<Map<String, Object>> handleStripeSessionCreationExceptions(
            StripeSessionCreationException exception) {
        return buildErrorResponse(exception, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(TooLargeMediaFileException.class)
    public ResponseEntity<Map<String, Object>> handleTooLargeMediaFileExceptions(
            TooLargeMediaFileException exception) {
        return buildErrorResponse(exception, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(TooLateToCancelRentalException.class)
    public ResponseEntity<Map<String, Object>> handleTooLateToCancelRentalExceptions(
            TooLateToCancelRentalException exception) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(
            Exception exception) {
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            Exception exception, HttpStatus httpStatus) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(STATUS, httpStatus.value());
        body.put(ERROR, httpStatus.getReasonPhrase());

        if (exception instanceof MethodArgumentNotValidException validationException) {
            List<String> errors = validationException.getBindingResult().getAllErrors().stream()
                    .map(this::getErrorMessage)
                    .collect(Collectors.toList());
            body.put(MESSAGE, errors);
        }

        body.put(MESSAGE, exception.getMessage());
        return ResponseEntity.status(httpStatus).body(body);
    }

    private String getErrorMessage(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            String field = fieldError.getField();
            String message = error.getDefaultMessage();
            return String.format("Field '%s' %s", field, message);
        }
        return error.getDefaultMessage();
    }

}
