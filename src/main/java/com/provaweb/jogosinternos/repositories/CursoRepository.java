package com.provaweb.jogosinternos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provaweb.jogosinternos.entities.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
     boolean existsByNome(String nome);
}
