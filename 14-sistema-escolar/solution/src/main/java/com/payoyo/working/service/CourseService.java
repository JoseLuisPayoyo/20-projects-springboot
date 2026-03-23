package com.payoyo.working.service;

import com.payoyo.working.entity.Course;
import com.payoyo.working.entity.EnrollmentStatus;
import com.payoyo.working.entity.Student;
import com.payoyo.working.exception.BusinessRuleException;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.CourseRepository;
import com.payoyo.working.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseService(CourseRepository courseRepository,
                         EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso no encontrado con id: " + id));
    }

    @Transactional
    public Course create(Course course) {
        return courseRepository.save(course);
    }

    @Transactional
    public Course update(Long id, Course courseData) {
        Course existing = findById(id);

        existing.setName(courseData.getName());
        existing.setCategory(courseData.getCategory());
        existing.setMaxStudents(courseData.getMaxStudents());
        existing.setActive(courseData.getActive());

        return existing;
    }

    @Transactional
    public void delete(Long id) {
        Course course = findById(id);

        if (enrollmentRepository.existsByCourseIdAndStatus(id, EnrollmentStatus.ACTIVE)) {
            throw new BusinessRuleException(
                    "No se puede eliminar el curso: tiene matrículas activas");
        }

        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public List<Course> findByCategory(String category) {
        return courseRepository.findByCategory(category);
    }

    /**
     * Obtiene los estudiantes de un curso navegando por sus matrículas.
     * Mismo patrón que StudentService.findCoursesByStudentId pero inverso.
     */
    @Transactional(readOnly = true)
    public List<Student> findStudentsByCourseId(Long courseId) {
        findById(courseId);

        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollment -> enrollment.getStudent())
                .collect(Collectors.toList());
    }
}