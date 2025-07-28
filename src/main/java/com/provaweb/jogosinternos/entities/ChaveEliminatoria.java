package com.provaweb.jogosinternos.entities;

import com.provaweb.jogosinternos.dto.ChaveDTO;
import com.provaweb.jogosinternos.dto.FaseDTO;
import com.provaweb.jogosinternos.dto.PartidaDTO;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "chaves_eliminatorias")
public class ChaveEliminatoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoChave tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    public enum TipoChave {
        ELIMINATORIA_SIMPLES,
        DUPLA_ELIMINACAO
    }

    @OneToMany(mappedBy = "chaveEliminatoria")
    private List<Jogo> jogos;

    public List<Jogo> getJogos() {
        return jogos;
    }

    public ChaveDTO toDTO(List<Jogo> jogosDaChave) {
        ChaveDTO dto = new ChaveDTO();
        dto.setId(this.id);
        dto.setNome(this.nome);
        dto.setTipo(this.tipo.name());
        dto.setFases(this.gerarFasesDTO(jogosDaChave));
        return dto;
    }

    private List<FaseDTO> gerarFasesDTO(List<Jogo> jogos) {
        if (jogos == null || jogos.isEmpty()) {
            return List.of();
        }

        Map<String, List<Jogo>> jogosPorFase = jogos.stream()
                .collect(Collectors.groupingBy(Jogo::getFase));

        return jogosPorFase.entrySet().stream()
                .map(entry -> {
                    FaseDTO faseDTO = new FaseDTO();
                    faseDTO.setNome(entry.getKey());
                    faseDTO.setPartidas(entry.getValue().stream()
                            .map(this::toPartidaDTO)
                            .sorted(Comparator.comparing(PartidaDTO::getData))
                            .collect(Collectors.toList()));
                    return faseDTO;
                })
                .sorted(Comparator.comparing(FaseDTO::getNome))
                .collect(Collectors.toList());
    }

    private PartidaDTO toPartidaDTO(Jogo jogo) {
        PartidaDTO dto = new PartidaDTO();
        dto.setEquipe1(jogo.getEquipe1().getNome());
        dto.setEquipe2(jogo.getEquipe2().getNome());
        dto.setPlacar1(jogo.getPlacarEquipe1());
        dto.setPlacar2(jogo.getPlacarEquipe2());
        dto.setData(jogo.getDataHora());
        dto.setStatus(jogo.isFinalizado() ? "FINALIZADA" : "AGENDADA");

        if (jogo.isFinalizado()) {
            dto.setVencedor(jogo.getPlacarEquipe1() > jogo.getPlacarEquipe2()
                    ? jogo.getEquipe1().getNome()
                    : jogo.getEquipe2().getNome());
        }
        return dto;
    }
}