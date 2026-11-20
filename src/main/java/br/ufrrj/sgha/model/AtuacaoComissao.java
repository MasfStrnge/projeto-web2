package br.ufrrj.sgha.model;

import java.time.LocalDate;

/** Uma atuação representa o perfil de uma pessoa durante um período específico. */
public class AtuacaoComissao {
    private Long id;
    private MembroComissao membro;
    private PerfilComissao perfil;
    private LocalDate dataInicio;
    private LocalDate dataValidade;
    private boolean ativa = true;

    public AtuacaoComissao() {
    }

    public AtuacaoComissao(PerfilComissao perfil, LocalDate dataInicio, LocalDate dataValidade) {
        setPerfil(perfil);
        setDataInicio(dataInicio);
        setDataValidade(dataValidade);
    }

    public boolean isVigenteEm(LocalDate data) {
        if (!ativa || data == null || dataInicio == null || data.isBefore(dataInicio)) {
            return false;
        }
        return dataValidade == null || !data.isAfter(dataValidade);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MembroComissao getMembro() {
        return membro;
    }

    public void setMembro(MembroComissao membro) {
        this.membro = membro;
    }

    public PerfilComissao getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilComissao perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil é obrigatório.");
        }
        this.perfil = perfil;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        if (dataInicio == null) {
            throw new IllegalArgumentException("A data de início é obrigatória.");
        }
        if (dataValidade != null && dataValidade.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A validade não pode anteceder o início.");
        }
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        if (dataValidade != null && dataInicio != null && dataValidade.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A validade não pode anteceder o início.");
        }
        this.dataValidade = dataValidade;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}
