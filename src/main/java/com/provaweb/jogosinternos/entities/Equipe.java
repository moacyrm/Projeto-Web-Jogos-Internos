package com.provaweb.jogosinternos.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.provaweb.jogosinternos.dto.EquipeDTO;

import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = false)
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    @ToString.Exclude
    private Curso curso;

    @ManyToOne
    @ToString.Exclude
    private Esporte esporte;

    @ManyToOne
    @ToString.Exclude
    private Campus campus;

    @ManyToOne
    @ToString.Exclude
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonBackReference
    @ToString.Exclude
    private Grupo grupo;

    public EquipeDTO toDTO() {
        return new EquipeDTO(this);
    }

    @OneToMany(mappedBy = "equipe", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<Atleta> atletas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    @ToString.Exclude
    private Atleta tecnico;

    @Enumerated(EnumType.STRING)
    private EquipeStatus status = EquipeStatus.PROJETO;
}
