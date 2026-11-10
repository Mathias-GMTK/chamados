package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Novo comentário no chamado")
public record ComentarioRequest(
        @NotBlank(message = "O texto é obrigatório")
        @Size(max = 1000, message = "O texto deve ter no máximo 1000 caracteres")
        @Schema(example = "Reiniciei o switch, aguardando retorno do cliente")
        String texto,

        @NotBlank(message = "O autor é obrigatório")
        @Size(max = 100, message = "O autor deve ter no máximo 100 caracteres")
        @Schema(example = "João Lima")
        String autor
) {}