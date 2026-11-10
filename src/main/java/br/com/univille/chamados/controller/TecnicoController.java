package br.com.univille.chamados.controller;

import br.com.univille.chamados.dto.TecnicoRequest;
import br.com.univille.chamados.dto.TecnicoResponse;
import br.com.univille.chamados.exception.ErroResposta;
import br.com.univille.chamados.service.TecnicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Técnicos")
@RestController
@RequestMapping("/tecnicos")
public class TecnicoController {

    private final TecnicoService service;

    public TecnicoController(TecnicoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar técnicos")
    @GetMapping
    public List<TecnicoResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Buscar técnico por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico encontrado"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/{id}")
    public TecnicoResponse buscar(@Parameter(description = "ID do técnico", example = "1")
                                    @PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Cadastrar técnico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Técnico cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um técnico com esse e-mail",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping
    public ResponseEntity<TecnicoResponse> criar(@Valid @RequestBody TecnicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @Operation(summary = "Atualizar técnico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um técnico com esse e-mail",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/{id}")
    public TecnicoResponse atualizar(@Parameter(description = "ID do técnico", example = "1")
                                       @PathVariable Long id,
                                       @Valid @RequestBody TecnicoRequest request) {
        return service.atualizar(id, request);
    }

    @Operation(summary = "Excluir técnico",
            description = "Não é permitido excluir um técnico que esteja atribuído a chamados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Técnico excluído"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Técnico atribuído a chamados",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@Parameter(description = "ID do técnico", example = "1")
                                        @PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
