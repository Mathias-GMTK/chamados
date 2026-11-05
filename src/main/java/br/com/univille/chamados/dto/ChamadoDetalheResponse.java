package br.com.univille.chamados.dto;

import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;

import java.time.LocalDateTime;
import java.util.List;

public record ChamadoDetalheResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Prioridade prioridade,
        StatusChamado status,
        Long clienteId,
        Long categoriaId,
        Long tecnicoId,
        List<ComentarioResponse> comentarios
) {}