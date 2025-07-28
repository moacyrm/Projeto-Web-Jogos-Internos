package com.provaweb.jogosinternos.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.Jogo;
import com.provaweb.jogosinternos.repositories.JogoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JogoService {
    private final JogoRepository jogoRepository;
    private final GrupoService grupoService;

    public Jogo criarJogo(Jogo jogo) {
        validarJogo(jogo);
        jogo.setFinalizado(false);
        jogo.setWoEquipe1(false);
        jogo.setWoEquipe2(false);
        return jogoRepository.save(jogo);
    }

    public List<Jogo> listarTodos() {
        return jogoRepository.findAll();
    }

    public List<Jogo> listarPorGrupo(Long grupoId) {
        return jogoRepository.findByGrupoId(grupoId);
    }

    public List<Jogo> listarPorEvento(Long eventoId) {
        return jogoRepository.findByEventoId(eventoId);
    }

    public Jogo atualizarPlacar(Long id, int placarEquipe1, int placarEquipe2) {
        Jogo jogo = buscarPorId(id);

        if (jogo.isFinalizado()) {
            throw new RuntimeException("jogo já finalizado");
        }

        jogo.setPlacarEquipe1(placarEquipe1);
        jogo.setPlacarEquipe2(placarEquipe2);
        jogo.setFinalizado(true);
        return jogoRepository.save(jogo);
    }

    public Jogo registrarWO(Long id, Long equipeId) {
        Jogo jogo = buscarPorId(id);

        if (equipeId == null) {

            jogo.setWoEquipe1(true);
            jogo.setWoEquipe2(true);
        } else {

            if (equipeId.equals(jogo.getEquipe1().getId())) {
                jogo.setWoEquipe1(true);
            } else if (equipeId.equals(jogo.getEquipe2().getId())) {
                jogo.setWoEquipe2(true);
            } else {
                throw new RuntimeException("equipe não pertence a este jogo");
            }
        }

        jogo.setFinalizado(true);
        return jogoRepository.save(jogo);
    }

    public void cancelarJogo(Long id) {
        Jogo jogo = buscarPorId(id);
        if (jogo.isFinalizado()) {
            throw new RuntimeException("não é possível cancelar um jogo finalizado");
        }
        jogoRepository.delete(jogo);
    }

    private Jogo buscarPorId(Long id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("jogo não encontrado"));
    }

    private void validarJogo(Jogo jogo) {

        if (jogo.getEquipe1().equals(jogo.getEquipe2())) {
            throw new RuntimeException("uma equipe não pode jogar contra si mesma");
        }

        if (jogo.getDataHora().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("data do jogo não pode ser no passado");
        }

        if (!grupoService.equipesNoMesmoGrupo(
                jogo.getEquipe1().getId(),
                jogo.getEquipe2().getId())) {
            throw new RuntimeException("equipes devem pertencer ao mesmo grupo");
        }
    }

    public Jogo desfazerWO(Long id) {
        Jogo jogo = buscarPorId(id);

        if (!jogo.isFinalizado() || (!jogo.isWoEquipe1() && !jogo.isWoEquipe2())) {
            throw new RuntimeException("este jogo não possui WO para ser desfeito");
        }

        jogo.setWoEquipe1(false);
        jogo.setWoEquipe2(false);
        jogo.setFinalizado(false);
        return jogoRepository.save(jogo);
    }

    public List<String> listarFases() {
        return jogoRepository.findDistinctFases();
    }

}
