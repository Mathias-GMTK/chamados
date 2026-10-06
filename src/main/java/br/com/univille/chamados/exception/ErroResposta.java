package br.com.univille.chamados.exception;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ErroResposta(
        LocalDateTime momento,
        int status,
        String erro,
        List<String>detalhes
) {}
