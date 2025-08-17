package com.provaweb.jogosinternos.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.dto.AtletaResumoDTO;
import com.provaweb.jogosinternos.dto.EquipeDetalhesDTO;
import com.provaweb.jogosinternos.dto.JogoResumoDTO;
import com.provaweb.jogosinternos.entities.*;
import com.provaweb.jogosinternos.repositories.AtletaRepository;
import com.provaweb.jogosinternos.repositories.EquipeRepository;
import com.provaweb.jogosinternos.repositories.EsporteRepository;
import com.provaweb.jogosinternos.repositories.JogoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipeService {
    private final EquipeRepository equipeRepository;
    private final AtletaService atletaService;
    private final EsporteRepository esporteRepository;
    private final AtletaRepository atletaRepository;
    private final JogoRepository jogoRepository;

    public Equipe cadastEquipe(Equipe equipe, Long tecnicoId) {
        if (equipe.getEvento() == null || equipe.getEvento().getId() == null) {
            throw new RuntimeException("Evento não informado.");
        }

        if (equipe.getCurso() == null || equipe.getCurso().getId() == null) {
            throw new RuntimeException("Curso não informado.");
        }

        if (equipe.getEsporte() == null || equipe.getEsporte().getId() == null) {
            throw new RuntimeException("Esporte não informado.");
        }

        if (equipeRepository.existsByEventoIdAndNome(equipe.getEvento().getId(), equipe.getNome())) {
            throw new RuntimeException("Já existe uma equipe com esse nome no evento.");
        }

        if (equipeRepository.existsByEventoIdAndCursoId(equipe.getEvento().getId(), equipe.getCurso().getId())) {
            throw new RuntimeException("Já existe uma equipe para esse curso nesse evento.");
        }

        if (tecnicoId != null && !atletaService.isTecnico(tecnicoId)) {
            throw new RuntimeException("Você não é técnico para cadastrar a equipe.");
        }

        Esporte esporte = esporteRepository.findById(equipe.getEsporte().getId())
                .orElseThrow(() -> new RuntimeException("Esporte não encontrado."));

        int minimo = esporte.getMinimoAtletas();
        int maximo = esporte.getMaximoAtletas();

        if (minimo <= 0 || maximo <= 0 || minimo > maximo) {
            throw new RuntimeException("Configuração de esporte inválida.");
        }

        equipe.setEsporte(esporte);
        Equipe equipeSalva = equipeRepository.save(equipe);

        if (tecnicoId != null) {
            atletaService.atualizarEquipe(tecnicoId, equipeSalva.getId(), tecnicoId);
        }

        return equipeSalva;
    }

    public List<Equipe> listarTodos() {
        return equipeRepository.findAll();
    }

    public Equipe buscarPorId(Long id) {
        return equipeRepository.findById(id).orElseThrow(() -> new RuntimeException("Equipe não encontrada."));
    }

    public List<Equipe> listPorEvento(Long eventoId) {
        return equipeRepository.findByEventoId(eventoId);
    }

    public Equipe atualizarEquipe(Long id, Equipe novaEquipe) {
        Equipe equipeExistente = buscarPorId(id);

        equipeExistente.setNome(novaEquipe.getNome());
        equipeExistente.setCurso(novaEquipe.getCurso());
        equipeExistente.setEsporte(novaEquipe.getEsporte());
        equipeExistente.setCampus(novaEquipe.getCampus());
        equipeExistente.setEvento(novaEquipe.getEvento());
        equipeExistente.setGrupo(novaEquipe.getGrupo());

        return equipeRepository.save(equipeExistente);
    }

    public Equipe buscarOuCriarEquipeBye(Evento evento, Esporte esporte, Curso curso, Campus campus) {
        return equipeRepository.findByNomeAndEventoId("Bye", evento.getId())
                .orElseGet(() -> {
                    Equipe bye = new Equipe();
                    bye.setNome("Bye");
                    bye.setEvento(evento);
                    bye.setEsporte(esporte);
                    bye.setCurso(curso);
                    bye.setCampus(campus);
                    return equipeRepository.save(bye);
                });
    }

    public List<Equipe> buscarEquipesPorEvento(Long eventoId) {
        return equipeRepository.findByEventoEventoId(eventoId);
    }

    public EquipeDetalhesDTO getEquipeDetalhesPorMatriculaAtleta(String matricula) {
        Atleta atleta = atletaRepository.findByMatriculaIgnoreCase(matricula)
                .orElseThrow(() -> new RuntimeException("Atleta nao encontrado"));

        if (atleta.getEquipe() == null) {
            throw new RuntimeException("Atleta não esta em uma equipe");
        }

        Equipe equipe = atleta.getEquipe();
        return mapToEquipeDetalhesDTO(equipe);
    }

    private EquipeDetalhesDTO mapToEquipeDetalhesDTO(Equipe equipe) {
        EquipeDetalhesDTO dto = new EquipeDetalhesDTO();
        dto.setId(equipe.getId());
        dto.setNome(equipe.getNome());
        dto.setEsporte(equipe.getEsporte() != null ? equipe.getEsporte().getNome() : null);
        dto.setCampus(equipe.getCampus() != null ? equipe.getCampus().getNome() : null);
        dto.setCurso(equipe.getCurso() != null ? equipe.getCurso().getNome() : null);
        dto.setEvento(equipe.getEvento() != null ? equipe.getEvento().getNome() : null);

        List<Atleta> membros = atletaRepository.findByEquipeId(equipe.getId());

        dto.setAtletas(membros.stream().map(a -> {
            AtletaResumoDTO atletaDTO = new AtletaResumoDTO();
            atletaDTO.setId(a.getId());
            atletaDTO.setNomeCompleto(a.getNomeCompleto());
            atletaDTO.setMatricula(a.getMatricula());
            atletaDTO.setTelefone(a.getTelefone());
            atletaDTO.setTecnico(a.isTecnico());
            atletaDTO.setCurso(a.getCurso() != null ? a.getCurso().getNome() : null);
            return atletaDTO;
        }).collect(Collectors.toList()));

        String nomeTecnico = membros.stream()
                .filter(Atleta::isTecnico)
                .findFirst()
                .map(Atleta::getNomeCompleto)
                .orElse("Não definido");

        dto.setTecnico(nomeTecnico);

        // Mapear próximos jogos (exemplo, você precisará implementar a lógica real)
        List<Jogo> todosJogos = jogoRepository.findByEquipeId(equipe.getId());

        List<Jogo> jogosFinalizados = todosJogos.stream()
                .filter(Jogo::isFinalizado)
                .collect(Collectors.toList());

        int vitorias = 0;
        int derrotas = 0;

        for (Jogo jogo : jogosFinalizados) {
            if (jogo.getEquipe1().getId().equals(equipe.getId())) {
                if (jogo.getPlacarEquipe1() > jogo.getPlacarEquipe2()) {
                    vitorias++;
                } else if (jogo.getPlacarEquipe1() < jogo.getPlacarEquipe2()) {
                    derrotas++;
                }
            } else if (jogo.getEquipe2().getId().equals(equipe.getId())) {
                if (jogo.getPlacarEquipe2() > jogo.getPlacarEquipe1()) {
                    vitorias++;
                } else if (jogo.getPlacarEquipe2() < jogo.getPlacarEquipe1()) {
                    derrotas++;
                }
            }
        }

        // Preenche as estatísticas no DTO
        dto.setJogosDisputados(jogosFinalizados.size());
        dto.setVitorias(vitorias);
        dto.setDerrotas(derrotas);
        dto.setPontuacao(vitorias * 3); // 3 pontos por vitória

        // Mapeia próximos jogos (não finalizados e com data futura)
        List<Jogo> proximosJogos = todosJogos.stream()
                .filter(j -> !j.isFinalizado() && j.getDataHora().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        dto.setProximosJogos(proximosJogos.stream().map(j -> {
            JogoResumoDTO jogoDTO = new JogoResumoDTO();
            jogoDTO.setId(j.getId());
            jogoDTO.setEquipe1Nome(j.getEquipe1() != null ? j.getEquipe1().getNome() : "Bye");
            jogoDTO.setEquipe2Nome(j.getEquipe2() != null ? j.getEquipe2().getNome() : "Bye");
            jogoDTO.setStatus("Agendado");
            jogoDTO.setFase(j.getFase());
            return jogoDTO;
        }).collect(Collectors.toList()));

        return dto;
    }



}
