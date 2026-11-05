package br.com.univille.chamados.entity;

public enum StatusChamado {
    ABERTO, EM_ATENDIMENTO, AGUARDANDO_CLIENTE, FECHADO


    public Set<StatusChamado> proximosPermitidos() {
        return switch (this) {
            case ABERTO -> Set.of(EM_ATENDIMENTO, FECHADO);
            case EM_ATENDIMENTO -> Set.of(AGUARDANDO_CLIENTE, FECHADO);
            case AGUARDANDO_CLIENTE -> Set.of(EM_ATENDIMENTO, FECHADO);
            case FECHADO -> Set.of();
        };
    }

    public boolean podeIrPara(StatusChamado novo) {
        return this == novo || proximosPermitidos().contains(novo);
    }
}