package com.provaweb.jogosinternos.controllers;

import com.provaweb.jogosinternos.dto.ChaveDTO;
import com.provaweb.jogosinternos.services.ChaveEliminatoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chaves")
@RequiredArgsConstructor
public class ChaveEliminatoriaController {

    private final ChaveEliminatoriaService chaveService;

    @PostMapping("/gerar/{eventoId}")
    public ResponseEntity<ChaveDTO> gerarChaveEliminatoria(@PathVariable Long eventoId) {
        ChaveDTO chaveDTO = chaveService.gerarChaveEliminatoria(eventoId);
        return ResponseEntity.ok(chaveDTO);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<ChaveDTO> buscarChavePorEvento(@PathVariable Long eventoId) {
        ChaveDTO chaveDTO = chaveService.buscarChavePorEvento(eventoId);
        return ResponseEntity.ok(chaveDTO);
    }
}
