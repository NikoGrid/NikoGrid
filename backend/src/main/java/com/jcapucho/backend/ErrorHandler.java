package com.jcapucho.backend;

import com.jcapucho.backend.exceptions.ResourceNotFound;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ErrorResponse handleResponseStatusException(ResponseStatusException exc) {
        final String reason = exc.getReason();
        return ErrorResponse.builder(exc, exc.getStatusCode(), reason != null ? reason : "")
                .build();
    }

    @ExceptionHandler
    public ErrorResponse handleResourceNotFound(Throwable exc) {
        return ErrorResponse.builder(exc, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
                .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ErrorResponse.builder(ex, HttpStatus.METHOD_NOT_ALLOWED, "Method not supported")
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, "Bad Request")
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleInvalidArgumentException(MethodArgumentNotValidException ex) {
        return ErrorResponse.builder(ex, ex.getBody())
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorResponse handleMissingArgumentException(MissingServletRequestParameterException ex) {
        return ErrorResponse.builder(ex, ex.getBody())
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, String.format("'%s' is of invalid type", ex.getValue()))
                .build();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ErrorResponse handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        return ErrorResponse.builder(ex, ex.getBody())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
                .build();
    }

    // Thrown by spring security

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
        return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ErrorResponse handleAuthorizationException(AuthorizationDeniedException ex) {
        return ErrorResponse.builder(ex, HttpStatus.FORBIDDEN, ex.getMessage())
                .build();
    }

    // Thrown by us

    @ExceptionHandler(ResourceNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFound exc) {
        return ErrorResponse.builder(exc, HttpStatus.NOT_FOUND, "The specified resource was not found")
                .build();
    }
}
