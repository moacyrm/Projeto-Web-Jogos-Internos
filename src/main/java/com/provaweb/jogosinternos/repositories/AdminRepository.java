package com.provaweb.jogosinternos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.provaweb.jogosinternos.entities.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByMatricula(String matricula);

    Optional<Admin> findByMatriculaIgnoreCase(String matricula);

    boolean existsByMatricula(String matricula);
}
