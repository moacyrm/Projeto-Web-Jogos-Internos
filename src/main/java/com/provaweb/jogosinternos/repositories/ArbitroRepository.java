package com.provaweb.jogosinternos.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provaweb.jogosinternos.entities.Arbitro;

public interface ArbitroRepository extends JpaRepository<Arbitro, Long> {
    Optional<Arbitro> findByMatricula(String matricula);
}
