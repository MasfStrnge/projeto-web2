package br.ufrrj.sgha.model;

public enum PerfilComissao {
    COMISSAO_EXTENSAO,
    COMISSAO_COMPLEMENTAR,
    COMISSAO_ESTAGIO,
    COORDENADOR,
    SECRETARIO;

    public boolean podeAnalisar(TipoHoras tipo) {
        if (this == COORDENADOR) {
            return true;
        }
        if (tipo == null) {
            return false;
        }
        switch (this) {
            case COMISSAO_EXTENSAO:
                return tipo == TipoHoras.EXTENSAO;
            case COMISSAO_COMPLEMENTAR:
                return tipo == TipoHoras.COMPLEMENTAR;
            case COMISSAO_ESTAGIO:
                return tipo == TipoHoras.ESTAGIO;
            default:
                return false;
        }
    }
}
