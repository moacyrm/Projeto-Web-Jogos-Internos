package com.provaweb.jogosinternos.services;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.Arbitro;
import com.provaweb.jogosinternos.repositories.ArbitroRepository;

import java.util.List;

@Service
public class ArbitroService {
    private final ArbitroRepository arbitroRepository;

    public ArbitroService(ArbitroRepository arbitroRepository) {
        this.arbitroRepository = arbitroRepository;
    }

    public List<Arbitro> listarTodos() {
        return arbitroRepository.findAll();
    }

    public Arbitro buscarPorId(Long id) {
        return arbitroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Árbitro não encontrado"));
    }

    public Arbitro salvar(Arbitro arbitro) {
        return arbitroRepository.save(arbitro);
    }

    public void deletar(Long id) {
        arbitroRepository.deleteById(id);
    }
}
