package com.payoyo.working.service;

import com.payoyo.working.entity.Book;
import com.payoyo.working.entity.enums.LoanStatus;
import com.payoyo.working.exception.BusinessRuleException;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.BookRepository;
import com.payoyo.working.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // Inyección por constructor vía Lombok (sin @Autowired)
public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public Book createBook(Book book) {
        // Validar unicidad de ISBN antes de persistir
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BusinessRuleException("Ya existe un libro con ISBN: " + book.getIsbn());
        }
        return bookRepository.save(book);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + id));
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ISBN: " + isbn));
    }

    public List<Book> findAvailable() {
        return bookRepository.findByAvailableTrue();
    }

    public List<Book> findByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<Book> findByGenre(String genre) {
        return bookRepository.findByGenreIgnoreCase(genre);
    }

    public Book updateBook(Long id, Book bookDetails) {
        Book book = findById(id);

        // Solo validar ISBN si cambió → evita falso positivo consigo mismo
        if (!book.getIsbn().equals(bookDetails.getIsbn())
                && bookRepository.existsByIsbn(bookDetails.getIsbn())) {
            throw new BusinessRuleException("Ya existe un libro con ISBN: " + bookDetails.getIsbn());
        }

        // NO actualizamos available → se gestiona exclusivamente vía préstamos
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setIsbn(bookDetails.getIsbn());
        book.setGenre(bookDetails.getGenre());
        book.setPublishedYear(bookDetails.getPublishedYear());

        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        Book book = findById(id);

        // Proteger integridad: no eliminar si hay préstamos activos
        if (loanRepository.existsByBookIdAndStatus(id, LoanStatus.ACTIVE)) {
            throw new BusinessRuleException(
                    "No se puede eliminar el libro '" + book.getTitle() + "' porque tiene préstamos activos");
        }
        bookRepository.delete(book);
    }
}