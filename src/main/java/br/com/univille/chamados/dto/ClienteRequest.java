package br.com.univille.chamados.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest (
    @NotBlank(message = "Nome é obrigatorio")
    @Size(max = 100, message = "Nome deve conter no maximo 100 caracteres")
    String nome,

    @NotBlank(message = "E-mail é obrigatorio")
    @Email (message = "E-mail invalido")
    String email,

    @Size(max = 20, message = "Telefone deve conter no maximo 20 caracteres")
    String telefone


){}
