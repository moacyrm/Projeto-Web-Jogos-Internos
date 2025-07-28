package com.provaweb.jogosinternos.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordenador {

    @Id
    private String matricula;

    private String nome;
    private String email;
    private String senha;
    
    @ManyToOne
    private Curso curso;

}
