package com.provaweb.jogosinternos.controllers;

import com.provaweb.jogosinternos.dto.ChaveDTO;
import com.provaweb.jogosinternos.services.ChaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chaves")
@RequiredArgsConstructor
public class ChaveController {

    private final ChaveService chaveService;

    @GetMapping("/{id}")
    public ChaveDTO getChave(@PathVariable Long id) {
        return chaveService.buscarChaveDTO(id);
    }
}