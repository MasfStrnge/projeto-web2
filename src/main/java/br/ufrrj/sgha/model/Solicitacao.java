package br.ufrrj.sgha.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pedido do aluno, com o documento enviado e o histórico de decisões. */
public class Solicitacao {
    private Long id;
    private Discente discente;
    private String nomeDocumento;
    private String caminhoArquivoComprovante;
    private TipoHoras tipoHoras;
    private Integer horasSolicitadas;
    private Integer horasConcedidas;
    private String observacaoDiscente;
    private String observacaoParecerPublico;
    private String observacaoAdministrativa;
    private StatusSolicitacao status = StatusSolicitacao.CRIADA;
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataUltimaAtualizacao = LocalDateTime.now();
    private final List<HistoricoStatus> historicos = new ArrayList<>();

    public Solicitacao() {
    }

    public Solicitacao(Discente discente, String nomeDocumento, TipoHoras tipoHoras,
                       Integer horasSolicitadas, String observacaoDiscente,
                       String caminhoArquivoComprovante) {
        setDiscente(discente);
        setNomeDocumento(nomeDocumento);
        setTipoHoras(tipoHoras);
        setHorasSolicitadas(horasSolicitadas);
        this.observacaoDiscente = observacaoDiscente == null ? "" : observacaoDiscente.trim();
        this.caminhoArquivoComprovante = caminhoArquivoComprovante;
        registrarHistorico(StatusSolicitacao.CRIADA, "Solicitação criada.", "", discente.getNomeCompleto(), null);
    }

    /** Encaminha a solicitação para a comissão. */
    public void enviar() {
        exigirStatus(StatusSolicitacao.CRIADA);
        alterarStatus(StatusSolicitacao.ENVIADA, "Solicitação enviada para análise.", "", discente.getNomeCompleto(), null);
    }

    /** Guarda o parecer e o perfil que o responsável exercia naquele momento. */
    public void registrarParecer(StatusSolicitacao novoStatus, Integer horasConcedidas, String parecerPublico,
                                 String observacaoAdministrativa, AtuacaoComissao atuacao) {
        if (atuacao == null || atuacao.getMembro() == null || !atuacao.getMembro().isAtivo()
                || !atuacao.isVigenteEm(LocalDate.now())) {
            throw new IllegalArgumentException("É necessária uma atuação vigente para registrar o parecer.");
        }
        if (novoStatus != StatusSolicitacao.AVALIADA) {
            throw new IllegalArgumentException("Nesta etapa, o parecer inicial deve usar o status AVALIADA.");
        }
        StatusSolicitacao anterior = status;
        boolean parecerInicial = anterior == StatusSolicitacao.ENVIADA
                && novoStatus == StatusSolicitacao.AVALIADA;
        if (!parecerInicial
                || !atuacao.getPerfil().podeAnalisar(tipoHoras)) {
            throw new IllegalStateException("O perfil ou estado atual não permite emitir esse parecer.");
        }
        if (horasConcedidas == null || horasConcedidas <= 0 || horasConcedidas > this.horasSolicitadas) {
            throw new IllegalArgumentException("As horas concedidas devem ser positivas e não superar as solicitadas.");
        }
        setHorasConcedidas(horasConcedidas);
        this.observacaoParecerPublico = parecerPublico;
        this.observacaoAdministrativa = observacaoAdministrativa;
        alterarStatus(novoStatus, parecerPublico, observacaoAdministrativa,
                atuacao.getMembro().getNomeCompleto() + " (" + atuacao.getPerfil().name() + ")", atuacao);
    }

    private void alterarStatus(StatusSolicitacao novoStatus, String observacao, String administrativa,
                               String responsavel, AtuacaoComissao atuacao) {
        if (novoStatus == null) {
            throw new IllegalArgumentException("O novo status é obrigatório.");
        }
        status = novoStatus;
        dataUltimaAtualizacao = LocalDateTime.now();
        registrarHistorico(novoStatus, observacao, administrativa, responsavel, atuacao);
    }

    private void registrarHistorico(StatusSolicitacao estado, String observacao, String administrativa,
                                    String responsavel, AtuacaoComissao atuacao) {
        historicos.add(new HistoricoStatus(estado, observacao, administrativa, responsavel, atuacao));
    }

    private void exigirStatus(StatusSolicitacao esperado) {
        if (status != esperado) {
            throw new IllegalStateException("A operação exige o status " + esperado + ".");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Discente getDiscente() {
        return discente;
    }

    public void setDiscente(Discente discente) {
        if (discente == null) {
            throw new IllegalArgumentException("O discente é obrigatório.");
        }
        if (this.discente != null && this.discente != discente) {
            throw new IllegalStateException("O discente da solicitação não pode ser trocado.");
        }
        if (this.discente == null) {
            this.discente = discente;
            discente.adicionarSolicitacao(this);
        }
    }

    public String getNomeDocumento() {
        return nomeDocumento;
    }

    public void setNomeDocumento(String nomeDocumento) {
        if (nomeDocumento == null || nomeDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do documento é obrigatório.");
        }
        this.nomeDocumento = nomeDocumento.trim();
    }

    public String getCaminhoArquivoComprovante() {
        return caminhoArquivoComprovante;
    }

    public void setCaminhoArquivoComprovante(String caminhoArquivoComprovante) {
        this.caminhoArquivoComprovante = caminhoArquivoComprovante;
    }

    public TipoHoras getTipoHoras() {
        return tipoHoras;
    }

    public void setTipoHoras(TipoHoras tipoHoras) {
        if (tipoHoras == null) {
            throw new IllegalArgumentException("A comissão de destino é obrigatória.");
        }
        this.tipoHoras = tipoHoras;
    }

    public Integer getHorasSolicitadas() {
        return horasSolicitadas;
    }

    public void setHorasSolicitadas(Integer horasSolicitadas) {
        if (horasSolicitadas == null || horasSolicitadas <= 0) {
            throw new IllegalArgumentException("As horas solicitadas devem ser positivas.");
        }
        if (horasConcedidas != null && horasConcedidas > horasSolicitadas) {
            throw new IllegalArgumentException("As horas solicitadas não podem ficar abaixo das horas já concedidas.");
        }
        this.horasSolicitadas = horasSolicitadas;
    }

    public Integer getHorasConcedidas() {
        return horasConcedidas;
    }

    public void setHorasConcedidas(Integer horasConcedidas) {
        if (horasConcedidas != null && horasConcedidas < 0) {
            throw new IllegalArgumentException("As horas concedidas não podem ser negativas.");
        }
        if (horasConcedidas != null && horasSolicitadas != null && horasConcedidas > horasSolicitadas) {
            throw new IllegalArgumentException("As horas concedidas não podem superar as solicitadas.");
        }
        this.horasConcedidas = horasConcedidas;
    }

    public String getObservacaoDiscente() {
        return observacaoDiscente;
    }

    public void setObservacaoDiscente(String observacaoDiscente) {
        this.observacaoDiscente = observacaoDiscente;
    }

    public String getObservacaoParecerPublico() {
        return observacaoParecerPublico;
    }

    public String getObservacaoAdministrativa() {
        return observacaoAdministrativa;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public List<HistoricoStatus> getHistoricos() {
        return Collections.unmodifiableList(historicos);
    }
}
