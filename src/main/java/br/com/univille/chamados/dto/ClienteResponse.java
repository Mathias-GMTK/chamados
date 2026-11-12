package br.com.univille.chamados.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.univille.chamados.entity.Cliente;

@Schema(description = "Cliente cadastrado")
public record ClienteResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Maria Souza") String nome,
        @Schema(example = "maria@empresa.com") String email,
        @Schema(example = "(47) 99999-0000") String telefone
) {
    public static ClienteResponse de(Cliente c){
        return new ClienteResponse(c.getId(), c.getNome(), c.getEmail(), c.getTelefone());
    }
}
