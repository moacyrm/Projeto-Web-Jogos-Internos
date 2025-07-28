package com.provaweb.jogosinternos.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
public class AtletaService {
    private final AtletaRepository atletaRepository;
    private final CoordenadorRepository coordenadorRepository;
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
        existente.setNomeCompleto(atleta.getNomeCompleto());
        existente.setApelido(atleta.getApelido());
        existente.setMatricula(atleta.getMatricula());
        existente.setTelefone(atleta.getTelefone());
        existente.setSenha(atleta.getSenha());
        existente.setTecnico(atleta.isTecnico());
        existente.setEquipe(atleta.getEquipe());
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

    public void definirTecnico(Long atletaId, String coordenadorId) {
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrado"));

        Coordenador coordenador = coordenadorRepository.findById(coordenadorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Coordenador não encontrado"));

        if (atleta.getCurso() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Atleta não possui curso definido.");
        }

        if (!coordenador.getCurso().getId().equals(atleta.getCurso().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Coordenador não autorizado para definir técnico neste curso.");
        }

        if (atleta.getEquipe() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Atleta não está associado a uma equipe.");
        }

        Long equipeId = atleta.getEquipe().getId();
        boolean existeTecnico = atletaRepository.existsByEquipeIdAndTecnicoTrue(equipeId);
        if (existeTecnico) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipe já possui um técnico");
        }

        atleta.setTecnico(true);
        atletaRepository.save(atleta);
    }

    public boolean isTecnico(Long atletaId) {
        Atleta tecnico = atletaRepository.findByIdAndTecnicoTrue(atletaId);
        return tecnico != null;
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
}
