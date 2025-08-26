package com.provaweb.jogosinternos.dto;

import com.provaweb.jogosinternos.entities.Equipe;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipeDTO {
    private Long id;
    private String nome;
    private String esporte;
    private String grupo;
    private String evento;
    private Long eventoId;
    private String curso;
    private Long atletasCount;

    public EquipeDTO(Equipe equipe) {
        this.id = equipe.getId();
        this.nome = equipe.getNome();
        this.esporte = equipe.getEsporte() != null ? equipe.getEsporte().getNome() : null;
        this.grupo = equipe.getGrupo() != null ? equipe.getGrupo().getNome() : null;
        this.evento = equipe.getEvento() != null ? equipe.getEvento().getNome() : null;
        this.eventoId = equipe.getEvento() != null ? equipe.getEvento().getId() : null;
        this.curso = equipe.getCurso() != null ? equipe.getCurso().getNome() : null;
    }

    public void setAtletasCount(Long atletasCount) {
        this.atletasCount = atletasCount;
    }
}
