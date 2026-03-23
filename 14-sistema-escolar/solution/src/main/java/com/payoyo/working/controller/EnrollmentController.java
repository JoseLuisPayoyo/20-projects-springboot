package com.payoyo.working.controller;

import com.payoyo.working.entity.Enrollment;
import com.payoyo.working.entity.EnrollmentStatus;
import com.payoyo.working.service.CourseService;
import com.payoyo.working.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    public EnrollmentController(EnrollmentService enrollmentService,
                                CourseService courseService) {
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Enrollment>> findAll() {
        return ResponseEntity.ok(enrollmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enrollment> findById(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.findById(id));
    }

    /**
     * El body espera {"studentId": 1, "courseId": 2}.
     * Usamos Map para recibir los IDs sin crear una clase extra.
     * En proyectos con DTOs (Proyecto 16+) esto sería un CreateEnrollmentRequest.
     */
    @PostMapping
    public ResponseEntity<Enrollment> create(@RequestBody Map<String, Long> request) {
        Long studentId = request.get("studentId");
        Long courseId = request.get("courseId");

        Enrollment enrollment = enrollmentService.create(studentId, courseId);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    /**
     * Endpoint específico para nota — no es un PUT genérico.
     * Recibe {"grade": 8.5} y solo modifica la calificación.
     */
    @PutMapping("/{id}/grade")
    public ResponseEntity<Enrollment> updateGrade(@PathVariable Long id,
                                                  @RequestBody Map<String, Double> request) {
        Double grade = request.get("grade");
        return ResponseEntity.ok(enrollmentService.updateGrade(id, grade));
    }

    /**
     * Endpoint específico para estado.
     * Recibe {"status": "COMPLETED"} como String y lo convierte a enum.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Enrollment> updateStatus(@PathVariable Long id,
                                                   @RequestBody Map<String, String> request) {
        EnrollmentStatus status = EnrollmentStatus.valueOf(request.get("status"));
        return ResponseEntity.ok(enrollmentService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> findByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.findByStudentId(studentId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Enrollment>> findByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.findByCourseId(courseId));
    }

    /**
     * Construye la respuesta de nota media manualmente con un Map.
     * Sin DTOs, es la forma más directa de devolver datos calculados
     * que no corresponden a ninguna entidad.
     */
    @GetMapping("/course/{courseId}/average")
    public ResponseEntity<Map<String, Object>> getAverageGrade(@PathVariable Long courseId) {
        Double average = enrollmentService.findAverageGradeByCourseId(courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("courseName", courseService.findById(courseId).getName());
        response.put("averageGrade", average != null ? average : "Sin calificaciones");

        return ResponseEntity.ok(response);
    }
}