package com.payoyo.working.repository;

import com.payoyo.working.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Búsqueda exacta por ISBN (para consultas)
    Optional<Book> findByIsbn(String isbn);

    // Más eficiente que findByIsbn para validaciones: solo hace COUNT, no trae la entidad
    boolean existsByIsbn(String isbn);

    // containsIgnoreCase → genera LOWER(author) LIKE LOWER('%author%')
    List<Book> findByAuthorContainingIgnoreCase(String author);

    // IgnoreCase → búsqueda case-insensitive por género exacto
    List<Book> findByGenreIgnoreCase(String genre);

    // Derived query: Spring genera WHERE available = true
    List<Book> findByAvailableTrue();
}