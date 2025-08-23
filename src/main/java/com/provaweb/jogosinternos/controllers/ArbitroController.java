package com.provaweb.jogosinternos.controllers;

import org.springframework.web.bind.annotation.*;

import com.provaweb.jogosinternos.entities.Arbitro;
import com.provaweb.jogosinternos.services.ArbitroService;

import java.util.List;

@RestController
@RequestMapping("/arbitros")
public class ArbitroController {
    private final ArbitroService arbitroService;

    public ArbitroController(ArbitroService arbitroService) {
        this.arbitroService = arbitroService;
    }

    @GetMapping
    public List<Arbitro> listarTodos() {
        return arbitroService.listarTodos();
    }

    @PostMapping
    public Arbitro criar(@RequestBody Arbitro arbitro) {
        return arbitroService.salvar(arbitro);
    }

    @PutMapping("/{id}")
    public Arbitro atualizar(@PathVariable Long id, @RequestBody Arbitro arbitro) {
        Arbitro existente = arbitroService.buscarPorId(id);
        existente.setNomeCompleto(arbitro.getNomeCompleto());
        existente.setTelefone(arbitro.getTelefone());
        return arbitroService.salvar(existente);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        arbitroService.deletar(id);
    }
}
