package com.ostreach.exceptions;

public class NewAndOldPasswordException extends RuntimeException {
    public NewAndOldPasswordException(String message) {
        super(message);
    }
}
