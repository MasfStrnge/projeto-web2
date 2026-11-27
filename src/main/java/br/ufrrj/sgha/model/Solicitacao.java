package br.ufrrj.sgha.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pedido do aluno, com o documento enviado e o histórico de decisões. */
@Entity
@Table(name = "solicitacoes")
public class Solicitacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "discente_id", nullable = false)
    private Discente discente;

    @Column(name = "nome_documento", nullable = false)
    private String nomeDocumento;

    @Column(name = "caminho_arquivo_comprovante", nullable = false, length = 500)
    private String caminhoArquivoComprovante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_horas", nullable = false)
    private TipoHoras tipoHoras;

    @Column(name = "horas_solicitadas", nullable = false)
    private Integer horasSolicitadas;

    @Column(name = "horas_concedidas")
    private Integer horasConcedidas;

    @Column(name = "observacao_discente", length = 1000)
    private String observacaoDiscente;

    @Column(name = "observacao_parecer_publico", length = 1000)
    private String observacaoParecerPublico;

    @Column(name = "observacao_administrativa", length = 1000)
    private String observacaoAdministrativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status = StatusSolicitacao.CRIADA;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_ultima_atualizacao", nullable = false)
    private LocalDateTime dataUltimaAtualizacao = LocalDateTime.now();

    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataRegistro ASC")
    private List<HistoricoStatus> historicos = new ArrayList<>();

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
        HistoricoStatus item = new HistoricoStatus(estado, observacao, administrativa, responsavel, atuacao);
        item.setSolicitacao(this);
        historicos.add(item);
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