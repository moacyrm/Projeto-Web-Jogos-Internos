package com.provaweb.jogosinternos.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.provaweb.jogosinternos.dto.ClassificacaoDTO;
import com.provaweb.jogosinternos.dto.GrupoDTO;
import com.provaweb.jogosinternos.entities.Grupo;
import com.provaweb.jogosinternos.entities.Jogo;
import com.provaweb.jogosinternos.services.GrupoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
@Slf4j
public class GrupoController {
    private final GrupoService grupoService;

    @Transactional
    @PostMapping("/sortear/{eventoId}")
    public ResponseEntity<?> sortearGrupos(@PathVariable Long eventoId) {
        try {
            List<Grupo> grupos = grupoService.sortearGrupo(eventoId);
            List<GrupoDTO> gruposDTO = grupos.stream()
                    .map(grupo -> grupoService.montarGrupoDTOComConfrontos(grupo))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(gruposDTO);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Erro ao sortear grupos para evento " + eventoId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro interno ao sortear grupos", "detail", ex.getMessage()));
        }
    }

    @GetMapping("/eventos/{eventoId}")
    public ResponseEntity<List<GrupoDTO>> listarTodos(@PathVariable Long eventoId) {
        List<Grupo> grupos = grupoService.listarTodosComJogos(eventoId);
        List<GrupoDTO> gruposDTO = grupos.stream()
                .map(grupo -> grupoService.montarGrupoDTOComConfrontos(grupo))
                .collect(Collectors.toList());
        return ResponseEntity.ok(gruposDTO);
    }

    @PostMapping("/{grupoId}/confrontos")
    public List<Jogo> criarConfrontos(@PathVariable Long grupoId) {
        Grupo grupo = grupoService.buscarGrupoComEquipes(grupoId);
        grupo.setId(grupoId);
        return grupoService.criarConfronto(grupo);
    }

    @DeleteMapping("/evento/{eventoId}")
    public void deletarGruposDoEvento(@PathVariable Long eventoId) {
        grupoService.deletarGruposDoEvento(eventoId);
    }

    @DeleteMapping("/eventos/{eventoId}/limpar")
    public ResponseEntity<Void> limparGrupos(@PathVariable Long eventoId) {
        grupoService.limparGruposDoEvento(eventoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/eventos/{eventoId}/podio")
    public ResponseEntity<List<ClassificacaoDTO>> getPodioFinal(@PathVariable Long eventoId) {
        List<ClassificacaoDTO> podio = grupoService.getClassificacaoFinal(eventoId);
        return ResponseEntity.ok(podio);
    }
}
