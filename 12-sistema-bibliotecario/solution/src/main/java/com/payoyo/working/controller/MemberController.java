package com.payoyo.working.controller;

import com.payoyo.working.entity.Loan;
import com.payoyo.working.entity.Member;
import com.payoyo.working.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Member> createMember(@Valid @RequestBody Member member) {
        return new ResponseEntity<>(memberService.createMember(member), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Member> getMemberByEmail(@PathVariable String email) {
        return ResponseEntity.ok(memberService.findByEmail(email));
    }

    // Endpoint anidado: préstamos dentro del contexto de un socio
    @GetMapping("/{id}/loans")
    public ResponseEntity<List<Loan>> getMemberLoans(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findLoansByMemberId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @Valid @RequestBody Member member) {
        return ResponseEntity.ok(memberService.updateMember(id, member));
    }

    // PATCH → modificación parcial (solo cambia active, no reemplaza la entidad)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Member> deactivateMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.deactivateMember(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}