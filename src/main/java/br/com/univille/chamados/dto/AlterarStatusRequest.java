package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.univille.chamados.entity.StatusChamado;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo status do chamado")
public record AlterarStatusRequest(
        @NotNull(message = "O status é obrigatório")
        @Schema(example = "EM_ATENDIMENTO")
        StatusChamado status
) {}