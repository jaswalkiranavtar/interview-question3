package com.example.demo.exception;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.demo.model.Error;

/**
 * A {@link ControllerAdvice} which globally handles {@link NotifyingException}
 * instances thrown from any controller and returns the correct response body
 * as the serialized form of json  and the correct HTTP status code.
 */
@ControllerAdvice
public class ExceptionAdvice {

    Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * Handles {@link MethodArgumentNotValidException} instances thrown by spring framework.
     *
     * @param exception The {@link MethodArgumentNotValidException} to handle
     *
     * @return A {@link ResponseEntity} with the validation error and field and {@link HttpStatus} code 400
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Error>> handle(final MethodArgumentNotValidException exception) {
        logger.error(exception.getMessage(), exception);
        List<Error> errors = exception.getBindingResult().getAllErrors().stream()
            .map(FieldError.class::cast)
            .map(this::convertFieldErrorToErrorObject)
            .collect(Collectors.toList());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link MethodArgumentTypeMismatchException} instances thrown by spring framework.
     *
     * @param exception The {@link MethodArgumentTypeMismatchException} to handle
     *
     * @return A {@link ResponseEntity} with the validation error and field and {@link HttpStatus} code 400
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<List<Error>> handle(final MethodArgumentTypeMismatchException exception) {
        logger.error(exception.getMessage(), exception);
        Error error = Error.builder()
                .fieldName(exception.getName()).objectName("").rejectedValue(exception.getValue().toString()).message(exception.getMessage())
                .build();
        return new ResponseEntity<>(Arrays.asList(error), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link HttpRequestMethodNotSupportedException} instances thrown by spring framework.
     *
     * @param exception The {@link HttpRequestMethodNotSupportedException} to handle
     *
     * @return A {@link ResponseEntity} with the validation error and field and {@link HttpStatus} code 405
     */
    @ResponseBody
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<List<Error>> handle(final HttpRequestMethodNotSupportedException exception) {
        logger.error(exception.getMessage(), exception);
        Error error = Error.builder()
                .fieldName("Http request method").objectName("").message(exception.getMessage())
                .expectedValue(Arrays.stream(exception.getSupportedMethods()).reduce("", (a, b) -> a + b + " "))
                .rejectedValue(exception.getMethod())
                .build();
        return new ResponseEntity<>(Arrays.asList(error), HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles {@link HttpMediaTypeNotAcceptableException} instances thrown by spring framework.
     *
     * @param exception The {@link HttpMediaTypeNotAcceptableException} to handle
     *
     * @return A {@link ResponseEntity} with the validation error and field and {@link HttpStatus} code 406
     */
    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<List<Error>> handle(final HttpMediaTypeNotAcceptableException exception) {
        logger.error(exception.getMessage(), exception);
        String mediaTypes = exception.getSupportedMediaTypes().stream().map(MediaType::toString).reduce("", (a, b) -> a + b + " ");
        Error error = Error.builder()
                .message(exception.getMessage())
                .objectName("Accept Header")
                .expectedValue(mediaTypes)
                .build();
        return new ResponseEntity<>(Arrays.asList(error), HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Handles {@link HttpMediaTypeNotSupportedException} instances thrown by spring framework.
     *
     * @param exception The {@link HttpMediaTypeNotSupportedException} to handle
     *
     * @return A {@link ResponseEntity} with the validation error and field and {@link HttpStatus} code 415
     */
    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<List<Error>> handle(final HttpMediaTypeNotSupportedException exception) {
        logger.error(exception.getMessage(), exception);
        String mediaTypes = exception.getSupportedMediaTypes().stream().map(MediaType::toString).reduce("", (a, b) -> a + b + " ");
        Error error = Error.builder()
                .message(exception.getMessage())
                .objectName("Content-Type Header")
                .expectedValue(mediaTypes)
                .rejectedValue(exception.getContentType() == null ? null : exception.getContentType().toString())
                .build();
        return new ResponseEntity<>(Arrays.asList(error), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handles {@link HttpMessageNotReadableException} instances thrown by spring framework.
     *
     * @param exception The {@link HttpMessageNotReadableException} to handle
     *
     * @return A {@link ResponseEntity} with the validation error and field and {@link HttpStatus} code 400
     */
    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<List<Error>> handle(final HttpMessageNotReadableException exception) {
        logger.error(exception.getMessage(), exception);
        Error error = Error.builder()
                .objectName("Request Body")
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(Arrays.asList(error), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all the exceptions NOT handled by one of the other exception handlers in this class.
     * This is a default handler, if the exception thrown doesn't match one of the other handled exceptions.
     * 
     * @param exception The {@link Exception} to handle
     *
     * @return A {@link ResponseEntity} with the correct body and {@link HttpStatus} code 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<Error>> handle(final Exception exception) {
        logger.error(exception.getMessage(), exception);
        Error error = Error.builder().message(exception.getMessage()).build();
        return new ResponseEntity<>(Arrays.asList(error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Error convertFieldErrorToErrorObject(FieldError fieldError) {
        return Error.builder()
                .fieldName(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .objectName(fieldError.getObjectName())
                .rejectedValue(fieldError.getRejectedValue() != null ? fieldError.getRejectedValue().toString() : null).build();
    }

}
