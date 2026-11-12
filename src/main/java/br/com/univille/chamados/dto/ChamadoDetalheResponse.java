package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Chamado com comentários")
public record ChamadoDetalheResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "Sem acesso à internet")
        String titulo,
        @Schema(example = "A rede caiu no setor financeiro desde as 9h")
        String descricao,
        @Schema(example = "2026-11-09T09:15:00")
        LocalDateTime dataAbertura,
        @Schema(description = "Preenchida quando o status vai para FECHADO")
        LocalDateTime dataFechamento,
        @Schema(example = "ALTA")
        Prioridade prioridade,
        @Schema(example = "EM_ATENDIMENTO")
        StatusChamado status,
        @Schema(example = "1")
        Long clienteId,
        @Schema(example = "1")
        Long categoriaId,
        @Schema(example = "1")
        Long tecnicoId,
        List<ComentarioResponse> comentarios
) {}