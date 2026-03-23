package com.payoyo.working.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "La categoría es obligatoria")
    @Column(nullable = false)
    private String category;

    @NotNull(message = "El número máximo de estudiantes es obligatorio")
    @Min(value = 1, message = "El curso debe admitir al menos 1 estudiante")
    @Column(nullable = false)
    private Integer maxStudents;

    /**
     * Default true: los cursos se crean activos.
     * @PrePersist garantiza el valor incluso si no se envía en el JSON.
     */
    @Column(nullable = false)
    private Boolean active;

    /** Mismo patrón que Student: @JsonIgnore para cortar recursión */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();

    public Course() {
    }

    public Course(String name, String category, Integer maxStudents) {
        this.name = name;
        this.category = category;
        this.maxStudents = maxStudents;
        this.active = true;
    }

    /** Asigna active=true si no se proporcionó valor */
    @PrePersist
    protected void onCreate() {
        if (this.active == null) {
            this.active = true;
        }
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }
}