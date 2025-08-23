package com.provaweb.jogosinternos.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.provaweb.jogosinternos.entities.Admin;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.entities.Evento;
import com.provaweb.jogosinternos.entities.TipoEvento;
import com.provaweb.jogosinternos.repositories.AdminRepository;
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
    private final AdminRepository adminRepository;

    public Evento criarEvento(Evento evento, String matriculaAdmin) {
        validarDatas(evento);

        Admin admin = adminRepository.findByMatriculaIgnoreCase(matriculaAdmin)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));

        evento.setCriadoPor(admin);
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

    public List<Evento> listarEventosDisponiveis() {
        LocalDate hoje = LocalDate.now();
        return eventoRepository.findByDataInicioAfter(hoje);
    }

    @Transactional
    public void inscreverEquipe(Long eventoId, Long equipeId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));

        // Verificar se a equipe já está inscrita
        if (equipe.getEvento() != null && equipe.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("Equipe já inscrita neste evento");
        }

        // Verificar se já existe equipe do mesmo curso/esporte
        boolean existeInscricao = equipeRepository.existsByEventoIdAndCursoIdAndEsporteId(
                eventoId,
                equipe.getCurso().getId(),
                equipe.getEsporte().getId());

        if (existeInscricao) {
            throw new RuntimeException("Já existe uma equipe deste curso/esporte no evento");
        }

        equipe.setEvento(evento);
        equipeRepository.save(equipe);
    }
}
