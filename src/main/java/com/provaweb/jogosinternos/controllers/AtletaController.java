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

import jakarta.persistence.EntityNotFoundException;
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
    public ResponseEntity<?> atualizarAtleta(
            @PathVariable Long id,
            @RequestBody Atleta atualizacoes) {

        try {
            Atleta atletaAtualizado = atletaService.atualizarAtleta(id, atualizacoes);
            return ResponseEntity.ok(atletaAtualizado.toDTO());

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Atleta não encontrado");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar atleta: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        atletaService.deletarAtleta(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/definir-tecnico")
    public ResponseEntity<Atleta> definirComoTecnico(@PathVariable Long id) {
        return ResponseEntity.ok(atletaService.definirComoTecnico(id));
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

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<AtletaDTO> buscarPorMatricula(@PathVariable String matricula) {
        Atleta atleta = atletaService.buscarPorMatricula(matricula);
        return ResponseEntity.ok(atleta.toDTO());
    }

}
