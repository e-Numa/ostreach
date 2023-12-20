package com.ostreach.exceptions;

public class TransactionErrorException extends RuntimeException {
    public TransactionErrorException(String message) {
        super(message);
    }
}
