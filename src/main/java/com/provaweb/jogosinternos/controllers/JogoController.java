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
import com.provaweb.jogosinternos.entities.Jogo;
import com.provaweb.jogosinternos.services.JogoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jogos")
@RequiredArgsConstructor
public class JogoController {
    private final JogoService jogoService;

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
    public Jogo atualizarPlacar(
            @PathVariable Long id,
            @RequestBody AtualizarPlacarDTO dto) {
        return jogoService.atualizarPlacar(id, dto.getPlacarEquipe1(), dto.getPlacarEquipe2());
    }

    @PutMapping("/{id}/wo")
    public Jogo registrarWO(
            @PathVariable Long id,
            @RequestParam(required = false) Long equipeId) {
        return jogoService.registrarWO(id, equipeId);
    }

    @DeleteMapping("/{id}")
    public void cancelarJogo(@PathVariable Long id) {
        jogoService.cancelarJogo(id);
    }

    @PutMapping("/{id}/desfazer-wo")
    public Jogo desfazerWO(@PathVariable Long id) {
        return jogoService.desfazerWO(id);
    }

    @GetMapping("/fases")
    public ResponseEntity<List<String>> listarFases() {
        return ResponseEntity.ok(jogoService.listarFases());
    }

}