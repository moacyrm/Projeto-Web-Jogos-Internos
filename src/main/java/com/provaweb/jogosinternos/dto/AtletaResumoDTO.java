package com.provaweb.jogosinternos.dto;

import lombok.Data;

@Data
public class AtletaResumoDTO {
    private Long id;
    private String nomeCompleto;
    private String matricula;
    private String email;
    private String telefone;
    private String curso;
    private boolean tecnico;
}