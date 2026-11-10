package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar/atualizar uma categoria")
public record CategoriaRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        @Schema(example = "Rede")
        String nome,

        @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
        @Schema(example = "Problemas de conexão e internet")
        String descricao
) {}