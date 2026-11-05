package br.com.univille.chamados.dto;

import br.com.univille.chamados.entity.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusChamado status
) {}