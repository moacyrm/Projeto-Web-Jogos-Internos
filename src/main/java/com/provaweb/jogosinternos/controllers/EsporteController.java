package com.provaweb.jogosinternos.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.provaweb.jogosinternos.entities.Esporte;
import com.provaweb.jogosinternos.services.EsporteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/esporte")
@RequiredArgsConstructor
public class EsporteController {
    private final EsporteService esporteService;

    @PostMapping
    public Esporte criar(@RequestBody Esporte esporte) {
        return esporteService.criarEsporte(esporte);
    }

    @GetMapping
    public List<Esporte> listarTodos() {
        return esporteService.listarTodos();
    }

    @GetMapping("/{id}")
    public Esporte buscarPorId(@PathVariable Long id) {
        return esporteService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Esporte atualizar(@PathVariable Long id, @RequestBody Esporte esporte) {
        return esporteService.atualizarEsporte(id, esporte);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        esporteService.deletarEsporte(id);
    }
}
