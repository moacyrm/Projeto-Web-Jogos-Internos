package com.provaweb.jogosinternos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankingDTO {
    private String equipeNome;
    private int pontos;
    private int jogos;
    private int saldoGols;
}
