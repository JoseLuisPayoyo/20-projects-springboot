package com.payoyo.working.service;

import com.payoyo.working.entity.Course;
import com.payoyo.working.entity.EnrollmentStatus;
import com.payoyo.working.entity.Student;
import com.payoyo.working.exception.BusinessRuleException;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.EnrollmentRepository;
import com.payoyo.working.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    /** Inyección por constructor — inmutable, testeable, sin @Autowired */
    public StudentService(StudentRepository studentRepository,
                          EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante no encontrado con id: " + id));
    }

    @Transactional
    public Student create(Student student) {
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new BusinessRuleException(
                    "Ya existe un estudiante con el email: " + student.getEmail());
        }
        return studentRepository.save(student);
    }

    @Transactional
    public Student update(Long id, Student studentData) {
        Student existing = findById(id);

        existing.setFirstName(studentData.getFirstName());
        existing.setLastName(studentData.getLastName());
        existing.setEmail(studentData.getEmail());
        existing.setBirthDate(studentData.getBirthDate());

        /**
         * No llamamos save() explícitamente.
         * La entidad está en estado "managed" (dentro de transacción),
         * Hibernate detecta los cambios y hace UPDATE automáticamente (dirty checking).
         */
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        Student student = findById(id);

        /** Protección: no eliminar si tiene matrículas activas */
        if (enrollmentRepository.existsByStudentIdAndStatus(id, EnrollmentStatus.ACTIVE)) {
            throw new BusinessRuleException(
                    "No se puede eliminar el estudiante: tiene matrículas activas");
        }

        studentRepository.delete(student);
    }

    /**
     * Obtiene los cursos de un estudiante a través de sus matrículas.
     * Navegamos: Student → enrollments → course de cada enrollment.
     */
    @Transactional(readOnly = true)
    public List<Course> findCoursesByStudentId(Long studentId) {
        findById(studentId); // Verificar que existe, lanza 404 si no

        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(enrollment -> enrollment.getCourse())
                .collect(Collectors.toList());
    }
}