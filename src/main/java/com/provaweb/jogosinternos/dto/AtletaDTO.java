package com.provaweb.jogosinternos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtletaDTO {
    private Long id;
    private String nomeCompleto;
    private String apelido;
    private String matricula;
    private String telefone;
    private boolean tecnico;
    private String equipeNome;
    private String esporteNome;
    private String eventoNome;
    private String tipo;

    public AtletaDTO(Long id, String nomeCompleto, String matricula) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.matricula = matricula;
    }

}
