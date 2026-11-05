package br.com.univille.chamados.dto;

import java.time.LocalDateTime;

public record ComentarioResponse(Long id, String texto, String autor, LocalDateTime data) {}