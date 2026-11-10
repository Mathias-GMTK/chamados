package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Comentário registrado")
public record ComentarioResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Reiniciei o switch, aguardando retorno do cliente") String texto,
        @Schema(example = "João Lima") String autor,
        @Schema(example = "2026-11-09T10:02:00") LocalDateTime data
) {}
