package com.nikogrid.backend.exceptions;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("Email already taken");
    }
}
