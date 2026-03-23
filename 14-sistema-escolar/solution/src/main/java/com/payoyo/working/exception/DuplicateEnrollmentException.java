package com.payoyo.working.exception;

/** 409 Conflict — El estudiante ya está matriculado en ese curso */
public class DuplicateEnrollmentException extends RuntimeException {

    public DuplicateEnrollmentException(String message) {
        super(message);
    }
}