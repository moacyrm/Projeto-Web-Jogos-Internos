package com.provaweb.jogosinternos.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.provaweb.jogosinternos.dto.EquipeDTO;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.services.EquipeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/equipes")
@RequiredArgsConstructor
public class EquipeController {
    private final EquipeService equipeService;

    @PostMapping
    public ResponseEntity<EquipeDTO> cadastrar(@RequestBody Equipe equipe, @RequestParam(required = false) Long tecnicoId) {
        try {
            Equipe nova = equipeService.cadastEquipe(equipe, tecnicoId);
            return ResponseEntity.ok(nova.toDTO());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/porevento/{eventoId}")
    public List<EquipeDTO> listarPorEvento(@PathVariable Long eventoId) {
        return equipeService.listPorEvento(eventoId).stream()
                .map(Equipe::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipeDTO> atualizarEquipe(@PathVariable Long id, @RequestBody Equipe novaEquipe) {
        try {
            Equipe equipeAtualizada = equipeService.atualizarEquipe(id, novaEquipe);
            return ResponseEntity.ok(equipeAtualizada.toDTO());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public List<EquipeDTO> listarTodasEquipes() {
        return equipeService.listarTodos().stream()
                .map(Equipe::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeDTO> buscarEquipePorId(@PathVariable Long id) {
        try {
            Equipe equipe = equipeService.buscarPorId(id);
            return ResponseEntity.ok(equipe.toDTO());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
