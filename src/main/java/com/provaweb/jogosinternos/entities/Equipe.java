package com.provaweb.jogosinternos.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.provaweb.jogosinternos.dto.EquipeDTO;

import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    private Curso curso;

    @ManyToOne
    private Esporte esporte;

    @ManyToOne
    private Campus campus;

    @ManyToOne
    private Evento evento;

    @ManyToOne
    @JoinColumn(name = "grupo_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonBackReference
    private Grupo grupo;

    public EquipeDTO toDTO() {
        return new EquipeDTO(this);
    }
}
