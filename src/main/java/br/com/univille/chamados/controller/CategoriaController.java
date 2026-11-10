package br.com.univille.chamados.controller;

import br.com.univille.chamados.dto.CategoriaRequest;
import br.com.univille.chamados.dto.CategoriaResponse;
import br.com.univille.chamados.exception.ErroResposta;
import br.com.univille.chamados.service.CategoriaService;
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

@Tag(name = "Categorias")
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar categorias")
    @GetMapping
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Buscar categoria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/{id}")
    public CategoriaResponse buscar(@Parameter(description = "ID da categoria", example = "1")
                                    @PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Criar categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @Operation(summary = "Atualizar categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@Parameter(description = "ID da categoria", example = "1")
                                       @PathVariable Long id,
                                       @Valid @RequestBody CategoriaRequest request) {
        return service.atualizar(id, request);
    }

    @Operation(summary = "Excluir categoria",
            description = "Não é permitido excluir uma categoria que esteja vinculada a chamados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria excluída"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Categoria vinculada a chamados",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@Parameter(description = "ID da categoria", example = "1")
                                        @PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
