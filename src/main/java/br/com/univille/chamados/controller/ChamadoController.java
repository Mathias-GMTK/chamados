package br.com.univille.chamados.controller;

import br.com.univille.chamados.dto.*;
import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;
import br.com.univille.chamados.exception.ErroResposta;
import br.com.univille.chamados.service.ChamadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Chamados")
@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar chamados",
            description = "Lista paginada com filtros opcionais. Padrão: 10 por página, "
                    + "ordenado por data de abertura (mais recentes primeiro).")
    @GetMapping
    public PagedModel<ChamadoResponse> listar(
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) StatusChamado status,
            @Parameter(description = "Filtrar por prioridade") @RequestParam(required = false) Prioridade prioridade,
            @Parameter(description = "Filtrar por cliente") @RequestParam(required = false) Long clienteId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return new PagedModel<>(service.listar(status, prioridade, clienteId, pageable));
    }

    @Operation(summary = "Detalhar chamado", description = "Retorna o chamado com todos os comentários.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chamado encontrado"),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/{id}")
    public ChamadoDetalheResponse buscar(@Parameter(description = "ID do chamado", example = "1")
                                         @PathVariable Long id) {
        return service.buscarDetalhe(id);
    }

    @Operation(summary = "Abrir chamado",
            description = "Cliente e categoria precisam existir. O técnico é opcional.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chamado aberto"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Cliente, categoria ou técnico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(@Valid @RequestBody ChamadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @Operation(summary = "Atualizar chamado",
            description = "Altera título, descrição, prioridade, cliente e categoria. "
                    + "Status e técnico são ignorados aqui: use PATCH /status e PATCH /atribuir. "
                    + "Chamados FECHADOS não podem ser alterados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chamado atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Chamado, cliente ou categoria não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Chamado fechado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/{id}")
    public ChamadoResponse atualizar(@Parameter(description = "ID do chamado", example = "1")
                                     @PathVariable Long id,
                                     @Valid @RequestBody ChamadoRequest request) {
        return service.atualizar(id, request);
    }

    @Operation(summary = "Atribuir técnico ao chamado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico atribuído"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Chamado ou técnico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Chamado fechado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PatchMapping("/{id}/atribuir")
    public ChamadoResponse atribuir(@Parameter(description = "ID do chamado", example = "1")
                                    @PathVariable Long id,
                                    @Valid @RequestBody AtribuirTecnicoRequest request) {
        return service.atribuir(id, request.tecnicoId());
    }

    @Operation(summary = "Alterar status do chamado",
            description = """
                    Transições permitidas:
                    - ABERTO → EM_ATENDIMENTO ou FECHADO
                    - EM_ATENDIMENTO → AGUARDANDO_CLIENTE ou FECHADO
                    - AGUARDANDO_CLIENTE → EM_ATENDIMENTO ou FECHADO
                    - FECHADO → nenhuma

                    Ao ir para FECHADO, a data de fechamento é preenchida automaticamente.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "400", description = "Status ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Transição de status não permitida",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PatchMapping("/{id}/status")
    public ChamadoResponse alterarStatus(@Parameter(description = "ID do chamado", example = "1")
                                         @PathVariable Long id,
                                         @Valid @RequestBody AlterarStatusRequest request) {
        return service.alterarStatus(id, request.status());
    }

    @Operation(summary = "Adicionar comentário ao chamado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentário adicionado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(
            @Parameter(description = "ID do chamado", example = "1") @PathVariable Long id,
            @Valid @RequestBody ComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.adicionarComentario(id, request));
    }

    @Operation(summary = "Excluir chamado", description = "Remove também os comentários do chamado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chamado excluído"),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@Parameter(description = "ID do chamado", example = "1")
                                        @PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
