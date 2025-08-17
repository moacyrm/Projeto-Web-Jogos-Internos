package com.provaweb.jogosinternos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.provaweb.jogosinternos.entities.Atleta;
import com.provaweb.jogosinternos.entities.Equipe;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    List<Equipe> findByEventoId(Long eventoId);

    Optional<Equipe> findByNomeAndEventoId(String nome, Long eventoId);

    boolean existsByEventoIdAndNome(Long eventoId, String nome);

    boolean existsByEventoIdAndCursoId(Long eventoId, Long cursoId);

    @Modifying
    @Transactional
    @Query("UPDATE Equipe e SET e.grupo = null WHERE e.evento.id = :eventoId")
    void desvincularEquipesDeGruposPorEvento(@Param("eventoId") Long eventoId);

    @Query("select e from Equipe e where e.grupo.evento.id = :eventoId")
    List<Equipe> findByEventoEventoId(@Param("eventoId") Long eventoId);

    Optional<Equipe> findByNome(String nome);

    List<Equipe> findByCursoId(Long cursoId);

    @Query("SELECT a FROM Atleta a LEFT JOIN FETCH a.equipe WHERE a.matricula = :matricula")
    Optional<Atleta> findByMatriculaWithEquipe(@Param("matricula") String matricula);

    boolean existsByEventoIdAndCursoIdAndEsporteId(Long eventoId, Long cursoId, Long esporteId);

}
