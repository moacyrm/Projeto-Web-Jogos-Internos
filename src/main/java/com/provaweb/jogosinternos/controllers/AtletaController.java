package com.provaweb.jogosinternos.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.provaweb.jogosinternos.dto.AtletaDTO;
import com.provaweb.jogosinternos.entities.Atleta;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.services.AtletaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/atletas")
@RequiredArgsConstructor
public class AtletaController {

    private final AtletaService atletaService;

    @PostMapping
    public ResponseEntity<AtletaDTO> criar(@RequestBody Atleta atleta) {
        Atleta novo = atletaService.cadastrarAtleta(atleta);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo.toDTO());
    }

    @GetMapping
    public ResponseEntity<List<AtletaDTO>> listarTodos() {
        List<AtletaDTO> dtos = atletaService.listarTodos()
                .stream()
                .map(Atleta::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtletaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(atletaService.buscarPorId(id).toDTO());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtletaDTO> atualizar(@PathVariable Long id, @RequestBody Atleta atleta) {
        Atleta atualizado = atletaService.atualizarAtleta(id, atleta);
        return ResponseEntity.ok(atualizado.toDTO());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        atletaService.deletarAtleta(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/definir-tecnico")
    public ResponseEntity<Void> definirTecnico(@PathVariable Long id, @RequestParam String coordenadorId) {
        atletaService.definirTecnico(id, coordenadorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tecnico")
    public ResponseEntity<Boolean> isTecnico(@PathVariable Long id) {
        return ResponseEntity.ok(atletaService.isTecnico(id));
    }

    @GetMapping("/{id}/equipe")
    public ResponseEntity<Equipe> getEquipeDoAtleta(@PathVariable Long id) {
        return ResponseEntity.ok(atletaService.buscarPorId(id).getEquipe());
    }

    @PutMapping("/{id}/equipe")
   public ResponseEntity<AtletaDTO> atualizarEquipe(
        @PathVariable Long id,
        @RequestParam Long equipeId,
        @RequestParam(required = false) Long tecnicoId) {

    Atleta atualizado = atletaService.atualizarEquipe(id, equipeId, tecnicoId);
    return ResponseEntity.ok(atualizado.toDTO());
}

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<AtletaDTO>> listarPorEquipe(@PathVariable Long equipeId) {
        List<AtletaDTO> dtos = atletaService.buscarPorEquipe(equipeId)
                .stream()
                .map(Atleta::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{atletaId}/equipe/{equipeId}")
    public ResponseEntity<AtletaDTO> buscarAtletaNaEquipe(
            @PathVariable Long atletaId,
            @PathVariable Long equipeId) {
        Atleta atleta = atletaService.buscarAtletaNaEquipe(atletaId, equipeId);
        return ResponseEntity.ok(atleta.toDTO());
    }
}
