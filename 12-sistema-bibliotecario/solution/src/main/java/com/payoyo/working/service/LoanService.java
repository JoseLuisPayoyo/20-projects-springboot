package com.payoyo.working.service;

import com.payoyo.working.entity.Book;
import com.payoyo.working.entity.Loan;
import com.payoyo.working.entity.Member;
import com.payoyo.working.entity.enums.LoanStatus;
import com.payoyo.working.exception.BusinessRuleException;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final int MAX_ACTIVE_LOANS = 5;

    private final LoanRepository loanRepository;
    private final BookService bookService;     // Inyecta Service, no Repository
    private final MemberService memberService; // Cada service gestiona su propia entidad

    // @Transactional: modifica Loan + Book (available) → si algo falla, rollback de ambos
    @Transactional
    public Loan createLoan(Long bookId, Long memberId) {
        Book book = bookService.findById(bookId);
        Member member = memberService.findById(memberId);

        // Validación 1: socio activo
        if (!member.getActive()) {
            throw new BusinessRuleException(
                    "El socio '" + member.getName() + "' no está activo");
        }

        // Validación 2: libro disponible
        if (!book.getAvailable()) {
            throw new BusinessRuleException(
                    "El libro '" + book.getTitle() + "' no está disponible");
        }

        // Validación 3: límite de préstamos activos
        long activeLoans = loanRepository.countByMemberIdAndStatus(memberId, LoanStatus.ACTIVE);
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            throw new BusinessRuleException(
                    "El socio '" + member.getName() + "' ha alcanzado el límite de "
                            + MAX_ACTIVE_LOANS + " préstamos activos");
        }

        // Marcar libro como no disponible (se persiste por contexto transaccional)
        book.setAvailable(false);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);

        return loanRepository.save(loan);
    }

    // @Transactional: modifica Loan (status, returnDate) + Book (available)
    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = findById(loanId);

        // Solo préstamos activos se pueden devolver
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException("Este préstamo ya fue devuelto");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        loan.getBook().setAvailable(true); // Libro disponible de nuevo

        return loanRepository.save(loan);
    }

    public List<Loan> findAll() {
        return loanRepository.findAll();
    }

    public Loan findById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con id: " + id));
    }

    public List<Loan> findActiveLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE);
    }

    // Préstamos vencidos: activos cuya fecha límite ya pasó
    public List<Loan> findOverdueLoans() {
        return loanRepository.findOverdueLoans(LocalDate.now());
    }

    public List<Loan> findByBookId(Long bookId) {
        return loanRepository.findByBookId(bookId);
    }
}