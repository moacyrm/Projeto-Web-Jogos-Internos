package com.provaweb.jogosinternos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.provaweb.jogosinternos.entities.Jogo;

public interface JogoRepository extends JpaRepository<Jogo, Long> {
    List<Jogo> findByGrupoId(Long grupoId);

    List<Jogo> findByEventoId(Long eventoId);

    List<Jogo> findByEventoIdAndFinalizadoFalse(Long eventoId);

    @Query("SELECT j FROM Jogo j WHERE j.evento.id = :eventoId ORDER BY j.dataHora DESC")
    List<Jogo> findByEventoIdOrderByDataHoraDesc(@Param("eventoId") Long eventoId);

    default Optional<Jogo> findTopByEventoIdOrderByDataHoraDesc(Long eventoId) {
        List<Jogo> jogos = findByEventoIdOrderByDataHoraDesc(eventoId);
        return jogos.isEmpty() ? Optional.empty() : Optional.of(jogos.get(0));
    }

    @Query("SELECT COUNT(j) FROM Jogo j WHERE j.evento.id = :eventoId AND j.finalizado = false AND j.fase IS NULL")
    long countByEventoIdAndFinalizadoFalseAndFaseIsNull(@Param("eventoId") Long eventoId);

    @Modifying
    @Query("DELETE FROM Jogo j WHERE j.evento.id = :eventoId OR j.grupo.evento.id = :eventoId")
    void deleteByEventoId(@Param("eventoId") Long eventoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Jogo j WHERE j.grupo.evento.id = :eventoId")
    void deleteByGrupoEventoId(@Param("eventoId") Long eventoId);

    List<Jogo> findByEventoIdAndFaseStartingWith(Long eventoId, String string);

    List<Jogo> findByEventoIdAndFase(Long eventoId, String fase);

    @Query("SELECT DISTINCT j.fase FROM Jogo j WHERE j.fase IS NOT NULL")
    List<String> findDistinctFases();

    @Query("SELECT COUNT(j) FROM Jogo j WHERE j.grupo.id = :grupoId AND j.finalizado = false")
    long countByGrupoIdAndFinalizadoFalse(@Param("grupoId") Long grupoId);

    List<Jogo> findByChaveEliminatoriaId(Long chaveId);

}
