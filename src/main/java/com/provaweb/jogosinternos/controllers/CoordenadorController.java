package com.provaweb.jogosinternos.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
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

import com.provaweb.jogosinternos.dto.AtletaDTO;
import com.provaweb.jogosinternos.entities.Coordenador;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.repositories.CoordenadorRepository;
import com.provaweb.jogosinternos.services.AtletaService;
import com.provaweb.jogosinternos.services.CoordenadorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coordenadores")
@RequiredArgsConstructor
public class CoordenadorController {

    private final CoordenadorService coordenadorService;
    private final CoordenadorRepository coordenadorRepository;
    private final AtletaService atletaService;

    @PostMapping
    public Coordenador criar(@RequestBody Coordenador coordenador) {
        return coordenadorService.cadastrarCoordenador(coordenador);
    }

    @GetMapping
    public List<Coordenador> listarTodos() {
        return coordenadorService.listarTodos();
    }

    @PutMapping("/{id}")
    public Coordenador atualizar(@PathVariable String id, @RequestBody Coordenador coordenador) {
        return coordenadorService.atualizarCoordenador(id, coordenador);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable String id) {
        coordenadorService.deletarCoordenador(id);
    }

    @GetMapping("/{matricula}/equipes")
    public List<Equipe> listarEquipesPorCurso(@PathVariable String matricula) {
        return coordenadorService.listarEquipesPorCoordenador(matricula);
    }

    @PutMapping("/definir-tecnico/{atletaId}")
    public ResponseEntity<?> definirTecnico(
            @PathVariable Long atletaId,
            @RequestParam String matriculaCoordenador) {
        return coordenadorService.definirTecnico(matriculaCoordenador, atletaId);
    }

    @GetMapping("/atletas")
    public ResponseEntity<?> listarAtletasPorCurso(
            @RequestParam String matriculaCoordenador) {
        try {
            var coordenador = coordenadorRepository.findById(matriculaCoordenador)
                    .orElseThrow(() -> new RuntimeException("Coordenador não encontrado"));

            if (coordenador.getCurso() == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            Long cursoId = coordenador.getCurso().getId();
            List<AtletaDTO> dtos = atletaService.listarAtletasDTOPorCurso(cursoId);

            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao buscar atletas"));
        }
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<Coordenador> buscarPorMatricula(@PathVariable String matricula) {
        Coordenador coordenador = coordenadorService.buscarPorId(matricula);
        return ResponseEntity.ok(coordenador);
    }

    @PostMapping("/{matricula}/equipes")
    public ResponseEntity<?> criarEquipePorCoordenador(
            @PathVariable String matricula,
            @RequestBody Equipe equipe,
            @RequestParam(required = false) Long tecnicoId) {

        return coordenadorService.criarEquipePorCoordenador(matricula, equipe, tecnicoId);
    }

}
