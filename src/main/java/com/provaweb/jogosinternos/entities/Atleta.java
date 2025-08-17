package com.provaweb.jogosinternos.entities;

import com.provaweb.jogosinternos.dto.AtletaDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo = TipoUsuario.ATLETA;

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
        dto.setId(this.id);
        dto.setNomeCompleto(this.nomeCompleto);
        dto.setApelido(this.apelido);
        dto.setMatricula(this.matricula);
        dto.setTelefone(this.telefone);
        dto.setTecnico(this.tecnico);
        dto.setTipo(this.tipo != null ? this.tipo.name() : null);

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
