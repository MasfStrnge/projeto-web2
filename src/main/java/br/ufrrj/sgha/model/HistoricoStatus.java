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
import java.time.LocalDateTime;

/** Mantém o responsável e a função exercida no momento de cada mudança do pedido. */
@Entity
@Table(name = "historicos_status")
public class HistoricoStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(name = "observacao_ou_parecer", length = 1000)
    private String observacaoOuParecer;

    @Column(name = "observacao_administrativa", length = 1000)
    private String observacaoAdministrativa;

    @ManyToOne
    @JoinColumn(name = "atuacao_responsavel_id")
    private AtuacaoComissao atuacaoResponsavel;

    @Column(name = "alterado_por", nullable = false)
    private String alteradoPor;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    protected HistoricoStatus() {
    }

    public HistoricoStatus(StatusSolicitacao status, String observacaoOuParecer,
                           String observacaoAdministrativa, String alteradoPor,
                           AtuacaoComissao atuacaoResponsavel) {
        if (status == null) {
            throw new IllegalArgumentException("O status é obrigatório.");
        }
        if (alteradoPor == null || alteradoPor.trim().isEmpty()) {
            throw new IllegalArgumentException("O responsável é obrigatório.");
        }
        this.status = status;
        this.observacaoOuParecer = observacaoOuParecer == null ? "" : observacaoOuParecer.trim();
        this.observacaoAdministrativa = observacaoAdministrativa == null ? "" : observacaoAdministrativa.trim();
        this.alteradoPor = alteradoPor.trim();
        this.atuacaoResponsavel = atuacaoResponsavel;
        this.dataRegistro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    void setSolicitacao(Solicitacao solicitacao) {
        this.solicitacao = solicitacao;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public String getObservacaoOuParecer() {
        return observacaoOuParecer;
    }

    public String getObservacaoAdministrativa() {
        return observacaoAdministrativa;
    }

    public AtuacaoComissao getAtuacaoResponsavel() {
        return atuacaoResponsavel;
    }

    public String getAlteradoPor() {
        return alteradoPor;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}