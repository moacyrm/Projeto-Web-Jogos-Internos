package com.provaweb.jogosinternos.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.provaweb.jogosinternos.dto.AtualizarPlacarDTO;
import com.provaweb.jogosinternos.dto.JogoDTO;
import com.provaweb.jogosinternos.entities.Arbitro;
import com.provaweb.jogosinternos.entities.Jogo;
import com.provaweb.jogosinternos.repositories.ArbitroRepository;
import com.provaweb.jogosinternos.services.JogoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jogos")
@RequiredArgsConstructor
public class JogoController {
    private final JogoService jogoService;
    private final ArbitroRepository arbitroRepository;

    @PostMapping
    public Jogo criar(@RequestBody Jogo jogo) {
        return jogoService.criarJogo(jogo);
    }

    @GetMapping
    public List<Jogo> listarTodos() {
        return jogoService.listarTodos();
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<JogoDTO>> listarPorGrupo(@PathVariable Long grupoId) {
        List<Jogo> jogos = jogoService.listarPorGrupo(grupoId);
        List<JogoDTO> dtos = jogos.stream()
                .map(JogoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<JogoDTO>> listarPorEvento(@PathVariable Long eventoId) {
        List<Jogo> jogos = jogoService.listarPorEvento(eventoId);
        List<JogoDTO> dtos = jogos.stream()
                .map(JogoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/placar")
    public ResponseEntity<JogoDTO> atualizarPlacar(
            @PathVariable Long id,
            @RequestBody AtualizarPlacarDTO dto) {
        Jogo atualizado = jogoService.atualizarPlacar(id, dto.getPlacarEquipe1(), dto.getPlacarEquipe2());
        return ResponseEntity.ok(new JogoDTO(atualizado));
    }

    @PutMapping("/{id}/wo")
    public ResponseEntity<JogoDTO> registrarWO(
            @PathVariable Long id,
            @RequestParam(required = false) Long equipeId) {
        Jogo atualizado = jogoService.registrarWO(id, equipeId);
        return ResponseEntity.ok(new JogoDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarJogo(@PathVariable Long id) {
        jogoService.cancelarJogo(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/desfazer-wo")
    public ResponseEntity<JogoDTO> desfazerWO(@PathVariable Long id) {
        Jogo atualizado = jogoService.desfazerWO(id);
        return ResponseEntity.ok(new JogoDTO(atualizado));
    }

    @GetMapping("/fases")
    public ResponseEntity<List<String>> listarFases() {
        return ResponseEntity.ok(jogoService.listarFases());
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<JogoDTO>> listarPorEquipe(@PathVariable Long equipeId) {
        List<Jogo> jogos = jogoService.listarPorEquipe(equipeId);
        List<JogoDTO> dtos = jogos.stream()
                .map(JogoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/atleta/matricula/{matricula}")
    public ResponseEntity<List<JogoDTO>> listarPorMatriculaAtleta(
            @PathVariable String matricula,
            @RequestParam(required = false) Long eventoId) {

        System.out.println("Buscando jogos para matrícula: " + matricula + ", evento: " + eventoId);

        List<Jogo> jogos = jogoService.listarPorMatriculaAtleta(matricula, eventoId);
        List<JogoDTO> dtos = jogos.stream()
                .map(JogoDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/coordenador/{matricula}")
    public ResponseEntity<List<JogoDTO>> listarJogosPorCoordenador(
            @PathVariable String matricula,
            @RequestParam(required = false) Long eventoId) {
        List<Jogo> jogos = jogoService.buscarJogosPorCoordenador(matricula, eventoId);
        List<JogoDTO> dtos = jogos.stream()
                .map(JogoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/definir-placar")
    public Jogo definirPlacar(
            @PathVariable Long id,
            @RequestParam int placarEquipe1,
            @RequestParam int placarEquipe2,
            @RequestParam String matriculaArbitro) {

        Jogo jogo = jogoService.buscarPorId(id);

        Arbitro arbitro = arbitroRepository.findByMatricula(matriculaArbitro)
                .orElseThrow(() -> new RuntimeException("Árbitro não encontrado"));

        if (!jogo.getArbitro().getId().equals(arbitro.getId())) {
            throw new RuntimeException("Somente o árbitro designado pode definir o placar deste jogo.");
        }

        jogo.setPlacarEquipe1(placarEquipe1);
        jogo.setPlacarEquipe2(placarEquipe2);
        jogo.setStatus("FINALIZADO");

        return jogoService.salvar(jogo);
    }

}