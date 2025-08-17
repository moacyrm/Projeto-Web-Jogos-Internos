package com.provaweb.jogosinternos.dto;

import lombok.Data;

import com.provaweb.jogosinternos.entities.Jogo;

@Data
public class EliminatoriaDTO {
    private String equipe1Nome;
    private String equipe2Nome;
    private Integer placarEquipe1;
    private Integer placarEquipe2;
    private String status; // "FINALIZADO" ou "AGENDADO"
    private String vencedor;

    public EliminatoriaDTO(Jogo jogo) {
        this.equipe1Nome = jogo.getEquipe1().getNome();
        this.equipe2Nome = jogo.getEquipe2().getNome();
        this.placarEquipe1 = jogo.getPlacarEquipe1();
        this.placarEquipe2 = jogo.getPlacarEquipe2();
        this.status = jogo.isFinalizado() ? "FINALIZADO" : "AGENDADO";

        if (jogo.isFinalizado()) {
            if (jogo.getPlacarEquipe1() > jogo.getPlacarEquipe2()) {
                this.vencedor = equipe1Nome;
            } else if (jogo.getPlacarEquipe2() > jogo.getPlacarEquipe1()) {
                this.vencedor = equipe2Nome;
            } else {
                this.vencedor = "EMPATE";
            }
        } else {
            this.vencedor = null;
        }
    }
}
