package com.payoyo.working.exception;

/** 409 Conflict — El curso ha alcanzado el máximo de plazas */
public class CourseFullException extends RuntimeException {

    public CourseFullException(String message) {
        super(message);
    }
}