package com.provaweb.jogosinternos.controllers;

import com.provaweb.jogosinternos.dto.LoginRequestDTO;
import com.provaweb.jogosinternos.entities.Admin;
import com.provaweb.jogosinternos.entities.Atleta;
import com.provaweb.jogosinternos.entities.Coordenador;
import com.provaweb.jogosinternos.repositories.AdminRepository;
import com.provaweb.jogosinternos.repositories.AtletaRepository;
import com.provaweb.jogosinternos.repositories.CoordenadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AtletaRepository atletaRepository;
    private final CoordenadorRepository coordenadorRepository;
    private final AdminRepository adminRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO login) {

        String matricula = login.getMatricula().trim().toUpperCase();
        String senha = login.getSenha().trim();

        Atleta atleta = atletaRepository.findByMatriculaIgnoreCase(matricula).orElse(null);
        if (atleta != null && senha.equals(atleta.getSenha().trim())) {
            return ResponseEntity.ok(Map.of(
                    "tipo", "ATLETA",
                    "id", atleta.getId(),
                    "matricula", atleta.getMatricula(),
                    "nome", atleta.getNomeCompleto(),
                    "tecnico", atleta.isTecnico()));
        }

        // Buscar coordenador ignorando case
        Coordenador coord = coordenadorRepository.findByMatriculaIgnoreCase(matricula).orElse(null);
        if (coord != null && senha.equals(coord.getSenha().trim())) {
            // Retornar dados essenciais
            Map<String, Object> response = new HashMap<>();
            response.put("tipo", "COORDENADOR");
            response.put("id", coord.getMatricula());
            response.put("matricula", coord.getMatricula());
            response.put("nome", coord.getNome());

            // Adicionar curso se existir
            if (coord.getCurso() != null) {
                response.put("cursoId", coord.getCurso().getId());
            }

            return ResponseEntity.ok(response);
        }

        Admin admin = adminRepository.findByMatriculaIgnoreCase(matricula).orElse(null);
        if (admin != null && senha.equals(admin.getSenha() == null ? "" : admin.getSenha().trim())) {
            Map<String, Object> response = new HashMap<>();
            response.put("tipo", "ADMIN");
            response.put("id", admin.getId());
            response.put("matricula", admin.getMatricula());
            response.put("nome", admin.getNome());
            // não retorne senha ou campos sensíveis
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
    }
}