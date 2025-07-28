package com.provaweb.jogosinternos.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PartidaDTO {
    private String equipe1;
    private String equipe2;
    private Integer placar1;
    private Integer placar2;
    private String vencedor;
    private LocalDateTime data;
    private String status;
}