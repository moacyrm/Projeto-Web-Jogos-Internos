package com.provaweb.jogosinternos.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.*;
import com.provaweb.jogosinternos.repositories.EquipeRepository;
import com.provaweb.jogosinternos.repositories.EsporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipeService {
    private final EquipeRepository equipeRepository;
    private final AtletaService atletaService;
    private final EsporteRepository esporteRepository;

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

}
