package com.provaweb.jogosinternos.dto;

import lombok.Data;
import java.util.List;

@Data
public class FaseDTO {
    private String nome;
    private List<PartidaDTO> partidas;
}
