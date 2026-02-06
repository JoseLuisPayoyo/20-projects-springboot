package com.payoyo.working.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false)
    private String name;

    // unique = true → no puede haber dos socios con el mismo email
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    @Column(unique = true, nullable = false)
    private String email;

    // Teléfono opcional → no lleva @NotBlank
    @Pattern(regexp = "^\\+?\\d[\\d\\s]{7,15}$",
            message = "Formato de teléfono no válido")
    private String phone;

    // Fecha de alta automática, no editable
    @Column(updatable = false)
    private LocalDate membershipDate;

    // Activo por defecto al registrarse
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // mappedBy = "member" → Loan es el dueño (tiene FK member_id)
    // Sin cascade → los préstamos son registros históricos, no se borran con el socio
    @OneToMany(mappedBy = "member")
    @JsonIgnore
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.membershipDate = LocalDate.now();
        if (this.active == null) {
            this.active = true;
        }
    }
}