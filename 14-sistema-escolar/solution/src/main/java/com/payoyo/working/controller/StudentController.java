package com.payoyo.working.controller;

import com.payoyo.working.entity.Course;
import com.payoyo.working.entity.Student;
import com.payoyo.working.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<Student>> findAll() {
        return ResponseEntity.ok(studentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> findById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.findById(id));
    }

    /**
     * @Valid activa Bean Validation sobre el body.
     * Si falla, Spring lanza MethodArgumentNotValidException
     * que captura nuestro GlobalExceptionHandler.
     */
    @PostMapping
    public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
        return new ResponseEntity<>(studentService.create(student), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> update(@PathVariable Long id,
                                          @Valid @RequestBody Student student) {
        return ResponseEntity.ok(studentService.update(id, student));
    }

    /** 204 No Content: eliminación exitosa sin body de respuesta */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint de relación: cursos del estudiante.
     * Vive en StudentController porque la URL parte de /api/students,
     * pero delega a StudentService que navega por las matrículas.
     */
    @GetMapping("/{id}/courses")
    public ResponseEntity<List<Course>> findCourses(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.findCoursesByStudentId(id));
    }
}