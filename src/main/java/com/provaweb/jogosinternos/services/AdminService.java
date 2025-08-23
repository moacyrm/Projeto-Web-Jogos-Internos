package com.provaweb.jogosinternos.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.provaweb.jogosinternos.entities.Admin;
import com.provaweb.jogosinternos.repositories.AdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    public Admin criarAdmin(Admin admin) {
        if (admin.getMatricula() == null || admin.getSenha() == null) {
            throw new RuntimeException("matricula e senha são obrigatórios");
        }
        if (adminRepository.existsByMatricula(admin.getMatricula())) {
            throw new RuntimeException("Já existe um admin com esse matricula");
        }
        return adminRepository.save(admin);
    }

    public List<Admin> listarAdmins() {
        return adminRepository.findAll();
    }

    public Admin buscarPorId(Long id) {
        return adminRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin não encontrado"));
    }

    public Admin buscarPorMatricula(String matricula) {
        return adminRepository.findByMatriculaIgnoreCase(matricula)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado com matrícula: " + matricula));
    }

    public boolean existePorMatricula(String matricula) {
        return adminRepository.existsByMatricula(matricula);
    }

    public void deletarAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new RuntimeException("Admin não encontrado");
        }
        adminRepository.deleteById(id);
    }
}
