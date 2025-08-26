package com.provaweb.jogosinternos.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.provaweb.jogosinternos.dto.AtletaDTO;
import com.provaweb.jogosinternos.dto.AtletaResumoDTO;
import com.provaweb.jogosinternos.dto.EquipeDTO;
import com.provaweb.jogosinternos.dto.EquipeDetalhesDTO;
import com.provaweb.jogosinternos.dto.JogoResumoDTO;
import com.provaweb.jogosinternos.dto.RankingDTO;
import com.provaweb.jogosinternos.entities.*;
import com.provaweb.jogosinternos.repositories.AtletaRepository;
import com.provaweb.jogosinternos.repositories.CampusRepository;
import com.provaweb.jogosinternos.repositories.CoordenadorRepository;
import com.provaweb.jogosinternos.repositories.EquipeRepository;
import com.provaweb.jogosinternos.repositories.EsporteRepository;
import com.provaweb.jogosinternos.repositories.EventoRepository;
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
    private final CoordenadorRepository coordenadorRepository;
    private final EventoRepository eventoRepository;
    private final CampusRepository campusRepository;

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
        return equipeRepository.findByEventoId(eventoId);
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

        dto.setJogosDisputados(jogosFinalizados.size());
        dto.setVitorias(vitorias);
        dto.setDerrotas(derrotas);
        dto.setPontuacao(vitorias * 3); // 3 pontos por vitória

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

    public List<EquipeDetalhesDTO> getEquipesDetalhesPorMatriculaCoordenador(String matriculaCoordenador) {
        var coordenador = coordenadorRepository.findById(matriculaCoordenador)
                .orElseThrow(() -> new RuntimeException("Coordenador não encontrado"));

        if (coordenador.getCurso() == null) {
            throw new RuntimeException("Coordenador não possui curso vinculado");
        }

        Long cursoId = coordenador.getCurso().getId();
        List<Equipe> equipes = equipeRepository.findByCursoId(cursoId);

        return equipes.stream()
                .map(this::mapToEquipeDetalhesDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Equipe criarEquipePorCoordenador(String matriculaCoordenador, Equipe novaEquipe, Long tecnicoId) {
        var coordenador = coordenadorRepository.findById(matriculaCoordenador)
                .orElseThrow(() -> new RuntimeException("Coordenador não encontrado"));

        if (coordenador.getCurso() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Coordenador não possui curso associado");
        }

        // força o curso do coordenador (evita criar para outro curso)
        novaEquipe.setCurso(coordenador.getCurso());

        // valida unicidade (proteção também feita pela constraint DB)
        if (equipeRepository.existsByEventoIdAndCursoIdAndEsporteId(
                novaEquipe.getEvento().getId(),
                novaEquipe.getCurso().getId(),
                novaEquipe.getEsporte().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Já existe equipe para esse evento/esporte/curso");
        }

        // salvar equipe (status DRAFT por padrão)
        Equipe salva = equipeRepository.save(novaEquipe);

        // se passou tecnicoId, valida e atribui
        if (tecnicoId != null) {
            Atleta tecnico = atletaService.buscarPorId(tecnicoId);
            // validar que atleta pertence ao mesmo curso (usar equipe.curso)
            Long cursoAtletaId = tecnico.getCurso() != null ? tecnico.getCurso().getId()
                    : (tecnico.getEquipe() != null && tecnico.getEquipe().getCurso() != null
                            ? tecnico.getEquipe().getCurso().getId()
                            : null);

            if (cursoAtletaId == null || !cursoAtletaId.equals(salva.getCurso().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Técnico não pertence ao curso desta equipe");
            }

            // atualiza atleta para essa equipe e marca tecnico
            atletaService.atualizarEquipe(tecnicoId, salva.getId(), tecnicoId);
        }

        return salva;
    }

    @Transactional
    public Equipe submitEquipe(Long equipeId, Long tecnicoId) {
        Equipe equipe = equipeRepository.findByIdWithTecnico(equipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrada"));

        if (equipe.getTecnico() == null || !equipe.getTecnico().getId().equals(tecnicoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o técnico pode submeter a equipe");
        }

        int qtd = atletaService.buscarPorEquipe(equipeId).size();

        Esporte esporte = esporteRepository.findById(equipe.getEsporte().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Esporte não encontrado"));

        if (qtd < esporte.getMinimoAtletas()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Equipe não possui atletas suficientes: mínimo " + esporte.getMinimoAtletas());
        }
        if (qtd > esporte.getMaximoAtletas()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Equipe possui mais atletas que o máximo: máximo " + esporte.getMaximoAtletas());
        }

        equipe.setStatus(EquipeStatus.ENVIADO);
        return equipeRepository.save(equipe);
    }

    public EquipeDetalhesDTO getEquipeDetalhesPorId(Long equipeId) {
        Equipe equipe = buscarPorId(equipeId);
        return mapToEquipeDetalhesDTO(equipe);
    }

    @Transactional(readOnly = true)
    public List<com.provaweb.jogosinternos.dto.EquipeDTO> listarEquipesPorMatriculaAtleta(String matricula,
            Long eventoId) {
        List<Equipe> equipes = equipeRepository.findByAtletaMatriculaAndEventoIdFetchAll(matricula, eventoId);
        return equipes.stream()
                .map(Equipe::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Equipe criarEquipeParaEvento(String nome, Long eventoId, Long esporteId, Long campusId,
            String matriculaTecnico) {
        Atleta tecnico = atletaRepository.findByMatriculaIgnoreCase(matriculaTecnico)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        boolean tecnicoEmOutraEquipe = equipeRepository.existsByTecnicoAndEvento(
                tecnico.getId(),
                eventoId);

        if (tecnicoEmOutraEquipe) {
            throw new RuntimeException("Este técnico já está associado a outra equipe neste evento");
        }

        Esporte esporte = esporteRepository.findById(esporteId)
                .orElseThrow(() -> new RuntimeException("Esporte não encontrado"));

        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new RuntimeException("Campus não encontrado"));
        // Verificar se já existe equipe para este técnico no evento
        boolean existeEquipe = equipeRepository.existsByEventoIdAndTecnicoMatricula(eventoId, matriculaTecnico);
        if (existeEquipe) {
            throw new RuntimeException("Você já possui uma equipe neste evento");
        }

        // Verificar se já existe equipe do mesmo curso/esporte no evento
        boolean existeInscricao = equipeRepository.existsByEventoIdAndCursoIdAndEsporteId(
                eventoId, tecnico.getCurso().getId(), esporteId);
        if (existeInscricao) {
            throw new RuntimeException("Já existe uma equipe deste curso/esporte no evento");
        }

        Equipe equipe = new Equipe();
        equipe.setNome(nome);
        equipe.setEvento(evento);
        equipe.setEsporte(esporte);
        equipe.setCurso(tecnico.getCurso());
        equipe.setCampus(campus);
        equipe.setTecnico(tecnico);
        equipe.setStatus(EquipeStatus.PROJETO);

        Equipe equipeSalva = equipeRepository.save(equipe);

        // Adicionar o técnico como atleta da equipe
        adicionarAtleta(equipeSalva.getId(), tecnico.getId(), matriculaTecnico);

        return equipeSalva;
    }

    public boolean verificarInscricao(Long eventoId, String matriculaTecnico) {
        return equipeRepository.existsByEventoIdAndTecnicoMatricula(eventoId, matriculaTecnico);
    }

    @Transactional
    public void adicionarAtleta(Long equipeId, Long atletaId, String matriculaTecnico) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));

        // Verificar se o usuário é o técnico da equipe
        if (!equipe.getTecnico().getMatricula().equals(matriculaTecnico)) {
            throw new RuntimeException("Apenas o técnico pode adicionar atletas");
        }

        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));

        // Verificar se o atleta pertence ao mesmo curso
        if (!atleta.getCurso().getId().equals(equipe.getCurso().getId())) {
            throw new RuntimeException("O atleta deve ser do mesmo curso");
        }

        // Verificar se o atleta já está em outra equipe no mesmo evento
        boolean jaInscrito = equipeRepository.existsByEventoIdAndAtletasMatricula(
                equipe.getEvento().getId(), atleta.getMatricula());
        if (jaInscrito) {
            throw new RuntimeException("Este atleta já está em outra equipe no evento");
        }

        Esporte esporte = equipe.getEsporte();
        int quantidadeAtletas = equipe.getAtletas().size();
        if (quantidadeAtletas >= esporte.getMaximoAtletas()) {
            throw new RuntimeException("Limite máximo de atletas atingido para este esporte");
        }

        equipe.getAtletas().add(atleta);
        atleta.setEquipe(equipe);

        equipeRepository.save(equipe);
        atletaRepository.save(atleta);
    }

    public List<AtletaDTO> listarAtletasDisponiveis(Long eventoId, String matriculaTecnico) {
        Atleta tecnico = atletaRepository.findByMatriculaIgnoreCase(matriculaTecnico)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));

        List<Atleta> atletasDisponiveis = atletaRepository.findByCursoIdAndNotInEvento(
                tecnico.getCurso().getId(), eventoId);

        return atletasDisponiveis.stream()
                .map(atleta -> new AtletaDTO(atleta.getId(), atleta.getNomeCompleto(), atleta.getMatricula()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RankingDTO> calcularRankingPorEvento(Long eventoId) {
        List<Equipe> equipes = equipeRepository.findByEventoId(eventoId);
        List<Jogo> jogos = jogoRepository.findByEventoId(eventoId);

        Map<String, RankingDTO> rankingMap = new HashMap<>();

        // Inicializar ranking para todas as equipes
        for (Equipe equipe : equipes) {
            if (!equipe.getNome().equalsIgnoreCase("Bye")) {
                rankingMap.put(equipe.getNome(), new RankingDTO(equipe.getNome(), 0, 0, 0));
            }
        }

        // Processar todos os jogos
        for (Jogo jogo : jogos) {
            if (!jogo.isFinalizado())
                continue;

            if (jogo.getEquipe1().getNome().equalsIgnoreCase("Bye") ||
                    jogo.getEquipe2().getNome().equalsIgnoreCase("Bye")) {
                continue;
            }

            RankingDTO equipe1 = rankingMap.get(jogo.getEquipe1().getNome());
            RankingDTO equipe2 = rankingMap.get(jogo.getEquipe2().getNome());

            if (equipe1 != null && equipe2 != null) {
                // Atualizar estatísticas
                equipe1.setJogos(equipe1.getJogos() + 1);
                equipe2.setJogos(equipe2.getJogos() + 1);

                equipe1.setSaldoGols(equipe1.getSaldoGols() + jogo.getPlacarEquipe1() - jogo.getPlacarEquipe2());
                equipe2.setSaldoGols(equipe2.getSaldoGols() + jogo.getPlacarEquipe2() - jogo.getPlacarEquipe1());

                // Determinar pontos
                if (jogo.getPlacarEquipe1() > jogo.getPlacarEquipe2()) {
                    equipe1.setPontos(equipe1.getPontos() + 3);
                } else if (jogo.getPlacarEquipe1() < jogo.getPlacarEquipe2()) {
                    equipe2.setPontos(equipe2.getPontos() + 3);
                } else {
                    equipe1.setPontos(equipe1.getPontos() + 1);
                    equipe2.setPontos(equipe2.getPontos() + 1);
                }
            }
        }

        // Converter para lista e ordenar por pontos (e saldo de gols em caso de empate)
        List<RankingDTO> ranking = new ArrayList<>(rankingMap.values());
        return ranking.stream()
                .sorted(Comparator.comparingInt(RankingDTO::getPontos).reversed()
                        .thenComparingInt(RankingDTO::getSaldoGols).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public void removerTodosAtletasDasEquipes() {
        // Primeiro: atualiza todos os atletas para remover a referência da equipe
        atletaRepository.removerEquipeDeTodosAtletas();

        // Segundo: limpa a lista de atletas de todas as equipes
        List<Equipe> equipes = equipeRepository.findAll();
        for (Equipe equipe : equipes) {
            equipe.getAtletas().clear();
        }
        equipeRepository.saveAll(equipes);
    }

    // EquipeService.java
    @Transactional
    public Equipe associarTecnico(Long equipeId, String matriculaTecnico) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));

        Atleta tecnico = atletaRepository.findByMatriculaIgnoreCase(matriculaTecnico)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));

        // Verificar se o técnico já está em outra equipe neste evento
        boolean tecnicoEmOutraEquipe = equipeRepository.existsByEventoIdAndTecnicoMatricula(
                equipe.getEvento().getId(), matriculaTecnico);

        if (tecnicoEmOutraEquipe) {
            throw new RuntimeException("Este técnico já está associado a outra equipe neste evento");
        }

        // Verificar se o técnico pertence ao mesmo curso da equipe
        if (!tecnico.getCurso().getId().equals(equipe.getCurso().getId())) {
            throw new RuntimeException("O técnico deve ser do mesmo curso da equipe");
        }

        equipe.setTecnico(tecnico);
        return equipeRepository.save(equipe);
    }

    public List<Equipe> buscarPorCursoEsporteEvento(Long cursoId, Long esporteId, Long eventoId) {
        return equipeRepository.findByCursoIdAndEsporteIdAndEventoId(cursoId, esporteId, eventoId);
    }

    public List<EquipeDTO> buscarEquipesPorCursoEsporteEvento(Long cursoId, Long esporteId, Long eventoId) {
        List<Equipe> equipes = equipeRepository.findByCursoIdAndEsporteIdAndEventoId(cursoId, esporteId, eventoId);

        return equipes.stream().map(equipe -> {
            EquipeDTO dto = new EquipeDTO();
            dto.setId(equipe.getId());
            dto.setNome(equipe.getNome());
            dto.setEvento(equipe.getEvento() != null ? equipe.getEvento().getNome() : null);
            dto.setEsporte(equipe.getEsporte() != null ? equipe.getEsporte().getNome() : null);
            dto.setCurso(equipe.getCurso() != null ? equipe.getCurso().getNome() : null);
            long atletasCount = atletaRepository.countByEquipeId(equipe.getId());
            dto.setAtletasCount(atletasCount);

            return dto;
        }).collect(Collectors.toList());
    }
}
