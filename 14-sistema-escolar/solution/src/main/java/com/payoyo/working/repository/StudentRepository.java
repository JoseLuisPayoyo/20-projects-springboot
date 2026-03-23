package com.payoyo.working.repository;

import com.payoyo.working.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Repository es opcional en interfaces que extienden JpaRepository
 * (Spring lo detecta automáticamente), pero mejora la legibilidad
 * y la traducción de excepciones de persistencia.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /** Verifica si ya existe un estudiante con ese email (para evitar duplicados) */
    boolean existsByEmail(String email);
}