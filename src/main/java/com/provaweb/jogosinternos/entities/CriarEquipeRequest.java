package com.provaweb.jogosinternos.entities;

import lombok.Data;

@Data
public class CriarEquipeRequest {
    private String nome;
    private Long eventoId;
    private Long esporteId;
    private Long campusId;
}
