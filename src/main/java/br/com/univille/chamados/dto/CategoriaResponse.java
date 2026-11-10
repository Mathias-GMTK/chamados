package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoria cadastrada")
public record CategoriaResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "Rede")
        String nome,
        @Schema(example = "Problemas de conexão e internet")
        String descricao
) {}
