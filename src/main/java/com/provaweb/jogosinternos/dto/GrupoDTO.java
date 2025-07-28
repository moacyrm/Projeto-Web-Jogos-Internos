package com.provaweb.jogosinternos.dto;

import java.util.List;
import lombok.Data;

@Data
public class GrupoDTO {
    private String nomeGrupo;
    private List<ConfrontoDTO> confrontos;
}
