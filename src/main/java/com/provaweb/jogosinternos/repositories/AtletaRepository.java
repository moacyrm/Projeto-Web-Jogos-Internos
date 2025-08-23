package com.provaweb.jogosinternos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.provaweb.jogosinternos.entities.Atleta;

public interface AtletaRepository extends JpaRepository<Atleta, Long> {
    Atleta findByIdAndTecnicoTrue(Long id);

    long countByEquipeId(Long equipeId);

    boolean existsByNomeCompleto(String nomeCompleto);

    boolean existsByEquipeIdAndTecnicoTrue(Long equipeId);

    List<Atleta> findByEquipeId(Long equipeId);

    Optional<Atleta> findByIdAndEquipeId(Long atletaId, Long equipeId);

    Optional<Atleta> findByMatricula(String matricula);

    Optional<Atleta> findByMatriculaIgnoreCase(String matricula);

    @Query("SELECT a FROM Atleta a WHERE a.equipe.id = :equipeId")
    List<Atleta> findByEquipeIdWithDetails(@Param("equipeId") Long equipeId);

    @Query("SELECT a FROM Atleta a LEFT JOIN FETCH a.equipe e LEFT JOIN FETCH e.esporte WHERE a.matricula = :matricula")
    Optional<Atleta> findByMatriculaWithEquipe(@Param("matricula") String matricula);

    List<Atleta> findByEquipeIsNull();

    @Modifying
    @Query("UPDATE Atleta a SET a.tecnico = false WHERE a.curso.id = :cursoId")
    void removerStatusTecnicoDoCurso(Long cursoId);

    List<Atleta> findByCursoId(Long cursoId);

    @Query("SELECT DISTINCT a FROM Atleta a JOIN a.equipe e WHERE e.curso.id = :cursoId AND a.equipe IS NOT NULL")
    List<Atleta> findAtletasPorCursoIdComEquipe(@Param("cursoId") Long cursoId);

    @Modifying
    @Query("UPDATE Atleta a SET a.tecnico = false WHERE a.equipe.id = :equipeId")
    void removerStatusTecnicoDaEquipe(@Param("equipeId") Long equipeId);

    @Query("SELECT a FROM Atleta a WHERE a.curso.id = :cursoId AND a NOT IN " +
            "(SELECT atl FROM Equipe e JOIN e.atletas atl WHERE e.evento.id = :eventoId)")
    List<Atleta> findByCursoIdAndNotInEvento(@Param("cursoId") Long cursoId, @Param("eventoId") Long eventoId);
}
