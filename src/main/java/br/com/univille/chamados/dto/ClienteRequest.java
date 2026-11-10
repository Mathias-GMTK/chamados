package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar/atualizar um cliente")
public record ClienteRequest (
    @NotBlank(message = "Nome é obrigatorio")
    @Size(max = 100, message = "Nome deve conter no maximo 100 caracteres")
    @Schema(example = "Maria Souza")
    String nome,

    @NotBlank(message = "E-mail é obrigatorio")
    @Email (message = "E-mail invalido")
    @Schema(example = "maria@empresa.com")
    String email,

    @Size(max = 20, message = "Telefone deve conter no maximo 20 caracteres")
    @Schema(example = "(47) 99999-0000")
    String telefone


){}
