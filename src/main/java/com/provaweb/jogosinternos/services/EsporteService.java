package com.provaweb.jogosinternos.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.Esporte;
import com.provaweb.jogosinternos.repositories.EsporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EsporteService {

    private final EsporteRepository esporteRepository;

    public Esporte criarEsporte(Esporte esporte) {
        validarLimites(esporte);
        if (esporteRepository.existsByNome(esporte.getNome())) {
            throw new RuntimeException("Já existe um esporte com este nome");
        }
        return esporteRepository.save(esporte);

    }

    public List<Esporte> listarTodos() {
        return esporteRepository.findAll();
    }

    public Esporte buscarPorId(Long id) {
        return esporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Esporte não encontrado"));
    }

    public Esporte atualizarEsporte(Long id, Esporte esporte) {
        validarLimites(esporte);
        Esporte existente = buscarPorId(id);
        existente.setNome(esporte.getNome());
        existente.setMaximoAtletas(esporte.getMaximoAtletas());
        existente.setMinimoAtletas(esporte.getMinimoAtletas());
        return esporteRepository.save(existente);
    }

    public void deletarEsporte(Long id) {
        if (!esporteRepository.existsById(id)) {
            throw new RuntimeException("Esporte não encontrado");
        }
        esporteRepository.deleteById(id);
    }

    private void validarLimites(Esporte esporte) {
        int min = esporte.getMinimoAtletas();
        int max = esporte.getMaximoAtletas();

        if (min <= 0 || max <= 0) {
            throw new RuntimeException("O mínimo e o máximo de atletas devem ser maiores que zero");
        }

        if (min > max) {
            throw new RuntimeException("O mínimo de atletas não pode ser maior que o máximo");
        }
    }
}
