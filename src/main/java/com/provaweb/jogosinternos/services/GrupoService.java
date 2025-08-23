// GrupoService.java
package com.provaweb.jogosinternos.services;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.provaweb.jogosinternos.dto.ClassificacaoDTO;
import com.provaweb.jogosinternos.dto.ConfrontoDTO;
import com.provaweb.jogosinternos.dto.GrupoDTO;
import com.provaweb.jogosinternos.entities.*;
import com.provaweb.jogosinternos.repositories.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final EquipeRepository equipeRepository;
    private final GrupoRepository grupoRepository;
    private final EventoRepository eventoRepository;
    private final JogoRepository jogoRepository;
    private final AtletaRepository atletaRepository;
    private final CursoRepository cursoRepository;

    @Transactional
    public List<Grupo> sortearGrupo(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (!grupoRepository.findByEventoId(eventoId).isEmpty()) {
            throw new RuntimeException("Esse evento já possui grupos. Cancele e sorteie novamente.");
        }

        List<Equipe> equipes = equipeRepository.findByEventoId(eventoId);
        Collections.shuffle(equipes);

        // validações (mantenha suas checagens atuais)
        for (Equipe equipe : equipes) {
            if (equipe.getCurso() == null) {
                throw new RuntimeException(
                        "Equipe '" + equipe.getNome() + "' não tem curso associado. " +
                                "Por favor, associe um curso válido à equipe antes do sorteio.");
            }

            Curso curso = cursoRepository.findById(equipe.getCurso().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Curso associado à equipe '" + equipe.getNome() + "' não encontrado no sistema"));

            if (curso.getTipo() == null) {
                throw new RuntimeException(
                        "Curso '" + curso.getNome() + "' não tem tipo definido. " +
                                "Defina o tipo do curso no cadastro antes do sorteio.");
            }

            if (!curso.getTipo().equals(evento.getTipoEvento())) {
                throw new RuntimeException(
                        "Tipo do curso '" + curso.getNome() + "' (" + curso.getTipo() + ") " +
                                "não é compatível com o tipo do evento (" + evento.getTipoEvento() + ")");
            }

            long numAtletas = atletaRepository.countByEquipeId(equipe.getId());
            if (numAtletas < equipe.getEsporte().getMinimoAtletas()) {
                throw new RuntimeException("Equipe " + equipe.getNome() + " não tem atletas suficientes");
            }
        }

        if (equipes.size() < 3) {
            throw new RuntimeException("Número mínimo de equipes não atingido");
        }

        List<Integer> tamanhosGrupos = definirDistribuicaoGrupos(equipes.size());
        List<Grupo> gruposCriados = new ArrayList<>();

        // 1) criar os objetos Grupo (sem equipes ainda)
        for (int i = 0; i < tamanhosGrupos.size(); i++) {
            Grupo grupo = new Grupo();
            grupo.setNome("Grupo " + (i + 1));
            grupo.setEvento(evento);
            grupo.setEquipes(new ArrayList<>());
            gruposCriados.add(grupo);
        }

        // 2) persistir os grupos primeiro para garantir ids
        gruposCriados = grupoRepository.saveAll(gruposCriados);

        // 3) distribuir equipes para os grupos salvos
        int equipeIndex = 0;
        for (int i = 0; i < gruposCriados.size(); i++) {
            Grupo grupoSalvo = gruposCriados.get(i);
            int tamanho = tamanhosGrupos.get(i);
            for (int j = 0; j < tamanho; j++) {
                Equipe equipe = equipes.get(equipeIndex++);
                // associa a equipe ao grupo salvo
                equipe.setGrupo(grupoSalvo);
                grupoSalvo.getEquipes().add(equipe);
            }
        }

        // 4) salvar as equipes já com o grupo definido
        equipeRepository.saveAll(equipes);

        // 5) (opcional) salvar os grupos novamente para sincronizar o lado
        // "one-to-many"
        grupoRepository.saveAll(gruposCriados);

        // 6) criar confrontos: recarregando grupos com equipes para evitar problemas de
        // detach/proxy
        for (Grupo g : gruposCriados) {
            Grupo grupoComEquipes = grupoRepository.findByIdWithEquipes(g.getId());
            criarConfronto(grupoComEquipes);
        }

        // retornar grupos do evento (com estado atualizado)
        return grupoRepository.findByEventoId(eventoId);
    }

    @Transactional
    public List<Jogo> criarConfronto(Grupo grupo) {// gera todos os jogos possiveis entre as equipes de um grupo
        List<Equipe> equipes = grupo.getEquipes();
        List<Jogo> jogos = new ArrayList<>();
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);

        for (int i = 0; i < equipes.size(); i++) {
            for (int j = i + 1; j < equipes.size(); j++) {
                Jogo jogo = new Jogo();
                jogo.setEquipe1(equipes.get(i));
                jogo.setEquipe2(equipes.get(j));
                jogo.setGrupo(grupo);
                jogo.setEvento(grupo.getEvento());
                jogo.setFinalizado(false);
                jogo.setDataHora(inicio.plusHours(jogos.size()));
                jogos.add(jogo);
            }
        }

        return jogoRepository.saveAll(jogos);
    }

    public List<Grupo> listarTodos() {
        return grupoRepository.findAll();
    }

    public Grupo buscarGrupoComEquipes(Long grupoId) {
        return grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));
    }

    public boolean equipesNoMesmoGrupo(Long equipe1Id, Long equipe2Id) {
        Long grupo1 = equipeRepository.findById(equipe1Id)
                .orElseThrow(() -> new RuntimeException("Equipe 1 não encontrada"))
                .getGrupo().getId();

        Long grupo2 = equipeRepository.findById(equipe2Id)
                .orElseThrow(() -> new RuntimeException("Equipe 2 não encontrada"))
                .getGrupo().getId();

        return grupo1.equals(grupo2);
    }

    public void validarSeJogosFinalizados(Long eventoId) {
        List<Jogo> jogosNaoFinalizados = jogoRepository.findByEventoIdAndFinalizadoFalse(eventoId);
        if (!jogosNaoFinalizados.isEmpty()) {
            throw new RuntimeException("Existem jogos da fase de grupos ainda não finalizados.");
        }
    }

    public void deletarGruposDoEvento(Long eventoId) {
        equipeRepository.desvincularEquipesDeGruposPorEvento(eventoId);
        jogoRepository.deleteByGrupoEventoId(eventoId);
        grupoRepository.deleteByEventoId(eventoId);
    }

    @Transactional
    public void limparGruposDoEvento(Long eventoId) {
        jogoRepository.deleteByGrupoEventoId(eventoId);
        equipeRepository.desvincularEquipesDeGruposPorEvento(eventoId);
        grupoRepository.deleteByEventoId(eventoId);
    }

    private List<Integer> definirDistribuicaoGrupos(int totalEquipes) {
        List<Integer> distribuicao = new ArrayList<>();// armazena o tamanho de cada grupo
        while (totalEquipes >= 3) {// enquanto for possivel formar grupo com pelo menos 3 equipes
            if (totalEquipes == 4 || totalEquipes == 5) {// se sobrar 4 ou 5
                distribuicao.add(totalEquipes);// cria com 4 ou 5
                return distribuicao;
            }
            if (totalEquipes % 3 == 1 && totalEquipes >= 4) {// se a divisao for 1 e tem mais de 4
                distribuicao.add(4);// cria com 4
                totalEquipes -= 4;// reduz o total
            } else if (totalEquipes % 3 == 2 && totalEquipes >= 5) {// se a divisao for 2 e tem mais de 5
                distribuicao.add(5);// reduz o total
                totalEquipes -= 5;
            } else {
                distribuicao.add(3);
                totalEquipes -= 3;
            }
        }

        if (totalEquipes > 0) {
            throw new RuntimeException("Não foi possível sortear os grupos corretamente.");
        }
        return distribuicao;
    }

    // dtos
    public GrupoDTO montarGrupoDTOComConfrontos(Grupo grupo) {
        List<Jogo> jogos = jogoRepository.findByGrupoId(grupo.getId());
        GrupoDTO dto = new GrupoDTO();
        dto.setNomeGrupo(grupo.getNome());
        List<ConfrontoDTO> confrontos = jogos.stream().map(jogo -> {
            ConfrontoDTO c = new ConfrontoDTO();
            c.setEquipe1(jogo.getEquipe1().getNome());
            c.setEquipe2(jogo.getEquipe2().getNome());
            c.setDataHora(jogo.getDataHora());
            return c;
        }).collect(Collectors.toList());
        dto.setConfrontos(confrontos);
        return dto;
    }

    public List<Grupo> listarTodosComJogos(Long eventoId) {
        return grupoRepository.findByEventoIdWithJogos(eventoId);
    }

    public List<ClassificacaoDTO> getClassificacaoFinal(Long eventoId) {

        List<Jogo> finais = jogoRepository.findByEventoIdAndFase(eventoId, "Final");
        List<Jogo> semifinais = jogoRepository.findByEventoIdAndFaseStartingWith(eventoId, "Semifinal");

        if (finais.size() != 1 || semifinais.size() != 2) {
            throw new RuntimeException("Jogos das finais incompletos ou ausentes.");
        }

        Jogo finalJogo = finais.get(0);// pega o jogo da final
        Equipe campeao = finalJogo.getPlacarEquipe1() > finalJogo.getPlacarEquipe2()// determina o campeao com base no
                                                                                    // placar
                ? finalJogo.getEquipe1()
                : finalJogo.getEquipe2();

        Equipe vice = finalJogo.getPlacarEquipe1() < finalJogo.getPlacarEquipe2()
                ? finalJogo.getEquipe1()
                : finalJogo.getEquipe2();

        List<Equipe> semifinalistas = new ArrayList<>();// lista com todos os semifinalistas
        for (Jogo semi : semifinais) {
            semifinalistas.add(semi.getEquipe1());
            semifinalistas.add(semi.getEquipe2());
        }

        semifinalistas.remove(campeao);
        semifinalistas.remove(vice);
        Equipe terceiro = semifinalistas.get(0);

        List<ClassificacaoDTO> podio = new ArrayList<>();
        podio.add(new ClassificacaoDTO(campeao, 0, 0, 0, 0));
        podio.add(new ClassificacaoDTO(vice, 0, 0, 0, 0));
        podio.add(new ClassificacaoDTO(terceiro, 0, 0, 0, 0));
        return podio;
    }

}
