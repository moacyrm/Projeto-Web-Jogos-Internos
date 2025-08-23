package com.provaweb.jogosinternos.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.provaweb.jogosinternos.entities.Admin;
import com.provaweb.jogosinternos.entities.Evento;
import com.provaweb.jogosinternos.dto.ChaveDTO;
import com.provaweb.jogosinternos.services.AdminService;
import com.provaweb.jogosinternos.services.EventoService;
import com.provaweb.jogosinternos.services.EliminatoriaService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final EventoService eventoService;
    private final EliminatoriaService eliminatoriaService;

    @PostMapping("/admins")
    public ResponseEntity<Admin> criarAdmin(@RequestBody Admin admin) {
        Admin criado = adminService.criarAdmin(admin);
        return ResponseEntity.ok(criado);
    }

    @GetMapping("/admins")
    public ResponseEntity<List<Admin>> listarAdmins() {
        return ResponseEntity.ok(adminService.listarAdmins());
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<?> deletarAdmin(@PathVariable Long id) {
        adminService.deletarAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/eventos")
    public ResponseEntity<Evento> criarEvento(
            @RequestParam String matriculaAdmin,
            @RequestBody Evento evento) {
        Evento criado = eventoService.criarEvento(evento, matriculaAdmin);
        return ResponseEntity.ok(criado);
    }

    @GetMapping("/eventos")
    public ResponseEntity<List<Evento>> listarEventos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @DeleteMapping("/eventos/{id}")
    public ResponseEntity<?> deletarEvento(@PathVariable Long id) {
        eventoService.deletarEvento(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/eventos/{eventoId}/gerar-eliminatoria")
    public ResponseEntity<?> gerarEliminatoria(
            @PathVariable Long eventoId,
            @RequestParam String matriculaAdmin) {
        // valida admin
        adminService.buscarPorMatricula(matriculaAdmin); // lançará se inválida

        try {
            ChaveDTO chave = eliminatoriaService.gerarChaveDTO(eventoId);
            return ResponseEntity.ok(chave);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}
