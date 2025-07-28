package com.provaweb.jogosinternos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provaweb.jogosinternos.entities.Evento;
import com.provaweb.jogosinternos.entities.TipoEvento;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByTipoEvento(TipoEvento tipoEvento);
}
