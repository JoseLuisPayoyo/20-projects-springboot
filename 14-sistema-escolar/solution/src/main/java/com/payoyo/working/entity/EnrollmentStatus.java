package com.payoyo.working.entity;

/**
 * Estados posibles de una matrícula.
 * ACTIVE: estudiante cursando activamente
 * COMPLETED: curso finalizado con calificación asignada
 * DROPPED: estudiante ha abandonado el curso
 */
public enum EnrollmentStatus {
    ACTIVE,
    COMPLETED,
    DROPPED
}