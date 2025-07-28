package com.provaweb.jogosinternos.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Jogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Equipe equipe1;

    @ManyToOne
    private Equipe equipe2;

    private int placarEquipe1;
    private int placarEquipe2;

    @ManyToOne
    @JsonBackReference
    private Grupo grupo;

    private LocalDateTime dataHora;

    private boolean woEquipe1;
    private boolean woEquipe2;

    private boolean finalizado;

    @ManyToOne
    private Evento evento;

    private String fase;

    @ManyToOne
    @JoinColumn(name = "chave_eliminatoria_id") 
    private ChaveEliminatoria chaveEliminatoria;

    @ManyToOne
    private Jogo jogoAnteriorEquipe1;

    @ManyToOne
    private Jogo jogoAnteriorEquipe2;
}