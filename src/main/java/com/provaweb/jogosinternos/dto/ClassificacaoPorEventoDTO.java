package com.provaweb.jogosinternos.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClassificacaoPorEventoDTO {
    private String nomeEvento;
    private String tipoEvento;
    private List<ClassificacaoSimplesDTO> classificacoes;
}
