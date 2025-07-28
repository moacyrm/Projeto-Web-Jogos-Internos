package com.provaweb.jogosinternos.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.Campus;
import com.provaweb.jogosinternos.repositories.CampusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampusService {
    private final CampusRepository campusRepository;

    public Campus criarCampus(Campus campus) {
        if (campusRepository.existsByNome(campus.getNome())) {
            throw new RuntimeException("Já existe um campus com este nome");
        }
        return campusRepository.save(campus);

    }

    public List<Campus> listarTodos() {
        return campusRepository.findAll();
    }

    public Campus buscarPorId(Long id) {
        return campusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campus não encontrado"));
    }

    public Campus atualizarCampus(Long id, Campus campus) {
        Campus existente = buscarPorId(id);
        existente.setNome(campus.getNome());
        return campusRepository.save(existente);
    }

    public void deletarCampus(Long id) {
        if (!campusRepository.existsById(id)) {
            throw new RuntimeException("Campus não encontrado");
        }
        campusRepository.deleteById(id);
    }
}
