package com.ostreach.exceptions;

public class EmptyOrderListException extends RuntimeException {
    public EmptyOrderListException(String message) {
        super(message);
    }
}
