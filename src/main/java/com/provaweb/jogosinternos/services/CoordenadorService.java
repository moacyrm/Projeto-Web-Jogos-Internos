package com.provaweb.jogosinternos.services;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.provaweb.jogosinternos.dto.AtletaDTO;
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
    private final AtletaService atletaService;
    private final EquipeService equipeService;

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

    @Transactional
    public ResponseEntity<?> definirTecnico(String matriculaCoordenador, Long atletaId) {
        // Busca coordenador
        Coordenador coordenador = coordenadorRepository.findById(matriculaCoordenador)
                .orElseThrow(() -> new RuntimeException("Coordenador não encontrado"));

        // Busca atleta
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));

        // O atleta precisa estar vinculado a uma equipe
        if (atleta.getEquipe() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Atleta não está vinculado a nenhuma equipe"));
        }

        // Verifica se o coordenador possui curso associado
        if (coordenador.getCurso() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Coordenador não possui curso associado"));
        }

        // Determina o curso do atleta: preferir atleta.getCurso() se existir, senão
        // usar equipe.curso
        Long cursoAtletaId = null;
        if (atleta.getCurso() != null) {
            cursoAtletaId = atleta.getCurso().getId();
        } else if (atleta.getEquipe() != null && atleta.getEquipe().getCurso() != null) {
            cursoAtletaId = atleta.getEquipe().getCurso().getId();
        }

        if (cursoAtletaId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Não foi possível determinar o curso do atleta"));
        }

        Long cursoCoordenadorId = coordenador.getCurso().getId();
        if (!cursoCoordenadorId.equals(cursoAtletaId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Atleta não pertence ao seu curso"));
        }

        Long equipeId = atleta.getEquipe().getId();

        atletaRepository.removerStatusTecnicoDaEquipe(equipeId);

        atleta.setTecnico(true);
        atletaRepository.save(atleta);

        return ResponseEntity.ok(Map.of(
                "message", "Técnico definido com sucesso",
                "atletaId", atletaId));
    }

    public ResponseEntity<List<AtletaDTO>> listarAtletasPorCurso(String matriculaCoordenador) {
        try {
            var coordenador = coordenadorRepository.findById(matriculaCoordenador)
                    .orElseThrow(() -> new RuntimeException("Coordenador não encontrado"));

            if (coordenador.getCurso() == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            Long cursoId = coordenador.getCurso().getId();
            List<AtletaDTO> dtos = atletaService.listarAtletasDTOPorCurso(cursoId);

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<?> criarEquipePorCoordenador(String matriculaCoordenador, Equipe equipe, Long tecnicoId) {
        try {
            Equipe salva = equipeService.criarEquipePorCoordenador(matriculaCoordenador, equipe, tecnicoId);
            return ResponseEntity.ok(Map.of("message", "Equipe criada", "id", salva.getId()));
        } catch (DataIntegrityViolationException dive) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Equipe já existe para esse evento/curso/esporte"));
        } catch (ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(Map.of("error", rse.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
