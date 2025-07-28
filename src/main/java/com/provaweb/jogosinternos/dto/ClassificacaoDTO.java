package com.provaweb.jogosinternos.dto;

import com.provaweb.jogosinternos.entities.Equipe;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassificacaoDTO {
    private Equipe equipe;
    private int pontos;
    private int saldo;
    private int pontosPro;
    private int pontosContra;
    
}
