package com.provaweb.jogosinternos.services;

import com.provaweb.jogosinternos.dto.ChaveDTO;
import com.provaweb.jogosinternos.dto.ClassificacaoDTO;
import com.provaweb.jogosinternos.dto.ClassificacaoPorEventoDTO;
import com.provaweb.jogosinternos.dto.ClassificacaoSimplesDTO;
import com.provaweb.jogosinternos.dto.EliminatoriaDTO;
import com.provaweb.jogosinternos.dto.JogoDTO;
import com.provaweb.jogosinternos.entities.*;
import com.provaweb.jogosinternos.entities.ChaveEliminatoria.TipoChave;
import com.provaweb.jogosinternos.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EliminatoriaService {

    // nome padrao para equipe que recebe bye
    private static final String BYE_EQUIPE_NOME = "Bye";

    private final EventoRepository eventoRepository;
    private final GrupoRepository grupoRepository;
    private final JogoRepository jogoRepository;
    private final EquipeService equipeService;
    private final ChaveRepository chaveRepository;

    @Transactional
    public ChaveEliminatoria gerarEliminatorias(Long eventoId) {
        Evento evento = validarEvento(eventoId); // valida se o evento existe
        validarChaveExistente(eventoId);// verifica se ja tem chave eliminatoria
        List<Grupo> grupos = validarEGerarClassificacao(eventoId);// busca os grupos

        Map<String, List<Equipe>> equipesClassificadas = classificarEquipesPorGrupo(grupos);// pega os 2 melhores

        List<Equipe> equipesChaveadas = distribuirEquipesNaChave(// embaralha e distribui as equipes
                equipesClassificadas.get("primeiros"),
                equipesClassificadas.get("segundos"));

        return criarEstruturaEliminatoria(evento, equipesChaveadas);
    }

    private Evento validarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
    }

    private void validarChaveExistente(Long eventoId) {
        if (chaveRepository.existsByEventoId(eventoId)) {
            throw new RuntimeException("Já existe uma chave eliminatória para este evento");
        }
    }

    private List<Grupo> validarEGerarClassificacao(Long eventoId) {
        List<Grupo> grupos = grupoRepository.findByEventoId(eventoId);

        if (grupos.isEmpty()) {
            throw new RuntimeException("Evento não possui grupos cadastrados");
        }

        long jogosPendentes = jogoRepository.countByEventoIdAndFinalizadoFalseAndFaseIsNull(eventoId);
        if (jogosPendentes > 0) {
            throw new RuntimeException("Existem " + jogosPendentes + " jogos da fase de grupos não finalizados");
        }

        return grupos;
    }

    private Map<String, List<Equipe>> classificarEquipesPorGrupo(List<Grupo> grupos) {// pra cada grupo, classifica as
                                                                                      // equipes e separa os primeiros
                                                                                      // dos segundos colocados
        Map<String, List<Equipe>> resultado = new HashMap<>();
        List<Equipe> primeiros = new ArrayList<>();
        List<Equipe> segundos = new ArrayList<>();

        for (Grupo grupo : grupos) {
            validarGrupo(grupo);
            List<Equipe> classificacao = classificarGrupo(grupo);
            primeiros.add(classificacao.get(0));
            segundos.add(classificacao.get(1));
        }

        resultado.put("primeiros", primeiros);
        resultado.put("segundos", segundos);
        return resultado;
    }

    private void validarGrupo(Grupo grupo) {
        if (grupo.getEquipes().size() < 2) {
            throw new RuntimeException("Grupo " + grupo.getNome() + " não tem equipes suficientes");
        }
    }

    private List<Equipe> classificarGrupo(Grupo grupo) {// ordena as equipes do grupo por pontos
        List<Jogo> jogos = jogoRepository.findByGrupoId(grupo.getId());
        List<ClassificacaoDTO> classificacao = calcularClassificacao(grupo.getEquipes(), jogos);

        return classificacao.stream() // retorna as duas melhores
                .limit(2)
                .map(ClassificacaoDTO::getEquipe)
                .collect(Collectors.toList());
    }

    private List<ClassificacaoDTO> calcularClassificacao(List<Equipe> equipes, List<Jogo> jogos) {// calcula a
                                                                                                  // classificação das
                                                                                                  // equipes com base
                                                                                                  // nos jogos
        List<ClassificacaoDTO> classificacao = equipes.stream()
                .map(e -> new ClassificacaoDTO(e, 0, 0, 0, 0))
                .collect(Collectors.toList());

        for (Jogo jogo : jogos) {
            if (!jogo.isFinalizado())
                continue;

            ClassificacaoDTO dto1 = encontrarEquipeNaClassificacao(classificacao, jogo.getEquipe1());
            ClassificacaoDTO dto2 = encontrarEquipeNaClassificacao(classificacao, jogo.getEquipe2());

            atualizarClassificacao(jogo, dto1, dto2);
        }

        return ordenarClassificacao(classificacao);
    }

    // busca dto classificacao de uma equipe
    private ClassificacaoDTO encontrarEquipeNaClassificacao(List<ClassificacaoDTO> classificacao, Equipe equipe) {
        return classificacao.stream()
                .filter(dto -> dto.getEquipe().getId().equals(equipe.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada na classificação"));
    }

    private void atualizarClassificacao(Jogo jogo, ClassificacaoDTO dto1, ClassificacaoDTO dto2) {// atualiza apos cada
                                                                                                  // jogo
        dto1.setPontosPro(dto1.getPontosPro() + jogo.getPlacarEquipe1());
        dto1.setPontosContra(dto1.getPontosContra() + jogo.getPlacarEquipe2());
        dto2.setPontosPro(dto2.getPontosPro() + jogo.getPlacarEquipe2());
        dto2.setPontosContra(dto2.getPontosContra() + jogo.getPlacarEquipe1());

        dto1.setSaldo(dto1.getPontosPro() - dto1.getPontosContra());
        dto2.setSaldo(dto2.getPontosPro() - dto2.getPontosContra());

        if (jogo.getPlacarEquipe1() > jogo.getPlacarEquipe2()) {
            dto1.setPontos(dto1.getPontos() + 3);
        } else if (jogo.getPlacarEquipe1() < jogo.getPlacarEquipe2()) {
            dto2.setPontos(dto2.getPontos() + 3);
        } else {
            dto1.setPontos(dto1.getPontos() + 1);
            dto2.setPontos(dto2.getPontos() + 1);
        }
    }

    // ordena por pontos, saldo e gols feitos
    private List<ClassificacaoDTO> ordenarClassificacao(List<ClassificacaoDTO> classificacao) {
        return classificacao.stream()
                .sorted(Comparator.comparingInt(ClassificacaoDTO::getPontos).reversed()
                        .thenComparingInt(ClassificacaoDTO::getSaldo).reversed()
                        .thenComparingInt(ClassificacaoDTO::getPontosPro).reversed())
                .collect(Collectors.toList());
    }

    // distribui os classificados na chave eliminatória embaralhando e evitando
    // confrontos entre equipes do mesmo grupo
    private List<Equipe> distribuirEquipesNaChave(List<Equipe> primeiros, List<Equipe> segundos) {
        validarQuantidadeEquipes(primeiros, segundos);
        Collections.shuffle(primeiros);
        Collections.shuffle(segundos);

        List<Equipe> segundosRearranjados = evitarConfrontosMesmoGrupo(primeiros, new ArrayList<>(segundos));
        return intercalarEquipes(primeiros, segundosRearranjados);
    }

    // valida se o número de equipes é igual entre os dois grupos
    private void validarQuantidadeEquipes(List<Equipe> primeiros, List<Equipe> segundos) {
        if (primeiros.size() != segundos.size()) {
            throw new RuntimeException("Número desigual de equipes entre primeiros e segundos colocados");
        }
    }

    // evitar confrontos mesmo grupo
    private List<Equipe> evitarConfrontosMesmoGrupo(List<Equipe> primeiros, List<Equipe> segundos) {
        for (int i = 0; i < primeiros.size(); i++) { // percorre todas as equipes em 1
            Equipe primeiro = primeiros.get(i);// pega a equipe da posição i dos primeiros
            boolean encontrouPar = false;// caso ache um segundo colocado de grupo diferente

            for (int j = i; j < segundos.size(); j++) {// percorre os segundos a partir da posição i
                Equipe segundo = segundos.get(j);// pega a equipe da posição j dos segundos

                if (!pertencemAoMesmoGrupo(primeiro, segundo)) {// verifica se sao do mesmo grupo
                    if (j != i)// se j for diferente de i, troca as posicoes
                        Collections.swap(segundos, i, j);// troca i e j na lista de segundos
                    encontrouPar = true;// marca que encontrou equipe valida
                    break;
                }
            }

            if (!encontrouPar) {
                System.out.println("Aviso: confronto entre equipes do mesmo grupo será necessário.");

            }
        }
        return segundos;
    }

    // verifica se sao do mesmo grupo
    private boolean pertencemAoMesmoGrupo(Equipe e1, Equipe e2) {
        return e1.getGrupo() != null && e2.getGrupo() != null &&
                e1.getGrupo().getId().equals(e2.getGrupo().getId());
    }

    // intercala as equipes dos dois grupos
    private List<Equipe> intercalarEquipes(List<Equipe> primeiros, List<Equipe> segundos) {
        List<Equipe> chaveada = new ArrayList<>();
        for (int i = 0; i < primeiros.size(); i++) {
            chaveada.add(primeiros.get(i));
            chaveada.add(segundos.get(i));
        }
        return chaveada;
    }

    // cria estrutura da chave eliminatória e os jogos da primeira fase
    private ChaveEliminatoria criarEstruturaEliminatoria(Evento evento, List<Equipe> equipes) {
        ChaveEliminatoria chave = criarChaveEliminatoria(evento);

        List<Jogo> jogos = criarJogosEliminatorios(equipes, evento, chave);
        jogoRepository.saveAll(jogos);
        chave.setJogos(jogos);
        return chave;
    }

    // cria e salva a chave eliminatória
    private ChaveEliminatoria criarChaveEliminatoria(Evento evento) {
        ChaveEliminatoria chave = new ChaveEliminatoria();
        chave.setEvento(evento);
        chave.setNome("Chave Eliminatória - " + evento.getNome());
        chave.setTipo(TipoChave.ELIMINATORIA_SIMPLES);
        return chaveRepository.save(chave);
    }

    private List<Jogo> criarJogosEliminatorios(List<Equipe> equipes, Evento evento, ChaveEliminatoria chave) {
        ajustarQuantidadeEquipes(equipes, evento);// garante que o número de equipes é par, se necessario cria bye
        List<Jogo> jogos = new ArrayList<>();// armazena os jogos eliminatorios criados
        String fase = determinarFase(equipes.size());// define a fase com base no número de equipes
        LocalDateTime dataBase = calcularDataBase(evento);// define a data base para os jogos

        for (int i = 0; i < equipes.size(); i += 2) {// percorre as equipes de 2 em 2
            Equipe e1 = equipes.get(i);// pega a equipe da posição i
            Equipe e2 = equipes.get(i + 1);// pega a equipe da posição i+1

            Jogo jogo = criarJogoEliminatorio(e1, e2, fase + " " + (i / 2 + 1), evento,
                    dataBase.plusHours(jogos.size())); // cria um novo jogo eliminatório
            jogo.setChaveEliminatoria(chave);// associa o jogo à chave eliminatória

            if (e1.getNome().equalsIgnoreCase(BYE_EQUIPE_NOME)) {// se a equipe 1 for a equipe bye
                configurarBye(jogo, 0, 1);// da vitoria automatica
            } else if (e2.getNome().equalsIgnoreCase(BYE_EQUIPE_NOME)) {// se a equipe 2 for bye
                configurarBye(jogo, 1, 0);// da vitoria automatica
            }

            jogos.add(jogo);// vai pra lista de jgos
        }

        return jogos;
    }

    // adiciona a equipe bye se o número de equipes for ímpar
    private void ajustarQuantidadeEquipes(List<Equipe> equipes, Evento evento) {
        if (equipes.size() % 2 != 0) {
            equipes.add(criarEquipeBye(evento, equipes.get(0)));
        }
    }

    // cria a equipe bye com base no modelo da primeira equipe
    private Equipe criarEquipeBye(Evento evento, Equipe modelo) {
        return equipeService.buscarOuCriarEquipeBye(
                evento,
                modelo.getEsporte(),
                modelo.getCurso(),
                modelo.getCampus());
    }

    // determina a fase com base no número de equipes
    private String determinarFase(int numEquipes) {
        return switch (numEquipes) {
            case 8 -> "Quartas de Final";
            case 4 -> "Semifinal";
            case 2 -> "Final";
            default -> "Pré-Eliminatória";
        };
    }

    // calcula a data base para os jogos, considerando o último jogo do evento ou a
    // data de início
    private LocalDateTime calcularDataBase(Evento evento) {
        return jogoRepository.findTopByEventoIdOrderByDataHoraDesc(evento.getId())
                .map(j -> j.getDataHora().plusDays(1))
                .orElse(evento.getDataInicio().atTime(10, 0));
    }

    // cria um jogo eliminatório com as equipes, fase, evento e data
    private Jogo criarJogoEliminatorio(Equipe e1, Equipe e2, String fase, Evento evento, LocalDateTime dataHora) {
        validarConfronto(e1, e2, fase);

        Jogo jogo = new Jogo();
        jogo.setEquipe1(e1);
        jogo.setEquipe2(e2);
        jogo.setEvento(evento);
        jogo.setFase(fase);
        jogo.setFinalizado(false);
        jogo.setDataHora(dataHora);
        return jogo;
    }

    // evita confronto do mesmo grupo antes da final
    private void validarConfronto(Equipe e1, Equipe e2, String fase) {
        if (!"Final".equals(fase) && pertencemAoMesmoGrupo(e1, e2)) {
            System.out.println("Aviso: confronto entre equipes do mesmo grupo na fase " + fase);
        }
    }

    // vitoria automatica contra bye
    private void configurarBye(Jogo jogo, int placar1, int placar2) {
        jogo.setFinalizado(true);
        jogo.setPlacarEquipe1(placar1);
        jogo.setPlacarEquipe2(placar2);
    }

    @Transactional
    public List<JogoDTO> gerarFinais(Long eventoId) {
        Evento evento = validarEvento(eventoId);// busca e valida o evento
        List<Jogo> semifinais = validarSemifinais(eventoId);// verifica se existem seminfiains finalizadas
        List<Equipe> finalistas = determinarFinalistas(semifinais);// determina as duas equipes da seminfinal

        ChaveEliminatoria chave = chaveRepository.findByEventoId(eventoId)// busca a chave eliminatoria associada ao
                                                                          // evento
                .orElseThrow(() -> new RuntimeException("Chave não encontrada para o evento"));

        Jogo finalJogo = criarJogoFinal(evento, finalistas);// cria jogo representando a final
        finalJogo.setChaveEliminatoria(chave);// associa esse jogo a chave eliminatoria do evento

        jogoRepository.save(finalJogo);// salva

        return List.of(new JogoDTO(finalJogo));// retorna o jogo em dto
    }

    // valida se existem semifinais finalizadas e retorna a lista
    private List<Jogo> validarSemifinais(Long eventoId) {
        List<Jogo> semifinais = jogoRepository.findByEventoIdAndFaseStartingWith(eventoId, "Semifinal");

        if (semifinais.size() != 2) {
            throw new RuntimeException("Número incorreto de semifinais encontradas");
        }

        validarJogosFinalizados(semifinais, "Semifinal");
        return semifinais;
    }

    // valida se todos os jogos de uma fase estão finalizados
    private void validarJogosFinalizados(List<Jogo> jogos, String fase) {
        for (Jogo jogo : jogos) {
            if (!jogo.isFinalizado()) {
                throw new RuntimeException("Jogo " + jogo.getId() + " da " + fase + " não foi finalizado");
            }
        }
    }

    // determina finalistas a aprtir dos vencedores da semifinais
    private List<Equipe> determinarFinalistas(List<Jogo> semifinais) {
        return semifinais.stream()
                .map(j -> j.getPlacarEquipe1() > j.getPlacarEquipe2() ? j.getEquipe1() : j.getEquipe2())
                .collect(Collectors.toList());
    }

    // valida se as fases finais estao completas e determina a classificação final
    @Transactional(readOnly = true)
    public List<ClassificacaoDTO> getClassificacaoFinal(Long eventoId) {
        List<Jogo> finais = jogoRepository.findByEventoIdAndFase(eventoId, "Final");
        List<Jogo> semifinais = jogoRepository.findByEventoIdAndFaseStartingWith(eventoId, "Semifinal");

        validarFasesCompletas(finais, semifinais);
        return determinarPodio(finais.get(0), semifinais);
    }

    // valida se as fases finais estão completas
    private void validarFasesCompletas(List<Jogo> finais, List<Jogo> semifinais) {
        if (finais.size() != 1 || semifinais.size() != 2) {
            throw new RuntimeException("Fases eliminatórias incompletas");
        }
    }

    // determina o podio do evento
    private List<ClassificacaoDTO> determinarPodio(Jogo finalJogo, List<Jogo> semifinais) {
        List<ClassificacaoDTO> podio = new ArrayList<>();// armazena o podio final

        Equipe campeao = determinarVencedor(finalJogo);// determina campeao
        podio.add(new ClassificacaoDTO(campeao, 0, 0, 0, 0));// adiciona o campeao como 1 no podio

        Equipe vice = determinarPerdedor(finalJogo);// determina o vice
        podio.add(new ClassificacaoDTO(vice, 0, 0, 0, 0));// adiciona

        try {
            Equipe terceiro = determinarTerceiroColocado(semifinais, campeao, vice);// tenta determinar o terceiro
            if (terceiro != null && !terceiro.getNome().equalsIgnoreCase("Bye")) {// nao pode ser nulo nem bye
                podio.add(new ClassificacaoDTO(terceiro, 0, 0, 0, 0));// adiciona
            }
        } catch (RuntimeException e) {
            System.out.println("Não foi possível determinar o terceiro colocado: " + e.getMessage());
        }

        return podio;
    }

    // primeiro
    private Equipe determinarVencedor(Jogo jogo) {
        return jogo.getPlacarEquipe1() > jogo.getPlacarEquipe2() ? jogo.getEquipe1() : jogo.getEquipe2();
    }

    // segundo
    private Equipe determinarPerdedor(Jogo jogo) {
        return jogo.getPlacarEquipe1() < jogo.getPlacarEquipe2() ? jogo.getEquipe1() : jogo.getEquipe2();
    }

    // terceiro
    private Equipe determinarTerceiroColocado(List<Jogo> semifinais, Equipe campeao, Equipe vice) {
        if (semifinais == null || semifinais.size() < 2) {
            throw new RuntimeException("Semifinais incompletas para determinar terceiro colocado");
        }

        return semifinais.stream()
                .flatMap(j -> Arrays.asList(j.getEquipe1(), j.getEquipe2()).stream())
                .filter(Objects::nonNull)
                .filter(e -> !e.equals(campeao))
                .filter(e -> !e.equals(vice))
                .filter(e -> !e.getNome().equalsIgnoreCase("Bye"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Não foi possível determinar o terceiro colocado"));
    }

    // dtos
    @Transactional
    public ChaveDTO gerarChaveDTO(Long eventoId) {
        ChaveEliminatoria chave = gerarEliminatorias(eventoId);
        List<Jogo> jogos = jogoRepository.findByChaveEliminatoriaId(chave.getId());
        return chave.toDTO(jogos);
    }

    @Transactional(readOnly = true)
    public ChaveDTO buscarChaveDTOPorEvento(Long eventoId) {
        ChaveEliminatoria chave = chaveRepository.findByEventoIdWithJogos(eventoId)
                .orElseThrow(() -> new RuntimeException("Chave não encontrada para o evento"));
        List<Jogo> jogos = chave.getJogos();
        return chave.toDTO(jogos);
    }

    @Transactional(readOnly = true)
    public List<JogoDTO> listarJogosPorChave(Long chaveId) {
        ChaveEliminatoria chave = chaveRepository.findByIdWithJogos(chaveId);
        if (chave == null)
            throw new RuntimeException("Chave não encontrada");
        return chave.getJogos().stream().map(JogoDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EliminatoriaDTO> listarEliminatoriasPorChave(Long chaveId) {
        ChaveEliminatoria chave = chaveRepository.findByIdWithJogos(chaveId);
        if (chave == null)
            throw new RuntimeException("Chave não encontrada");
        return chave.getJogos().stream().map(EliminatoriaDTO::new).collect(Collectors.toList());
    }

    // jogo final
    private Jogo criarJogoFinal(Evento evento, List<Equipe> finalistas) {
        return criarJogoEliminatorio(
                finalistas.get(0),
                finalistas.get(1),
                "Final",
                evento,
                calcularDataBase(evento));
    }

    // gerar manualmente a proxima fase das eliminatorias
    public void gerarProximaFaseManual(Long eventoId, String novaFase) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        ChaveEliminatoria chave = chaveRepository.findByEventoId(eventoId)
                .orElseThrow(() -> new RuntimeException("Chave eliminatória não encontrada"));

        List<Jogo> anteriores = jogoRepository.findByEventoId(eventoId).stream()
                .filter(j -> j.getFase() != null && j.getFase().startsWith("Pré-Eliminatória") && j.isFinalizado())
                .collect(Collectors.toList());

        List<Equipe> classificados = anteriores.stream()// busca os jogos do evento
                .map(j -> j.getPlacarEquipe1() > j.getPlacarEquipe2() ? j.getEquipe1() : j.getEquipe2())
                .collect(Collectors.toList());// pega os vencedores e converte pra lsitas de classificados

        if (classificados.size() % 2 != 0) {// caso for impar cria o bye
            classificados.add(equipeService.buscarOuCriarEquipeBye(evento, classificados.get(0).getEsporte(),
                    classificados.get(0).getCurso(), classificados.get(0).getCampus()));
        }

        List<Jogo> novosJogos = new ArrayList<>();// armazena os jogos da nova fase
        LocalDateTime baseData = calcularDataBase(evento);

        for (int i = 0; i < classificados.size(); i += 2) {// percorre de 2 em 2
            Equipe e1 = classificados.get(i);
            Equipe e2 = classificados.get(i + 1);

            Jogo jogo = criarJogoEliminatorio(e1, e2, novaFase + " " + (i / 2 + 1), evento, baseData.plusHours(i));
            jogo.setChaveEliminatoria(chave);

            if (e1.getNome().equalsIgnoreCase("Bye")) {
                configurarBye(jogo, 0, 1);
            } else if (e2.getNome().equalsIgnoreCase("Bye")) {
                configurarBye(jogo, 1, 0);
            }

            novosJogos.add(jogo);
        }

        jogoRepository.saveAll(novosJogos);
    }

    // gera automaticamente a proxima fase das eliminatorias
    @Transactional
    public void gerarProximaFase(Long eventoId, String novaFase) {
        Evento evento = validarEvento(eventoId);
        ChaveEliminatoria chave = chaveRepository.findByEventoId(eventoId)
                .orElseThrow(() -> new RuntimeException("Chave eliminatória não encontrada"));

        List<Jogo> anteriores = jogoRepository.findByEventoId(eventoId).stream()
                .filter(j -> j.getFase() != null && j.getFase().startsWith("Pré-Eliminatória") && j.isFinalizado())
                .collect(Collectors.toList());

        List<Equipe> classificados = anteriores.stream()
                .map(j -> j.getPlacarEquipe1() > j.getPlacarEquipe2() ? j.getEquipe1() : j.getEquipe2())
                .collect(Collectors.toList());

        if (classificados.size() % 2 != 0) {
            classificados.add(equipeService.buscarOuCriarEquipeBye(
                    evento,
                    classificados.get(0).getEsporte(),
                    classificados.get(0).getCurso(),
                    classificados.get(0).getCampus()));
        }

        List<Jogo> novosJogos = new ArrayList<>();
        LocalDateTime baseData = calcularDataBase(evento);

        for (int i = 0; i < classificados.size(); i += 2) {
            Equipe e1 = classificados.get(i);
            Equipe e2 = classificados.get(i + 1);

            Jogo jogo = criarJogoEliminatorio(e1, e2, novaFase + " " + (i / 2 + 1), evento, baseData.plusHours(i));
            jogo.setChaveEliminatoria(chave);

            if (e1.getNome().equalsIgnoreCase("Bye")) {
                configurarBye(jogo, 0, 1);
            } else if (e2.getNome().equalsIgnoreCase("Bye")) {
                configurarBye(jogo, 1, 0);
            }

            novosJogos.add(jogo);
        }

        jogoRepository.saveAll(novosJogos);
    }

    // dto top 3
    @Transactional(readOnly = true)
    public Map<Long, List<ClassificacaoDTO>> getTop3PorEvento() {
        List<Evento> eventos = eventoRepository.findAll();
        Map<Long, List<ClassificacaoDTO>> resultados = new HashMap<>();

        for (Evento evento : eventos) {
            try {
                List<ClassificacaoDTO> top3 = getClassificacaoFinal(evento.getId());
                resultados.put(evento.getId(), top3);
            } catch (RuntimeException e) {
                System.out.println("Evento " + evento.getNome() + " não possui classificação final completa.");
            }
        }

        return resultados;
    }

    @Transactional(readOnly = true)
    public List<ClassificacaoPorEventoDTO> listarTop3PorEvento() {
        List<Evento> eventos = eventoRepository.findAll();
        List<ClassificacaoPorEventoDTO> resultado = new ArrayList<>();

        for (Evento evento : eventos) {
            try {
                List<ClassificacaoDTO> classificacao;

                try {
                    // Tenta obter a classificação final completa
                    classificacao = getClassificacaoFinal(evento.getId());
                } catch (RuntimeException e) {
                    // Se não conseguir, tenta obter uma classificação parcial
                    classificacao = calcularClassificacaoGeralEvento(evento.getId());

                    // Filtra apenas as primeiras 3 equipes para o top 3
                    if (classificacao.size() > 3) {
                        classificacao = classificacao.subList(0, 3);
                    }
                }

                List<ClassificacaoSimplesDTO> top3Simplificado = classificacao.stream()
                        .filter(c -> !c.getEquipe().getNome().equalsIgnoreCase("Bye"))
                        .map(c -> new ClassificacaoSimplesDTO(
                                c.getEquipe().getNome(),
                                c.getEquipe().getEsporte().getNome(),
                                c.getEquipe().getCampus().getNome()))
                        .distinct()
                        .limit(3)
                        .collect(Collectors.toList());

                resultado.add(new ClassificacaoPorEventoDTO(
                        evento.getNome(),
                        evento.getTipoEvento().name(),
                        top3Simplificado));

            } catch (RuntimeException e) {
                System.out.println("Evento " + evento.getNome() + " não possui classificação: " + e.getMessage());

                resultado.add(new ClassificacaoPorEventoDTO(
                        evento.getNome(),
                        evento.getTipoEvento().name(),
                        new ArrayList<>()));
            }
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<ClassificacaoDTO> calcularClassificacaoGeralEvento(Long eventoId) {
        List<Equipe> equipes = equipeService.buscarEquipesPorEvento(eventoId);
        List<Jogo> jogos = jogoRepository.findByEventoId(eventoId);

        List<ClassificacaoDTO> classificacao = equipes.stream()
                .map(e -> new ClassificacaoDTO(e, 0, 0, 0, 0))
                .collect(Collectors.toList());

        for (Jogo jogo : jogos) {
            if (!jogo.isFinalizado())
                continue;

            ClassificacaoDTO dto1 = encontrarEquipeNaClassificacao(classificacao, jogo.getEquipe1());
            ClassificacaoDTO dto2 = encontrarEquipeNaClassificacao(classificacao, jogo.getEquipe2());

            atualizarClassificacao(jogo, dto1, dto2);
        }

        return ordenarClassificacao(classificacao);
    }

}
