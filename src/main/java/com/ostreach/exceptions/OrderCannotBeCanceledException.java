package com.ostreach.exceptions;

public class OrderCannotBeCanceledException extends RuntimeException {
    public OrderCannotBeCanceledException(String message) {
        super(message);
    }
}
