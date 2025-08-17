package com.provaweb.jogosinternos.dto;

import lombok.Data;
import java.util.List;

@Data
public class EquipeDetalhesDTO {
    public Long id;
    private String nome;
    private String esporte;
    private String campus;
    private String evento;
    private String curso;
    private String tecnico;
    private Integer vitorias;
    private Integer derrotas;
    private Integer jogosDisputados;
    private Integer pontuacao;

    private List<AtletaResumoDTO> atletas;
    private List<JogoResumoDTO> proximosJogos;

    
}
