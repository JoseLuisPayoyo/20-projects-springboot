package com.payoyo.working.exception;

/** 400 Bad Request — Violación de regla de negocio genérica */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}