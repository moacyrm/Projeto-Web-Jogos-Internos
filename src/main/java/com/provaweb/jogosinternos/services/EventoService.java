package com.provaweb.jogosinternos.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.provaweb.jogosinternos.entities.Evento;
import com.provaweb.jogosinternos.entities.TipoEvento;
import com.provaweb.jogosinternos.repositories.EquipeRepository;
import com.provaweb.jogosinternos.repositories.EventoRepository;
import com.provaweb.jogosinternos.repositories.GrupoRepository;
import com.provaweb.jogosinternos.repositories.JogoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {
    private final EventoRepository eventoRepository;
    private final EquipeRepository equipeRepository;
    private final GrupoRepository grupoRepository;
    private final JogoRepository jogoRepository;

    public Evento criarEvento(Evento evento) {
        validarDatas(evento);
        return eventoRepository.save(evento);
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("evento não encontrado"));
    }

    public List<Evento> listarPorTipo(TipoEvento tipoEvento) {
        return eventoRepository.findByTipoEvento(tipoEvento);
    }

    public Evento atualizarEvento(Long id, Evento evento) {
        Evento existente = buscarPorId(id);
        existente.setNome(evento.getNome());
        existente.setTipoEvento(evento.getTipoEvento());
        existente.setDataInicio(evento.getDataInicio());
        existente.setDataFim(evento.getDataFim());
        return eventoRepository.save(existente);
    }

    @Transactional
    public void deletarEvento(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        equipeRepository.desvincularEquipesDeGruposPorEvento(id);
        grupoRepository.deleteByEventoId(id);
        jogoRepository.deleteByEventoId(id);
        eventoRepository.delete(evento);
    }

    private void validarDatas(Evento evento) {
        if (evento.getDataFim().isBefore(evento.getDataInicio())) {
            throw new RuntimeException("data final não pode ser anterior à data inicial");
        }
    }
}
