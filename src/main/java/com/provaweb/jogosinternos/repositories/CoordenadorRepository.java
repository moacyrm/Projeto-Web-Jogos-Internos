package com.provaweb.jogosinternos.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provaweb.jogosinternos.entities.Coordenador;

public interface CoordenadorRepository extends JpaRepository<Coordenador, String> {
    boolean existsByEmail(String email);
    
    Optional<Coordenador> findByMatricula(String matricula);
    Optional<Coordenador> findByMatriculaIgnoreCase(String matricula);

}
