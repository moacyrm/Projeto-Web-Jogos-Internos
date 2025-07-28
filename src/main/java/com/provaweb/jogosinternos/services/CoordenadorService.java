package com.provaweb.jogosinternos.services;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.Coordenador;
import com.provaweb.jogosinternos.entities.Equipe;
import com.provaweb.jogosinternos.repositories.CoordenadorRepository;
import com.provaweb.jogosinternos.repositories.EquipeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoordenadorService {

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
}
