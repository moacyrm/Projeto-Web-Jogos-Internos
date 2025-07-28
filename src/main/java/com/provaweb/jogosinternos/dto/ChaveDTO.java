package com.provaweb.jogosinternos.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChaveDTO {
    private Long id;
    private String nome;
    private String tipo;
    private List<FaseDTO> fases;
    
}
