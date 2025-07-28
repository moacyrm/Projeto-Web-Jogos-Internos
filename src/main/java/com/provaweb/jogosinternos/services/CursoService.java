package com.provaweb.jogosinternos.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.Curso;
import com.provaweb.jogosinternos.repositories.CursoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    public Curso criarCurso(Curso curso) {
        if (cursoRepository.existsByNome(curso.getNome())) {
            throw new RuntimeException("Já existe um curso com este nome");
        }
        return cursoRepository.save(curso);

    }

    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }

    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));
    }

    public Curso atualizarCurso(Long id, Curso curso) {
        Curso existente = buscarPorId(id);
        existente.setNome(curso.getNome());
        existente.setCampus(curso.getCampus());
        return cursoRepository.save(existente);
    }

    public void deletarCurso(Long id) {
        if (!cursoRepository.existsById(id)) {
            throw new RuntimeException("Curso não encontrado");
        }
        cursoRepository.deleteById(id);
    }
}
