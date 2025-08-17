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
import org.springframework.web.bind.annotation.RestController;

import com.provaweb.jogosinternos.entities.Evento;
import com.provaweb.jogosinternos.entities.TipoEvento;
import com.provaweb.jogosinternos.services.EventoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    public Evento criar(@RequestBody Evento evento) {
        return eventoService.criarEvento(evento);
    }

    @GetMapping
    public List<Evento> listarTodos() {
        return eventoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Evento buscarPorId(@PathVariable Long id) {
        return eventoService.buscarPorId(id);
    }

    @GetMapping("/portipo/{tipo}")
    public List<Evento> listarPorTipo(@PathVariable TipoEvento tipo) {
        return eventoService.listarPorTipo(tipo);
    }

    @PutMapping("/{id}")
    public Evento atualizar(@PathVariable Long id, @RequestBody Evento evento) {
        return eventoService.atualizarEvento(id, evento);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        eventoService.deletarEvento(id);
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Evento>> listarEventosDisponiveis() {
        List<Evento> eventos = eventoService.listarEventosDisponiveis();
        return ResponseEntity.ok(eventos);
    }

    @PostMapping("/{eventoId}/inscrever-equipe/{equipeId}")
    public ResponseEntity<Void> inscreverEquipe(
            @PathVariable Long eventoId,
            @PathVariable Long equipeId) {
        eventoService.inscreverEquipe(eventoId, equipeId);
        return ResponseEntity.ok().build();
    }

}
