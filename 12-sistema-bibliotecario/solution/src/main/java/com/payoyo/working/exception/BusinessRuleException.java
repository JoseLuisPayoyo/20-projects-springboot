package com.payoyo.working.exception;

// Se lanza al violar reglas de negocio: libro no disponible, límite de préstamos,
// ISBN duplicado, socio inactivo, etc. → Mapeada a 409 Conflict
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}