package com.provaweb.jogosinternos.services;

import com.provaweb.jogosinternos.dto.ChaveDTO;
import com.provaweb.jogosinternos.entities.*;
import com.provaweb.jogosinternos.repositories.ChaveRepository;
import com.provaweb.jogosinternos.repositories.EventoRepository;
import com.provaweb.jogosinternos.repositories.GrupoRepository;
import com.provaweb.jogosinternos.repositories.JogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChaveEliminatoriaService {

    private final EventoRepository eventoRepository;
    private final GrupoRepository grupoRepository;
    private final JogoRepository jogoRepository;
    private final ChaveRepository chaveRepository;
    private final EquipeService equipeService;

    @Transactional
    public ChaveDTO gerarChaveEliminatoria(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        // Verifica se já existe chave eliminatória para o evento
        if (chaveRepository.existsByEventoId(eventoId)) {
            throw new RuntimeException("Eliminatória já foi gerada para esse evento");
        }

        ChaveEliminatoria chave = new ChaveEliminatoria();
        chave.setNome("Chave Principal");
        chave.setTipo(ChaveEliminatoria.TipoChave.ELIMINATORIA_SIMPLES);
        chave.setEvento(evento);
        chave = chaveRepository.saveAndFlush(chave);

        // Busca os grupos do evento
        List<Grupo> grupos = grupoRepository.findByEventoId(eventoId);

        // Obtém os classificados (top 2 de cada grupo)
        List<Equipe> classificados = obterClassificados(grupos);

        // Preenche com equipes "Bye" para completar potência de 2
        int totalEquipes = classificados.size();
        int totalCompleto = proximaPotenciaDe2(totalEquipes);
        while (classificados.size() < totalCompleto) {
            Equipe bye = equipeService.buscarOuCriarEquipeBye(evento, classificados.get(0).getEsporte(),
                    classificados.get(0).getCurso(), classificados.get(0).getCampus());
            classificados.add(bye);
        }

        // Embaralha para evitar confrontos repetidos
        Collections.shuffle(classificados);

        // Divide em dois grupos para confrontos (primeiros vs segundos)
        List<Equipe> primeiros = new ArrayList<>();
        List<Equipe> segundos = new ArrayList<>();
        for (int i = 0; i < classificados.size(); i++) {
            if (i % 2 == 0) {
                primeiros.add(classificados.get(i));
            } else {
                segundos.add(classificados.get(i));
            }
        }

        Collections.shuffle(primeiros);
        Collections.shuffle(segundos);

        // Define a fase da eliminatória baseado no número de equipes
        String fase = definirFase(classificados.size());

        // Cria os jogos da chave eliminatória
        List<Jogo> jogos = new ArrayList<>();
        for (int i = 0; i < primeiros.size(); i++) {
            Equipe e1 = primeiros.get(i);
            Equipe e2 = segundos.get(i);

            Jogo jogo = new Jogo();
            jogo.setEquipe1(e1);
            jogo.setEquipe2(e2);
            jogo.setEvento(evento);
            jogo.setFase(fase);
            jogo.setFinalizado(false);
            jogo.setDataHora(calcularDataProximoJogo(evento));
            jogo.setChaveEliminatoria(chave);

            // Se tiver equipe "Bye", finaliza automaticamente o jogo dando vitória para o
            // adversário
            if (e1.getNome().equalsIgnoreCase("Bye") || e2.getNome().equalsIgnoreCase("Bye")) {
                jogo.setFinalizado(true);
                jogo.setPlacarEquipe1(e1.getNome().equalsIgnoreCase("Bye") ? 0 : 1);
                jogo.setPlacarEquipe2(e2.getNome().equalsIgnoreCase("Bye") ? 0 : 1);
            }

            jogos.add(jogo);
        }

        // Salva os jogos e retorna o DTO
        chave.setJogos(jogoRepository.saveAll(jogos));
        return chave.toDTO(jogos);
    }

    private List<Equipe> obterClassificados(List<Grupo> grupos) {
        List<Equipe> classificados = new ArrayList<>();

        for (Grupo grupo : grupos) {// conta quantos jogos ainda nao foram finalizados
            long pendentes = grupo.getJogos().stream().filter(j -> !j.isFinalizado()).count();
            if (pendentes > 0) {
                throw new RuntimeException("Grupo " + grupo.getNome() + " possui jogos pendentes.");
            }

            if (grupo.getEquipes().size() < 2) {
                throw new RuntimeException("Grupo " + grupo.getNome() + " não possui equipes suficientes");
            }

            List<Equipe> ordenadas = classificar(grupo);
            classificados.add(ordenadas.get(0));
            classificados.add(ordenadas.get(1));
        }

        return classificados;
    }

    private List<Equipe> classificar(Grupo grupo) {
        List<Equipe> equipes = grupo.getEquipes();// pega a lista de equipes do grupo

        // armazena pontos, gols e gols recebidos
        Map<Long, Integer> pontos = new HashMap<>();
        Map<Long, Integer> saldo = new HashMap<>();
        Map<Long, Integer> feitos = new HashMap<>();

        for (Equipe e : equipes) {// inicializa os mapas com 0
            pontos.put(e.getId(), 0);
            saldo.put(e.getId(), 0);
            feitos.put(e.getId(), 0);
        }

        for (Jogo j : grupo.getJogos()) {// percorre todos os jogos do grupo
            if (!j.isFinalizado())// ignora os nao finalizados
                continue;

            Long id1 = j.getEquipe1().getId();
            Long id2 = j.getEquipe2().getId();

            if (!equipes.contains(j.getEquipe1()) || !equipes.contains(j.getEquipe2()))
                continue;

            // atualiza pontos para equipe 1
            pontos.put(id1, pontos.get(id1) + (j.getPlacarEquipe1() > j.getPlacarEquipe2() ? 3
                    : j.getPlacarEquipe1() == j.getPlacarEquipe2() ? 1 : 0));

            // atualiza pontos para equipe 2
            pontos.put(id2, pontos.get(id2) + (j.getPlacarEquipe2() > j.getPlacarEquipe1() ? 3
                    : j.getPlacarEquipe1() == j.getPlacarEquipe2() ? 1 : 0));

            // atualiza saldo de gols e gols recebidos
            saldo.put(id1, saldo.get(id1) + j.getPlacarEquipe1() - j.getPlacarEquipe2());
            saldo.put(id2, saldo.get(id2) + j.getPlacarEquipe2() - j.getPlacarEquipe1());

            // atualiza gols feitos
            feitos.put(id1, feitos.get(id1) + j.getPlacarEquipe1());
            feitos.put(id2, feitos.get(id2) + j.getPlacarEquipe2());
        }

        return equipes.stream()// ordena as equipes por pontos, saldo de gols e gols feitos respectivsamente
                .sorted(Comparator
                        .comparing((Equipe e) -> pontos.get(e.getId())).reversed()
                        .thenComparing(e -> saldo.get(e.getId()), Comparator.reverseOrder())
                        .thenComparing(e -> feitos.get(e.getId()), Comparator.reverseOrder()))
                .toList();
    }

    private String definirFase(int total) {
        return switch (total) {
            case 2 -> "Final";
            case 4 -> "Semifinal";
            case 8 -> "Quartas de Final";
            case 16 -> "Oitavas de Final";
            default -> "Eliminatória";
        };
    }

    private LocalDateTime calcularDataProximoJogo(Evento evento) {
        return evento.getDataInicio().atTime(10, 0);
    }

    public ChaveDTO buscarChavePorEvento(Long eventoId) {
        ChaveEliminatoria chave = chaveRepository.findByEventoId(eventoId)
                .orElseThrow(() -> new RuntimeException("Chave não encontrada para o evento ID: " + eventoId));
        return chave.toDTO(chave.getJogos());
    }

    private int proximaPotenciaDe2(int n) {
        int potencia = 1;
        while (potencia < n) {
            potencia <<= 1; // multiplica por 2
        }
        return potencia;
    }

}
