package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.univille.chamados.entity.Perfil;

@Schema(description = "Técnico cadastrado")
public record TecnicoResponse(
    @Schema(example = "1")
    Long id, 
    @Schema(example = "João Lima")
    String nome,
    @Schema(example = "joao@suporte.com")
    String email,
    @Schema(example = "TECNICO")
    Perfil perfil
) {}