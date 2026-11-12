package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para abrir/atualizar um chamado")
public record ChamadoRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        @Schema(example = "Sem acesso à internet")
        String titulo,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        @Schema(example = "A rede caiu no setor financeiro desde as 9h")
        String descricao,

        @NotNull(message = "A prioridade é obrigatória")
        @Schema(example = "ALTA")
        Prioridade prioridade,

        @Schema(description = "Ignorado: todo chamado abre como ABERTO. Para mudar, use PATCH /chamados/{id}/status", example = "ABERTO")
        StatusChamado status,

        @NotNull(message = "O cliente é obrigatório")
        @Schema(example = "1")
        Long clienteId,

        @NotNull(message = "A categoria é obrigatória")
        @Schema(example = "1")
        Long categoriaId,

        @Schema(description = "Opcional, usado só na abertura. Depois, use PATCH /chamados/{id}/atribuir", example = "1")
        Long tecnicoId
) {}