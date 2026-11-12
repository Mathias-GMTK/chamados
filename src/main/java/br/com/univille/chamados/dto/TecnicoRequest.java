package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.univille.chamados.entity.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar/atualizar um técnico")
public record TecnicoRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        @Schema(example = "João Lima")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        @Schema(example = "joao@suporte.com")
        String email,

        @NotNull(message = "O perfil é obrigatório")
        @Schema(example = "TECNICO")
        Perfil perfil
) {}