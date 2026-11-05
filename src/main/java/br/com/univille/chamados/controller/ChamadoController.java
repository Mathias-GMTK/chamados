package br.com.univille.chamados.controller;

import br.com.univille.chamados.dto.*;
import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;
import br.com.univille.chamados.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<ChamadoResponse> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return new PagedModel<>(service.listar(status, prioridade, clienteId, pageable));
    }

    @GetMapping("/{id}")
    public ChamadoDetalheResponse buscar(@PathVariable Long id) {
        return service.buscarDetalhe(id);
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(@Valid @RequestBody ChamadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PutMapping("/{id}")
    public ChamadoResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody ChamadoRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/atribuir")
    public ChamadoResponse atribuir(@PathVariable Long id,
                                    @Valid @RequestBody AtribuirTecnicoRequest request) {
        return service.atribuir(id, request.tecnicoId());
    }

    @PatchMapping("/{id}/status")
    public ChamadoResponse alterarStatus(@PathVariable Long id,
                                         @Valid @RequestBody AlterarStatusRequest request) {
        return service.alterarStatus(id, request.status());
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(
            @PathVariable Long id, @Valid @RequestBody ComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.adicionarComentario(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}