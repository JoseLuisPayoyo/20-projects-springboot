package com.payoyo.working.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import com.payoyo.working.entity.enums.LoanStatus;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fecha de préstamo: se genera automáticamente
    @Column(updatable = false, nullable = false)
    private LocalDate loanDate;

    // Fecha límite: loanDate + 14 días, calculada en @PrePersist
    @Column(nullable = false)
    private LocalDate dueDate;

    // null hasta que se devuelve el libro
    private LocalDate returnDate;

    // EnumType.STRING → guarda "ACTIVE", "RETURNED" en vez de 0, 1
    // Más legible en BD y resistente a cambios de orden en el enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;

    // ManyToOne: Loan es el dueño de AMBAS relaciones
    // LAZY → no carga Book/Member hasta que se accede (mejor rendimiento en consultas masivas)
    // @JoinColumn → define la FK en la tabla loan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Calcula loanDate y dueDate automáticamente al crear
    @PrePersist
    protected void onCreate() {
        this.loanDate = LocalDate.now();
        this.dueDate = this.loanDate.plusDays(14);
    }
}