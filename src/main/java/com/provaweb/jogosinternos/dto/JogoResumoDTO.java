package com.provaweb.jogosinternos.dto;

import lombok.Data;

@Data
public class JogoResumoDTO {
    private Long id;
    private String equipe1Nome;
    private String equipe2Nome;
    private String fase;
    private String status;
    private Integer placarEquipe1;
    private Integer placarEquipe2;

}
