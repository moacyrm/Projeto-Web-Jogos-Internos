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

import com.provaweb.jogosinternos.entities.Campus;
import com.provaweb.jogosinternos.services.CampusService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/campus")
@RequiredArgsConstructor
public class CampusController {
    private final CampusService campusService;

    @PostMapping
    public Campus criar(@RequestBody Campus campus) {
        return campusService.criarCampus(campus);
    }

    @GetMapping
    public List<Campus> listarTodos() {
        return campusService.listarTodos();
    }

    @GetMapping("/{id}")
    public Campus buscarPorId(@PathVariable Long id) {
        return campusService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Campus atualizar(@PathVariable Long id, @RequestBody Campus campus) {
        return campusService.atualizarCampus(id, campus);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        campusService.deletarCampus(id);
    }
}
