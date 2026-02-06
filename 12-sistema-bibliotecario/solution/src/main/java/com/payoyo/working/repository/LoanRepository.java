package com.payoyo.working.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.payoyo.working.entity.Loan;
import com.payoyo.working.entity.enums.LoanStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Filtrar préstamos por estado (ACTIVE, RETURNED, OVERDUE)
    List<Loan> findByStatus(LoanStatus status);

    // Historial completo de préstamos de un libro
    List<Loan> findByBookId(Long bookId);

    // Préstamos de un socio
    List<Loan> findByMemberId(Long memberId);

    // Contar préstamos activos de un socio → para validar límite de 5
    long countByMemberIdAndStatus(Long memberId, LoanStatus status);

    // @Query necesario: combina estado + comparación de fecha (no expresable solo con derived queries)
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.dueDate < :today")
    List<Loan> findOverdueLoans(@Param("today") LocalDate today);

    // Verificar si un libro tiene préstamos activos → para validar eliminación de libro
    boolean existsByBookIdAndStatus(Long bookId, LoanStatus status);

    // Verificar si un socio tiene préstamos activos → para validar eliminación de socio
    boolean existsByMemberIdAndStatus(Long memberId, LoanStatus status);
}