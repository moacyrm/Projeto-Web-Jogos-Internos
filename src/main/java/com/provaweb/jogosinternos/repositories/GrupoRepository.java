package com.provaweb.jogosinternos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.provaweb.jogosinternos.entities.Grupo;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    List<Grupo> findByEventoId(Long eventoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Grupo g WHERE g.evento.id = :eventoId")
    void deleteByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT g FROM Grupo g LEFT JOIN FETCH g.equipes WHERE g.id = :id")
    Grupo findByIdWithEquipes(@Param("id") Long id);

    @Query("SELECT DISTINCT g FROM Grupo g LEFT JOIN FETCH g.jogos WHERE g.evento.id = :eventoId")
    List<Grupo> findByEventoIdWithJogos(@Param("eventoId") Long eventoId);

}
