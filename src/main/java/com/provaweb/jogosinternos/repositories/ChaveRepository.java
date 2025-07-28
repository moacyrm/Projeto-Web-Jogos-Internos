package com.provaweb.jogosinternos.repositories;

import com.provaweb.jogosinternos.entities.ChaveEliminatoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChaveRepository extends JpaRepository<ChaveEliminatoria, Long> {

    @Query("SELECT c FROM ChaveEliminatoria c JOIN FETCH c.jogos WHERE c.id = :id")
    ChaveEliminatoria findByIdWithJogos(@Param("id") Long id);

    @Query("SELECT c FROM ChaveEliminatoria c LEFT JOIN FETCH c.jogos WHERE c.evento.id = :eventoId")
    Optional<ChaveEliminatoria> findByEventoIdWithJogos(@Param("eventoId") Long eventoId);

    Optional<ChaveEliminatoria> findByEventoId(Long eventoId);

    boolean existsByEventoId(Long eventoId);

}