package com.provaweb.jogosinternos.dto;

import java.time.LocalDateTime;

import com.provaweb.jogosinternos.entities.Jogo;

import lombok.Data;

@Data
public class JogoDTO {
    private Long id;
    private String equipe1Nome;
    private String equipe2Nome;
    private Integer placarEquipe1;
    private Integer placarEquipe2;
    private LocalDateTime dataHora;
    private String status;
    private String fase;

    public JogoDTO(Jogo jogo) {
        this.id = jogo.getId();
        this.equipe1Nome = jogo.getEquipe1().getNome();
        this.equipe2Nome = jogo.getEquipe2().getNome();
        this.placarEquipe1 = jogo.getPlacarEquipe1();
        this.placarEquipe2 = jogo.getPlacarEquipe2();
        this.dataHora = jogo.getDataHora();
        this.status = jogo.isFinalizado() ? "FINALIZADO" : "AGENDADO";
        this.fase = jogo.getFase() != null ? jogo.getFase() : "Fase de Grupos";
    }
}
