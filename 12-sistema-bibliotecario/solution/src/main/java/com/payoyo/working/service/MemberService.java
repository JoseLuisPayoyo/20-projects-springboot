package com.payoyo.working.service;

import com.payoyo.working.entity.Loan;
import com.payoyo.working.entity.Member;
import com.payoyo.working.entity.enums.LoanStatus;
import com.payoyo.working.exception.BusinessRuleException;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.LoanRepository;
import com.payoyo.working.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public Member createMember(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new BusinessRuleException("Ya existe un socio con email: " + member.getEmail());
        }
        return memberRepository.save(member);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con id: " + id));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con email: " + email));
    }

    // Obtener préstamos de un socio (valida que el socio exista)
    public List<Loan> findLoansByMemberId(Long id) {
        findById(id); // Lanza 404 si no existe
        return loanRepository.findByMemberId(id);
    }

    public Member updateMember(Long id, Member memberDetails) {
        Member member = findById(id);

        // Solo validar email si cambió
        if (!member.getEmail().equals(memberDetails.getEmail())
                && memberRepository.existsByEmail(memberDetails.getEmail())) {
            throw new BusinessRuleException("Ya existe un socio con email: " + memberDetails.getEmail());
        }

        member.setName(memberDetails.getName());
        member.setEmail(memberDetails.getEmail());
        member.setPhone(memberDetails.getPhone());

        return memberRepository.save(member);
    }

    // PATCH: solo cambia active, no elimina préstamos existentes
    public Member deactivateMember(Long id) {
        Member member = findById(id);
        member.setActive(false);
        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        Member member = findById(id);

        if (loanRepository.existsByMemberIdAndStatus(id, LoanStatus.ACTIVE)) {
            throw new BusinessRuleException(
                    "No se puede eliminar el socio '" + member.getName() + "' porque tiene préstamos activos");
        }
        memberRepository.delete(member);
    }
}