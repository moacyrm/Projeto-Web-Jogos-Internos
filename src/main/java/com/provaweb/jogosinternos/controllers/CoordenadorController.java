package com.provaweb.jogosinternos.controllers;

import java.util.List;

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

import com.provaweb.jogosinternos.entities.Atleta;
import com.provaweb.jogosinternos.entities.Coordenador;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.services.CoordenadorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coordenadores")
@RequiredArgsConstructor
public class CoordenadorController {

    private final CoordenadorService coordenadorService;

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
    public ResponseEntity<List<Atleta>> listarAtletasPorCurso(
            @RequestParam String matriculaCoordenador) {
        return coordenadorService.listarAtletasPorCurso(matriculaCoordenador);
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<Coordenador> buscarPorMatricula(@PathVariable String matricula) {
        Coordenador coordenador = coordenadorService.buscarPorId(matricula);
        return ResponseEntity.ok(coordenador);
    }
}
