package com.payoyo.working.exception;

// RuntimeException → unchecked, no obliga a try-catch
// Se lanza cuando no se encuentra un recurso por ID, ISBN, email, etc.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}