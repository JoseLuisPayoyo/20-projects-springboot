package com.payoyo.working.repository;

import com.payoyo.working.entity.Enrollment;
import com.payoyo.working.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository más complejo del proyecto.
 * Combina queries derivadas (Spring genera el SQL) con JPQL custom.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /** Matrículas de un estudiante */
    List<Enrollment> findByStudentId(Long studentId);

    /** Matrículas de un curso */
    List<Enrollment> findByCourseId(Long courseId);

    /** Verificar si ya existe matrícula (prevenir duplicados) */
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * Contar matrículas activas de un curso.
     * Se usa para verificar si hay plazas disponibles
     * comparando con course.maxStudents.
     */
    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    /** Verificar si un estudiante tiene matrículas con cierto estado (para eliminación segura) */
    boolean existsByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /** Verificar si un curso tiene matrículas con cierto estado (para eliminación segura) */
    boolean existsByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    /**
     * Nota media de un curso — JPQL custom.
     * AVG ignora NULLs automáticamente en SQL, pero el filtro
     * explícito "grade IS NOT NULL" hace la intención más clara.
     * Retorna null si no hay matrículas calificadas.
     */
    @Query("SELECT AVG(e.grade) FROM Enrollment e WHERE e.course.id = :courseId AND e.grade IS NOT NULL")
    Double findAverageGradeByCourseId(@Param("courseId") Long courseId);
}