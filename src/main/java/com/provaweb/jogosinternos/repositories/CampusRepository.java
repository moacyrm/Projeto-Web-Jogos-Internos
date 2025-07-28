package com.provaweb.jogosinternos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provaweb.jogosinternos.entities.Campus;

public interface CampusRepository extends JpaRepository<Campus, Long> {
    boolean existsByNome(String nome);
}