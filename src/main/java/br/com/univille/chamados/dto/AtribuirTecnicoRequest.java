package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "Técnico que vai atender o chamado")
public record AtribuirTecnicoRequest(
        @NotNull(message = "O técnico é obrigatório")
        @Schema(example = "1")
        Long tecnicoId
) {}