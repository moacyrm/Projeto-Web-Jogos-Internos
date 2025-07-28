package com.provaweb.jogosinternos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provaweb.jogosinternos.entities.Atleta;

public interface AtletaRepository extends JpaRepository<Atleta, Long> {
    Atleta findByIdAndTecnicoTrue(Long id);

    long countByEquipeId(Long equipeId);

    boolean existsByNomeCompleto(String nomeCompleto);

    boolean existsByEquipeIdAndTecnicoTrue(Long equipeId);

    List<Atleta> findByEquipeId(Long equipeId);

    Optional<Atleta> findByIdAndEquipeId(Long atletaId, Long equipeId);
}
