package com.payoyo.working.service;

import com.payoyo.working.entity.Course;
import com.payoyo.working.entity.Enrollment;
import com.payoyo.working.entity.EnrollmentStatus;
import com.payoyo.working.entity.Student;
import com.payoyo.working.exception.BusinessRuleException;
import com.payoyo.working.exception.CourseFullException;
import com.payoyo.working.exception.DuplicateEnrollmentException;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service más complejo del proyecto.
 * Orquesta las validaciones de negocio antes de cada operación
 * sobre matrículas: duplicados, plazas, estado del curso, notas.
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;
    private final CourseService courseService;

    /**
     * Inyectamos Services (no Repositories directamente) para Student y Course.
     * Así reutilizamos la lógica de findById con su manejo de 404,
     * evitando duplicar código.
     */
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentService studentService,
                             CourseService courseService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Enrollment findById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Matrícula no encontrada con id: " + id));
    }

    /**
     * Flujo de creación de matrícula con 4 validaciones:
     * 1. Student y Course existen (delegado a sus Services → 404 si no)
     * 2. Curso activo
     * 3. No duplicada
     * 4. Hay plazas disponibles
     */
    @Transactional
    public Enrollment create(Long studentId, Long courseId) {
        Student student = studentService.findById(studentId);
        Course course = courseService.findById(courseId);

        if (!course.getActive()) {
            throw new BusinessRuleException(
                    "No se puede matricular en un curso inactivo: " + course.getName());
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateEnrollmentException(
                    "El estudiante ya está matriculado en el curso: " + course.getName());
        }

        /**
         * Contamos solo matrículas ACTIVE para las plazas.
         * Las DROPPED o COMPLETED no ocupan plaza.
         */
        long activeEnrollments = enrollmentRepository
                .countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);

        if (activeEnrollments >= course.getMaxStudents()) {
            throw new CourseFullException(
                    "El curso '" + course.getName() + "' ha alcanzado el máximo de "
                    + course.getMaxStudents() + " estudiantes");
        }

        Enrollment enrollment = new Enrollment(student, course);
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Actualizar nota con validación de rango.
     * Bean Validation (@DecimalMin/@DecimalMax) protege en la entidad,
     * pero validamos aquí también para dar mensajes de error claros.
     */
    @Transactional
    public Enrollment updateGrade(Long id, Double grade) {
        Enrollment enrollment = findById(id);

        if (grade < 0.0 || grade > 10.0) {
            throw new BusinessRuleException(
                    "La calificación debe estar entre 0.0 y 10.0");
        }

        enrollment.setGrade(grade);
        return enrollment;
    }

    /**
     * Cambiar estado con validación de coherencia:
     * - COMPLETED requiere nota asignada previamente
     * - ACTIVE y DROPPED se pueden asignar libremente
     */
    @Transactional
    public Enrollment updateStatus(Long id, EnrollmentStatus status) {
        Enrollment enrollment = findById(id);

        if (status == EnrollmentStatus.COMPLETED && enrollment.getGrade() == null) {
            throw new BusinessRuleException(
                    "No se puede completar la matrícula sin calificación asignada");
        }

        enrollment.setStatus(status);
        return enrollment;
    }

    @Transactional
    public void delete(Long id) {
        Enrollment enrollment = findById(id);
        enrollmentRepository.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> findByStudentId(Long studentId) {
        studentService.findById(studentId); // Verificar existencia → 404
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> findByCourseId(Long courseId) {
        courseService.findById(courseId);
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public Double findAverageGradeByCourseId(Long courseId) {
        courseService.findById(courseId);
        return enrollmentRepository.findAverageGradeByCourseId(courseId);
    }
}