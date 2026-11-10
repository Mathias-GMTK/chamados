package br.com.univille.chamados.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Formato padrão das respostas de erro da API")
public record ErroResposta(
        @Schema(example = "2026-11-09T10:30:00")
        LocalDateTime momento,
        @Schema(example = "400")
        int status,
        @Schema(example = "Dados inválidos")
        String erro,
        @Schema(example = "[\"titulo: O título é obrigatório\"]")
        List<String>detalhes
) {}
