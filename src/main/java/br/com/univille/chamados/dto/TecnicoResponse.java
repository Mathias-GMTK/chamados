package br.com.univille.chamados.dto;

import br.com.univille.chamados.entity.Perfil;

public record TecnicoResponse(
    Long id, 
    String nome,
    String email,
    Perfil perfil
) {}