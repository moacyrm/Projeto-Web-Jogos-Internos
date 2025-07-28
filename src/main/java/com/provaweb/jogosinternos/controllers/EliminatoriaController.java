package com.provaweb.jogosinternos.controllers;

import com.provaweb.jogosinternos.dto.ChaveDTO;
import com.provaweb.jogosinternos.dto.ClassificacaoPorEventoDTO;
import com.provaweb.jogosinternos.dto.EliminatoriaDTO;
import com.provaweb.jogosinternos.dto.JogoDTO;
import com.provaweb.jogosinternos.repositories.ChaveRepository;
import com.provaweb.jogosinternos.services.EliminatoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eliminatorias")
@RequiredArgsConstructor
public class EliminatoriaController {

    private final EliminatoriaService eliminatoriaService;
    private final ChaveRepository chaveRepository;

    @PostMapping("/gerar/{eventoId}")
    public ResponseEntity<?> gerarEliminatorias(@PathVariable Long eventoId) {
        try {
            ChaveDTO chaveDTO = eliminatoriaService.gerarChaveDTO(eventoId);
            return ResponseEntity.ok(chaveDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/jogos/{chaveId}")
    public ResponseEntity<List<JogoDTO>> listarJogosPorChave(@PathVariable Long chaveId) {
        List<JogoDTO> jogos = eliminatoriaService.listarJogosPorChave(chaveId);
        return ResponseEntity.ok(jogos);
    }

    @GetMapping("/partidas/{chaveId}")
    public ResponseEntity<List<EliminatoriaDTO>> listarEliminatoriasPorChave(@PathVariable Long chaveId) {
        List<EliminatoriaDTO> eliminatorias = eliminatoriaService.listarEliminatoriasPorChave(chaveId);
        return ResponseEntity.ok(eliminatorias);
    }

    @GetMapping("/teste/existeChave/{eventoId}")
    public boolean testeExisteChave(Long eventoId) {
        return chaveRepository.existsByEventoId(eventoId);
    }

    @PostMapping("/gerarFinais/{eventoId}")
    public ResponseEntity<?> gerarFinais(@PathVariable Long eventoId) {
        try {
            List<JogoDTO> jogos = eliminatoriaService.gerarFinais(eventoId);
            return ResponseEntity.ok(jogos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/chave/{eventoId}")
    public ResponseEntity<ChaveDTO> buscarChaveDTO(@PathVariable Long eventoId) {
        ChaveDTO dto = eliminatoriaService.buscarChaveDTOPorEvento(eventoId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/gerarProximaFase")
    public ResponseEntity<?> gerarProximaFase(@RequestParam Long eventoId, @RequestParam String fase) {
        try {
            eliminatoriaService.gerarProximaFase(eventoId, fase);
            return ResponseEntity.ok("Fase '" + fase + "' gerada com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/top3eventos")
    public ResponseEntity<List<ClassificacaoPorEventoDTO>> listarTop3PorEvento() {
        List<ClassificacaoPorEventoDTO> resultado = eliminatoriaService.listarTop3PorEvento();
        return ResponseEntity.ok(resultado);
    }
}
