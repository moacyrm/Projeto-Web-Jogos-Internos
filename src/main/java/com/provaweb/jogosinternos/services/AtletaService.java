package com.provaweb.jogosinternos.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.provaweb.jogosinternos.dto.AtletaDTO;
import com.provaweb.jogosinternos.entities.Atleta;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.repositories.AtletaRepository;
import com.provaweb.jogosinternos.repositories.EquipeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtletaService {
    private final AtletaRepository atletaRepository;
    private final EquipeRepository equipeRepository;

    public Atleta cadastrarAtleta(Atleta atleta) {
        if (atleta.getEquipe() != null) {
            long quantidade = atletaRepository.countByEquipeId(atleta.getEquipe().getId());

            int max = atleta.getEquipe().getEsporte().getMaximoAtletas();
            if (quantidade >= max) {
                throw new RuntimeException("Equipe já atingiu o número máximo de atletas para este esporte.");
            }
        }
        return atletaRepository.save(atleta);
    }

    public Atleta buscarPorId(Long id) {
        return atletaRepository.findById(id).orElseThrow(() -> new RuntimeException("atleta nao encontrado"));
    }

    public List<Atleta> buscarPorEquipe(Long equipeId) {
        return atletaRepository.findByEquipeId(equipeId);
    }

    public Atleta buscarAtletaNaEquipe(Long atletaId, Long equipeId) {
        return atletaRepository.findByIdAndEquipeId(atletaId, equipeId)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado na equipe"));
    }

    public List<Atleta> listarTodos() {
        return atletaRepository.findAll();
    }

    public Atleta atualizarAtleta(Long id, Atleta atleta) {
        Atleta existente = buscarPorId(id);

        // Atualiza apenas os campos não nulos
        if (atleta.getNomeCompleto() != null) {
            existente.setNomeCompleto(atleta.getNomeCompleto());
        }
        if (atleta.getApelido() != null) {
            existente.setApelido(atleta.getApelido());
        }
        if (atleta.getMatricula() != null) {
            existente.setMatricula(atleta.getMatricula());
        }
        if (atleta.getTelefone() != null) {
            existente.setTelefone(atleta.getTelefone());
        }
        if (atleta.getSenha() != null && !atleta.getSenha().isEmpty()) {
            existente.setSenha(atleta.getSenha());
        }

        existente.setTecnico(atleta.isTecnico());

        if (atleta.getEquipe() != null) {
            existente.setEquipe(atleta.getEquipe());
        }

        return atletaRepository.save(existente);
    }

    public void deletarAtleta(Long id) {
        Atleta atleta = buscarPorId(id);
        if (atleta.isTecnico()) {
            throw new RuntimeException("Não é possível remover um técnico");
        }
        if (!atletaRepository.existsById(id)) {
            throw new RuntimeException("atleta não encontrado");
        }
        atletaRepository.deleteById(id);
    }

    public Atleta definirComoTecnico(Long atletaId) {
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado"));

        if (atleta.getEquipe() == null) {
            throw new RuntimeException("Atleta não está vinculado a uma equipe");
        }

        Long equipeId = atleta.getEquipe().getId();
        atletaRepository.removerStatusTecnicoDaEquipe(equipeId);

        atleta.setTecnico(true);
        return atletaRepository.save(atleta);
    }

    public boolean isTecnico(Long atletaId) {
        return atletaRepository.findById(atletaId)
                .map(Atleta::isTecnico)
                .orElse(false);
    }

    public Atleta atualizarEquipe(Long atletaId, Long equipeId, Long tecnicoId) {
        Atleta atleta = buscarPorId(atletaId);

        if (tecnicoId != null) {
            Atleta tecnico = buscarPorId(tecnicoId);

            if (!tecnico.isTecnico() || !tecnico.getEquipe().getId().equals(equipeId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o técnico pode gerenciar sua equipe");
            }
        }

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrada"));

        atleta.setEquipe(equipe);
        return atualizarAtleta(atletaId, atleta);
    }

    public AtletaDTO buscarAtletaDTO(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));
        return atleta.toDTO();
    }

    public Atleta buscarPorMatricula(String matricula) {
        return atletaRepository.findByMatriculaWithEquipe(matricula)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado com a matrícula: " + matricula));
    }

    public List<Atleta> findAtletasSemEquipe() {
        return atletaRepository.findByEquipeIsNull();
    }

    public List<Atleta> listarAtletasPorCursoSemDuplicatas(Long cursoId) {

        List<Atleta> atletas = atletaRepository.findAtletasPorCursoIdComEquipe(cursoId);

        if (atletas == null || atletas.isEmpty()) {
            return new ArrayList<>();
        }
        LinkedHashMap<String, Atleta> mapa = new LinkedHashMap<>();
        for (Atleta a : atletas) {
            String key = a.getMatricula() != null ? a.getMatricula() : "id:" + a.getId();
            mapa.putIfAbsent(key, a);
        }

        return new ArrayList<>(mapa.values());
    }

    public List<AtletaDTO> listarAtletasDTOPorCurso(Long cursoId) {
        List<Atleta> atletas = atletaRepository.findByCursoId(cursoId);

        return atletas.stream()
                .map(atleta -> {
                    AtletaDTO dto = new AtletaDTO();
                    dto.setId(atleta.getId());
                    dto.setNomeCompleto(atleta.getNomeCompleto());
                    dto.setMatricula(atleta.getMatricula());
                    dto.setApelido(atleta.getApelido());
                    dto.setTecnico(atleta.isTecnico());
                    dto.setTelefone(atleta.getTelefone());
                    // Adicione outros campos necessários
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
