package com.provaweb.jogosinternos.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.provaweb.jogosinternos.entities.Equipe;
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

    @Query("SELECT j FROM Jogo j " +
            "WHERE j.equipe1.id = :equipeId OR j.equipe2.id = :equipeId")
    List<Jogo> findByEquipeId(@Param("equipeId") Long equipeId);

    @Query("""
                SELECT j FROM Jogo j
                WHERE j.equipe1.id IN (
                    SELECT a.equipe.id FROM Atleta a WHERE a.matricula = :matricula
                )
                OR j.equipe2.id IN (
                    SELECT a.equipe.id FROM Atleta a WHERE a.matricula = :matricula
                )
                ORDER BY j.dataHora ASC
            """)
    List<Jogo> findByAtletaMatricula(@Param("matricula") String matricula);

    @Query("SELECT j FROM Jogo j WHERE " +
            "(j.equipe1.id = :equipeId OR j.equipe2.id = :equipeId) " +
            "AND j.dataHora > :agora " +
            "ORDER BY j.dataHora ASC")
    List<Jogo> findProximosJogosPorEquipeId(@Param("equipeId") Long equipeId,
            @Param("agora") LocalDateTime agora);

    @Query("SELECT j FROM Jogo j LEFT JOIN FETCH j.equipe1 LEFT JOIN FETCH j.equipe2 WHERE j.equipe1 = :equipe1 OR j.equipe2 = :equipe2")
    List<Jogo> findByEquipe1OrEquipe2WithDetails(@Param("equipe1") Equipe equipe1, @Param("equipe2") Equipe equipe2);

    @Query("SELECT DISTINCT j FROM Jogo j " +
            "LEFT JOIN FETCH j.equipe1 e1 " +
            "LEFT JOIN FETCH j.equipe2 e2 " +
            "WHERE e1.curso.id = :cursoId OR e2.curso.id = :cursoId")
    List<Jogo> findByCursoId(@Param("cursoId") Long cursoId);

}
