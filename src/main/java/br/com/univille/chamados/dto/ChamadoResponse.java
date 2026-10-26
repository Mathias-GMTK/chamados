package br.com.univille.chamados.dto;

import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Prioridade prioridade,
        StatusChamado status,
        Long clienteId,
        Long categoriaId,
        Long tecnicoId
) {}