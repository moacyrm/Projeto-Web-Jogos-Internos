package com.provaweb.jogosinternos.entities;

import com.provaweb.jogosinternos.dto.AtletaDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Atleta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeCompleto;
    private String apelido;
    private String matricula;
    private String telefone;
    private String senha;

    private boolean tecnico;

    @ManyToOne
    private Curso curso;

    @ManyToOne
    private Equipe equipe;

    public AtletaDTO toDTO() {
        AtletaDTO dto = new AtletaDTO();
        dto.setNomeCompleto(this.nomeCompleto);

        if (this.equipe != null) {
            dto.setEquipeNome(this.equipe.getNome());

            if (this.equipe.getEsporte() != null) {
                dto.setEsporteNome(this.equipe.getEsporte().getNome());
            }

            if (this.equipe.getEvento() != null) {
                dto.setEventoNome(this.equipe.getEvento().getNome());
            }
        }

        return dto;
    }
}
