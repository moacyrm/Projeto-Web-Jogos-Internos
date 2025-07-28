package com.provaweb.jogosinternos.services;

import com.provaweb.jogosinternos.dto.ChaveDTO;
import com.provaweb.jogosinternos.entities.ChaveEliminatoria;
import com.provaweb.jogosinternos.entities.Jogo;
import com.provaweb.jogosinternos.repositories.ChaveRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChaveService {

    private final ChaveRepository chaveRepository;

    public ChaveDTO buscarChaveDTO(Long id) {
        ChaveEliminatoria chave = chaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chave não encontrada"));

        List<Jogo> jogos = chave.getJogos();
        return chave.toDTO(jogos);
    }

}