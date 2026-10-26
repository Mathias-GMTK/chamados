package br.com.univille.chamados.controller;

import br.com.univille.chamados.dto.TecnicoRequest;
import br.com.univille.chamados.dto.TecnicoResponse;
import br.com.univille.chamados.service.TecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tecnicos")
public class TecnicoController {

    private final TecnicoService service;

    public TecnicoController(TecnicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TecnicoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public TecnicoResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<TecnicoResponse> criar(@Valid @RequestBody TecnicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PutMapping("/{id}")
    public TecnicoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TecnicoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}