package br.ufrrj.sgha.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Uma atuação representa o perfil de uma pessoa durante um período específico. */
@Entity
@Table(name = "atuacoes_comissao")
public class AtuacaoComissao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membro_id", nullable = false)
    private MembroComissao membro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilComissao perfil;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(nullable = false)
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

    /** Verdadeiro se as duas atuações ativas têm algum dia em comum. */
    public boolean sobrepoe(AtuacaoComissao outra) {
        if (outra == null || !ativa || !outra.ativa || dataInicio == null || outra.dataInicio == null) {
            return false;
        }
        boolean terminaAntes = dataValidade != null && dataValidade.isBefore(outra.dataInicio);
        boolean comecaDepois = outra.dataValidade != null && outra.dataValidade.isBefore(dataInicio);
        return !terminaAntes && !comecaDepois;
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

    /**
     * O histórico de pareceres aponta para esta atuação, então o perfil de uma atuação
     * já gravada não muda, para trocar de função, encerre esta e crie outra.
     */
    public void setPerfil(PerfilComissao perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil é obrigatório.");
        }
        if (id != null && this.perfil != null && this.perfil != perfil) {
            throw new IllegalStateException(
                    "O perfil de uma atuação já registrada não pode mudar. Encerre-a e crie uma nova atuação.");
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