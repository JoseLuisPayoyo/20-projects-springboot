package com.payoyo.working.repository;

import com.payoyo.working.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /** Filtra cursos por categoría (ej: "Matemáticas", "Ciencias") */
    List<Course> findByCategory(String category);
}