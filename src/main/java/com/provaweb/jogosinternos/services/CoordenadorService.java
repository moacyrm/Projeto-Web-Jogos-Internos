package com.provaweb.jogosinternos.services;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.Atleta;
import com.provaweb.jogosinternos.entities.Coordenador;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.repositories.AtletaRepository;
import com.provaweb.jogosinternos.repositories.CoordenadorRepository;
import com.provaweb.jogosinternos.repositories.EquipeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoordenadorService {

    private final AtletaRepository atletaRepository;
    private final CoordenadorRepository coordenadorRepository;
    private final EquipeRepository equipeRepository;

    public Coordenador cadastrarCoordenador(Coordenador coordenador) {
        if (coordenadorRepository.existsById(coordenador.getMatricula())) {
            throw new RuntimeException("Já existe um coordenador com essa matrícula.");
        }

        String senhaGerada = gerarSenhaAleatoria(8);
        coordenador.setSenha(senhaGerada);

        Coordenador salvo = coordenadorRepository.save(coordenador);

        System.out.println("Email com senha enviado para o email do coordenador:");
        return salvo;
    }

    private String gerarSenhaAleatoria(int tamanho) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder senha = new StringBuilder();

        for (int i = 0; i < tamanho; i++) {
            senha.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return senha.toString();
    }

    public Coordenador buscarPorId(String matricula) {
        return coordenadorRepository.findById(matricula)
                .orElseThrow(() -> new RuntimeException("Coordenador não encontrado."));
    }

    public List<Coordenador> listarTodos() {
        return coordenadorRepository.findAll();
    }

    public Coordenador atualizarCoordenador(String matricula, Coordenador coordenadorAtualizado) {
        Coordenador existente = buscarPorId(matricula);
        existente.setNome(coordenadorAtualizado.getNome());
        existente.setEmail(coordenadorAtualizado.getEmail());
        existente.setSenha(coordenadorAtualizado.getSenha());

        return coordenadorRepository.save(existente);
    }

    public void deletarCoordenador(String matricula) {
        if (!coordenadorRepository.existsById(matricula)) {
            throw new RuntimeException("Coordenador não encontrado.");
        }
        coordenadorRepository.deleteById(matricula);
    }

    public List<Equipe> listarEquipesPorCoordenador(String matricula) {
        Coordenador coordenador = buscarPorId(matricula);

        if (coordenador.getCurso() == null) {
            throw new RuntimeException("Coordenador não possui curso associado.");
        }

        return equipeRepository.findByCursoId(coordenador.getCurso().getId());
    }

    public ResponseEntity<?> definirTecnico(String matriculaCoordenador, Long atletaId) {
        Coordenador coordenador = coordenadorRepository.findById(matriculaCoordenador)
                .orElseThrow(() -> new RuntimeException("Coordenador não encontrado"));

        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));

        // Verifica se pertencem ao mesmo curso
        if (coordenador.getCurso() == null || atleta.getCurso() == null ||
                !coordenador.getCurso().getId().equals(atleta.getCurso().getId())) {
            return ResponseEntity.status(403).body("Atleta não pertence ao seu curso");
        }

        // Remove status de técnico de outros atletas
        atletaRepository.removerStatusTecnicoDoCurso(coordenador.getCurso().getId());

        // Define novo técnico
        atleta.setTecnico(true);
        atletaRepository.save(atleta);

        return ResponseEntity.ok(Map.of(
                "message", "Técnico definido com sucesso",
                "atletaId", atletaId));
    }

    public ResponseEntity<List<Atleta>> listarAtletasPorCurso(String matriculaCoordenador) {
        try {
            Coordenador coordenador = coordenadorRepository.findById(matriculaCoordenador)
                    .orElseThrow(() -> new RuntimeException("Coordenador não encontrado"));

            if (coordenador.getCurso() == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<Atleta> atletas = atletaRepository.findByCursoId(coordenador.getCurso().getId());
            return ResponseEntity.ok(atletas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
