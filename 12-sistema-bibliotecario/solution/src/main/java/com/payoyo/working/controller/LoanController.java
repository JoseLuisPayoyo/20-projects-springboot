package com.payoyo.working.controller;

import com.payoyo.working.entity.Loan;
import com.payoyo.working.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // Body simple con Map → solo necesitamos bookId y memberId, no una entidad completa
    // Alternativa más robusta: crear un LoanRequestDTO (se verá en proyectos 16-20)
    @PostMapping
    public ResponseEntity<Loan> createLoan(@RequestBody Map<String, Long> request) {
        Long bookId = request.get("bookId");
        Long memberId = request.get("memberId");
        return new ResponseEntity<>(loanService.createLoan(bookId, memberId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.findById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Loan>> getActiveLoans() {
        return ResponseEntity.ok(loanService.findActiveLoans());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Loan>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.findOverdueLoans());
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Loan>> getLoansByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(loanService.findByBookId(bookId));
    }

    // PATCH → operación específica de devolución, no un update genérico
    @PatchMapping("/{id}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }
}