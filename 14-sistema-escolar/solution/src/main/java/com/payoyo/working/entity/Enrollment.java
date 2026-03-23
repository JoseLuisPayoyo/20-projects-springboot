package com.payoyo.working.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "enrollments",
    /**
     * Restricción única a nivel de BD: un estudiante no puede
     * matricularse dos veces en el mismo curso.
     * Es la red de seguridad final — la validación principal
     * se hace en el Service antes de llegar aquí.
     */
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"student_id", "course_id"},
        name = "uk_student_course"
    )
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne: muchas matrículas pueden pertenecer a un estudiante.
     * Enrollment es el owning side — aquí vive la FK en la tabla.
     * FetchType.LAZY: no cargamos el Student completo a menos que se acceda.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * ManyToOne: muchas matrículas pueden pertenecer a un curso.
     * Mismo patrón que con Student — FK en esta tabla.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Se auto-asigna al crear la matrícula mediante @PrePersist */
    @Column(nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    /** Nullable: la nota se asigna después, no al matricularse */
    @DecimalMin(value = "0.0", message = "La nota mínima es 0.0")
    @DecimalMax(value = "10.0", message = "La nota máxima es 10.0")
    private Double grade;

    /** Estado inicial ACTIVE, se cambia vía endpoint dedicado */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EnrollmentStatus status;

    public Enrollment() {
    }

    public Enrollment(Student student, Course course) {
        this.student = student;
        this.course = course;
        this.status = EnrollmentStatus.ACTIVE;
    }

    /**
     * @PrePersist se ejecuta justo antes del INSERT.
     * Garantiza que enrolledAt y status siempre tienen valor,
     * independientemente de cómo se construya el objeto.
     */
    @PrePersist
    protected void onCreate() {
        this.enrolledAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = EnrollmentStatus.ACTIVE;
        }
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
}