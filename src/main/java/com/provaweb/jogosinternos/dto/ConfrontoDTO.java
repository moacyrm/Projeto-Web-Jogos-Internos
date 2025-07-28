package com.provaweb.jogosinternos.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConfrontoDTO {
    private String equipe1;
    private String equipe2;
    private LocalDateTime dataHora;
}