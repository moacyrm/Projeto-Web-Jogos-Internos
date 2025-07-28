package com.provaweb.jogosinternos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provaweb.jogosinternos.entities.Esporte;

public interface EsporteRepository extends JpaRepository<Esporte, Long> {
    boolean existsByNome(String nome);

}
