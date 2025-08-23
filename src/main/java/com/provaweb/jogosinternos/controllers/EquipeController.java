package com.provaweb.jogosinternos.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.provaweb.jogosinternos.dto.AtletaDTO;
import com.provaweb.jogosinternos.dto.EquipeDTO;
import com.provaweb.jogosinternos.dto.EquipeDetalhesDTO;
import com.provaweb.jogosinternos.dto.RankingDTO;
import com.provaweb.jogosinternos.entities.CriarEquipeRequest;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.services.EquipeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/equipes")
@RequiredArgsConstructor
public class EquipeController {
    private final EquipeService equipeService;

    @PostMapping
    public ResponseEntity<EquipeDTO> cadastrar(@RequestBody Equipe equipe,
            @RequestParam(required = false) Long tecnicoId) {
        try {
            Equipe nova = equipeService.cadastEquipe(equipe, tecnicoId);
            return ResponseEntity.ok(nova.toDTO());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
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

    @GetMapping("/atleta/{matricula}")
    public ResponseEntity<EquipeDetalhesDTO> getEquipePorAtleta(@PathVariable String matricula) {
        try {
            EquipeDetalhesDTO detalhes = equipeService.getEquipeDetalhesPorMatriculaAtleta(matricula);
            return ResponseEntity.ok(detalhes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @GetMapping("/coordenador/{matricula}")
    public ResponseEntity<List<EquipeDetalhesDTO>> getEquipesPorCoordenador(@PathVariable String matricula) {
        try {
            List<EquipeDetalhesDTO> lista = equipeService.getEquipesDetalhesPorMatriculaCoordenador(matricula);
            return ResponseEntity.ok(lista);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<?> submitEquipe(@PathVariable Long id, @RequestParam Long tecnicoId) {
        try {
            Equipe salva = equipeService.submitEquipe(id, tecnicoId);
            return ResponseEntity.ok(Map.of("message", "Equipe submetida", "status", salva.getStatus()));
        } catch (ResponseStatusException r) {
            return ResponseEntity.status(r.getStatusCode()).body(Map.of("error", r.getReason()));
        }
    }

    @GetMapping("/por-atleta/{matricula}")
    public ResponseEntity<List<EquipeDTO>> listarPorAtleta(
            @PathVariable String matricula,
            @RequestParam(required = false) Long eventoId) {
        List<EquipeDTO> dtos = equipeService.listarEquipesPorMatriculaAtleta(matricula, eventoId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/detalhes")
    public ResponseEntity<EquipeDetalhesDTO> getDetalhes(@PathVariable Long id) {
        EquipeDetalhesDTO dto = equipeService.getEquipeDetalhesPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/criar-para-evento")
    public ResponseEntity<EquipeDTO> criarEquipeParaEvento(
            @RequestBody CriarEquipeRequest request,
            @RequestParam String matriculaTecnico) {
        try {
            Equipe novaEquipe = equipeService.criarEquipeParaEvento(
                    request.getNome(),
                    request.getEventoId(),
                    request.getEsporteId(),
                    matriculaTecnico);
            return ResponseEntity.ok(novaEquipe.toDTO());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/verificar-inscricao")
    public ResponseEntity<Boolean> verificarInscricao(
            @RequestParam Long eventoId,
            @RequestParam String matriculaTecnico) {
        boolean inscrito = equipeService.verificarInscricao(eventoId, matriculaTecnico);
        return ResponseEntity.ok(inscrito);
    }

    @PostMapping("/{equipeId}/adicionar-atleta/{atletaId}")
    public ResponseEntity<Void> adicionarAtleta(
            @PathVariable Long equipeId,
            @PathVariable Long atletaId,
            @RequestParam String matriculaTecnico) {
        try {
            equipeService.adicionarAtleta(equipeId, atletaId, matriculaTecnico);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/atletas-disponiveis")
    public ResponseEntity<List<AtletaDTO>> listarAtletasDisponiveis(
            @RequestParam Long eventoId,
            @RequestParam String matriculaTecnico) {
        List<AtletaDTO> atletas = equipeService.listarAtletasDisponiveis(eventoId, matriculaTecnico);
        return ResponseEntity.ok(atletas);
    }

    @GetMapping("/ranking/{eventoId}")
    public ResponseEntity<List<RankingDTO>> getRankingPorEvento(@PathVariable Long eventoId) {
        try {
            List<RankingDTO> ranking = equipeService.calcularRankingPorEvento(eventoId);
            return ResponseEntity.ok(ranking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
