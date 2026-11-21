package br.ufrrj.sgha.model;

import java.time.LocalDateTime;

/** Mantém o responsável e a função exercida no momento de cada mudança do pedido. */
public class HistoricoStatus {
    private final StatusSolicitacao status;
    private final String observacaoOuParecer;
    private final String observacaoAdministrativa;
    private final AtuacaoComissao atuacaoResponsavel;
    private final String alteradoPor;
    private final LocalDateTime dataRegistro;

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
