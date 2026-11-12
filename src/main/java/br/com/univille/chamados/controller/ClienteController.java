package br.com.univille.chamados.controller;

import java.net.URI;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.univille.chamados.dto.ChamadoResponse;
import br.com.univille.chamados.dto.ClienteRequest;
import br.com.univille.chamados.dto.ClienteResponse;
import br.com.univille.chamados.exception.ErroResposta;
import br.com.univille.chamados.service.ChamadoService;
import br.com.univille.chamados.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;
    private final ChamadoService chamadoService;

    public ClienteController(ClienteService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @Operation(summary = "Cadastrar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe cliente com esse e-mail",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest dto) {
        ClienteResponse criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/clientes/" + criado.id())).body(criado);
    }

    @Operation(summary = "Listar clientes")
    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Buscar cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/{id}")
    public ClienteResponse buscar(@Parameter(description = "ID do cliente", example = "1")
                                  @PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(summary = "Listar chamados de um cliente",
            description = "Lista paginada, ordenada por data de abertura (mais recentes primeiro).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de chamados do cliente"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/{id}/chamados")
    public PagedModel<ChamadoResponse> listarChamados(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id,
            @ParameterObject
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return new PagedModel<>(chamadoService.listarPorCliente(id, pageable));
    }

    @Operation(summary = "Atualizar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe cliente com esse e-mail",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/{id}")
    public ClienteResponse atualizar(@Parameter(description = "ID do cliente", example = "1")
                                     @PathVariable Long id,
                                     @Valid @RequestBody ClienteRequest dto) {
        return service.atualizar(id, dto);
    }

    @Operation(summary = "Excluir cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do cliente", example = "1")
                                        @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
