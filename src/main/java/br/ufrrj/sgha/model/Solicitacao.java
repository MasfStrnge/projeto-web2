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
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Toda mudança de status grava um HistoricoStatus.
@Entity
@Table(name = "solicitacoes")
public class Solicitacao {
    private static final String RESPONSAVEL_SISTEMA = "Sistema";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Evita que dois avaliadores sobrescrevam as alterações um do outro.
    @Version
    private Long versao;

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
    @OrderBy("dataRegistro ASC, id ASC")
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
        if (caminhoArquivoComprovante == null || caminhoArquivoComprovante.trim().isEmpty()) {
            throw new IllegalStateException("Anexe o comprovante antes de enviar a solicitação.");
        }
        alterarStatus(StatusSolicitacao.ENVIADA, "Solicitação enviada para análise.", "",
                discente.getNomeCompleto(), null);
    }

    public void aceitar() {
        if (!isPermiteAceitar()) {
            throw new IllegalStateException(
                    "Só é possível aceitar um parecer já emitido (AVALIADA ou AVALIADA_APOS_RECURSO).");
        }
        alterarStatus(StatusSolicitacao.ACEITA, "Parecer aceito pelo discente.", "",
                discente.getNomeCompleto(), null);
        alterarStatus(StatusSolicitacao.CONCLUIDO, "Solicitação concluída após o aceite do parecer.", "",
                RESPONSAVEL_SISTEMA, null);
    }

    public void abrirRecurso(String justificativa) {
        exigirStatus(StatusSolicitacao.AVALIADA);
        alterarStatus(StatusSolicitacao.RECURSO, comMotivo("Recurso aberto pelo discente.", justificativa), "",
                discente.getNomeCompleto(), null);
    }

    public void abrirRecursoColegiado(String justificativa) {
        exigirStatus(StatusSolicitacao.AVALIADA_APOS_RECURSO);
        alterarStatus(StatusSolicitacao.RECURSO_COLEGIADO,
                comMotivo("Recurso ao Colegiado aberto pelo discente.", justificativa), "",
                discente.getNomeCompleto(), null);
    }

    public void cancelarPeloDiscente(String motivo) {
        if (!isPermiteCancelar()) {
            throw new IllegalStateException("Esta solicitação não pode mais ser cancelada pelo discente.");
        }
        alterarStatus(StatusSolicitacao.CANCELADA, comMotivo("Solicitação cancelada pelo discente.", motivo), "",
                discente.getNomeCompleto(), null);
    }

    public void registrarParecer(Integer horasConcedidas, String parecerPublico,
                                 String observacaoAdministrativa, AtuacaoComissao atuacao) {
        registrarParecer(statusDoProximoParecer(), horasConcedidas, parecerPublico,
                observacaoAdministrativa, atuacao);
    }

    /** Igual ao método acima, mas confere se o status informado é o esperado para o momento. */
    public void registrarParecer(StatusSolicitacao novoStatus, Integer horasConcedidas, String parecerPublico,
                                 String observacaoAdministrativa, AtuacaoComissao atuacao) {
        exigirAtuacaoVigente(atuacao);
        StatusSolicitacao esperado = statusDoProximoParecer();
        if (novoStatus != esperado) {
            throw new IllegalArgumentException("Neste momento o parecer deve usar o status " + esperado + ".");
        }
        exigirPermissaoDeAnalise(atuacao);
        validarDecisao(horasConcedidas, parecerPublico);
        aplicarDecisao(horasConcedidas, parecerPublico, observacaoAdministrativa);
        alterarStatus(novoStatus, parecerPublico, observacaoAdministrativa, descreverResponsavel(atuacao), atuacao);
    }

    public void registrarParecerColegiado(Integer horasConcedidas, String parecerPublico,
                                          String observacaoAdministrativa, AtuacaoComissao atuacao) {
        exigirStatus(StatusSolicitacao.RECURSO_COLEGIADO);
        exigirAtuacaoVigente(atuacao);
        exigirCoordenador(atuacao, "emitir o parecer do Colegiado");
        validarDecisao(horasConcedidas, parecerPublico);
        aplicarDecisao(horasConcedidas, parecerPublico, observacaoAdministrativa);
        alterarStatus(StatusSolicitacao.CONCLUIDO, "Parecer do Colegiado: " + parecerPublico.trim(),
                observacaoAdministrativa, descreverResponsavel(atuacao), atuacao);
    }

    public void corrigirNomeDocumento(String novoNome, AtuacaoComissao atuacao) {
        if (!isAguardandoParecer()) {
            throw new IllegalStateException(
                    "O nome do documento só pode ser corrigido enquanto a solicitação aguarda parecer.");
        }
        exigirAtuacaoVigente(atuacao);
        exigirPermissaoDeAnalise(atuacao);
        String anterior = nomeDocumento;
        String corrigido = validarNomeDocumento(novoNome);
        if (corrigido.equals(anterior)) {
            return;
        }
        nomeDocumento = corrigido;
        dataUltimaAtualizacao = LocalDateTime.now();
        registrarHistorico(status, "Nome do documento corrigido de \"" + anterior + "\" para \"" + corrigido + "\".",
                "", descreverResponsavel(atuacao), atuacao);
    }

    public void cancelarAdministrativamente(String motivo, AtuacaoComissao atuacao) {
        if (status == StatusSolicitacao.CANCELADA) {
            throw new IllegalStateException("A solicitação já está cancelada.");
        }
        exigirAtuacaoVigente(atuacao);
        exigirCoordenador(atuacao, "cancelar administrativamente uma solicitação");
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo do cancelamento administrativo.");
        }
        alterarStatus(StatusSolicitacao.CANCELADA, "Cancelamento administrativo. Motivo: " + motivo.trim(), "",
                descreverResponsavel(atuacao), atuacao);
    }

    public void restaurar(String motivo, AtuacaoComissao atuacao) {
        exigirStatus(StatusSolicitacao.CANCELADA);
        exigirAtuacaoVigente(atuacao);
        exigirCoordenador(atuacao, "restaurar uma solicitação cancelada");
        StatusSolicitacao anterior = statusAntesDoCancelamento();
        alterarStatus(anterior, comMotivo("Solicitação restaurada pelo Coordenador ao status " + anterior + ".", motivo),
                "", descreverResponsavel(atuacao), atuacao);
    }

    public boolean isPermiteAlterar() {
        return status == StatusSolicitacao.CRIADA;
    }

    public boolean isPermiteEnviar() {
        return status == StatusSolicitacao.CRIADA;
    }

    /** Único status em que a exclusão física é permitida. */
    public boolean isPermiteExcluir() {
        return status == StatusSolicitacao.CRIADA;
    }

    public boolean isPermiteCancelar() {
        return status != StatusSolicitacao.CANCELADA && status != StatusSolicitacao.CONCLUIDO;
    }

    public boolean isPermiteAceitar() {
        return status == StatusSolicitacao.AVALIADA || status == StatusSolicitacao.AVALIADA_APOS_RECURSO;
    }

    public boolean isPermiteRecurso() {
        return status == StatusSolicitacao.AVALIADA;
    }

    public boolean isPermiteRecursoColegiado() {
        return status == StatusSolicitacao.AVALIADA_APOS_RECURSO;
    }

    public boolean isPermiteRestaurar() {
        return status == StatusSolicitacao.CANCELADA;
    }

    public boolean isAguardandoParecer() {
        return status == StatusSolicitacao.ENVIADA
                || status == StatusSolicitacao.RECURSO
                || status == StatusSolicitacao.RECURSO_COLEGIADO;
    }

    private StatusSolicitacao statusDoProximoParecer() {
        if (status == StatusSolicitacao.ENVIADA) {
            return StatusSolicitacao.AVALIADA;
        }
        if (status == StatusSolicitacao.RECURSO) {
            return StatusSolicitacao.AVALIADA_APOS_RECURSO;
        }
        throw new IllegalStateException("O status atual (" + status + ") não aguarda parecer da comissão.");
    }

    /** Todo status novo gera um histórico; o penúltimo registro é o status anterior ao cancelamento. */
    private StatusSolicitacao statusAntesDoCancelamento() {
        if (historicos.size() < 2) {
            throw new IllegalStateException("Não há status anterior registrado para restaurar.");
        }
        return historicos.get(historicos.size() - 2).getStatus();
    }

    private void validarDecisao(Integer horas, String parecerPublico) {
        if (horas == null || horas < 0 || horas > horasSolicitadas) {
            throw new IllegalArgumentException(
                    "As horas concedidas devem estar entre 0 e as horas solicitadas (" + horasSolicitadas + ").");
        }
        if (parecerPublico == null || parecerPublico.trim().isEmpty()) {
            throw new IllegalArgumentException("O parecer é obrigatório.");
        }
    }

    private void aplicarDecisao(Integer horas, String parecerPublico, String observacaoAdmin) {
        setHorasConcedidas(horas);
        this.observacaoParecerPublico = parecerPublico.trim();
        this.observacaoAdministrativa = observacaoAdmin == null ? "" : observacaoAdmin.trim();
    }

    private void exigirAtuacaoVigente(AtuacaoComissao atuacao) {
        if (atuacao == null || atuacao.getMembro() == null || !atuacao.getMembro().isAtivo()
                || !atuacao.isVigenteEm(LocalDate.now())) {
            throw new IllegalArgumentException("É necessária uma atuação vigente para realizar esta operação.");
        }
    }

    private void exigirPermissaoDeAnalise(AtuacaoComissao atuacao) {
        if (!atuacao.getPerfil().podeAnalisar(tipoHoras)) {
            throw new IllegalStateException("O perfil " + atuacao.getPerfil()
                    + " não pode analisar solicitações de " + tipoHoras.getDescricao() + ".");
        }
    }

    private void exigirCoordenador(AtuacaoComissao atuacao, String acao) {
        if (atuacao.getPerfil() != PerfilComissao.COORDENADOR) {
            throw new IllegalStateException("Somente o Coordenador pode " + acao + ".");
        }
    }

    private void exigirEdicaoPeloDiscente() {
        if (status != StatusSolicitacao.CRIADA) {
            throw new IllegalStateException("Só é possível alterar uma solicitação com status CRIADA.");
        }
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

    private static String descreverResponsavel(AtuacaoComissao atuacao) {
        return atuacao.getMembro().getNomeCompleto() + " (" + atuacao.getPerfil().name() + ")";
    }

    private static String comMotivo(String base, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            return base;
        }
        return base + " Motivo: " + motivo.trim();
    }

    private static String validarNomeDocumento(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do documento é obrigatório.");
        }
        return nome.trim();
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
        exigirEdicaoPeloDiscente();
        this.nomeDocumento = validarNomeDocumento(nomeDocumento);
    }

    public String getCaminhoArquivoComprovante() {
        return caminhoArquivoComprovante;
    }

    public void setCaminhoArquivoComprovante(String caminhoArquivoComprovante) {
        exigirEdicaoPeloDiscente();
        this.caminhoArquivoComprovante = caminhoArquivoComprovante;
    }

    public TipoHoras getTipoHoras() {
        return tipoHoras;
    }

    public void setTipoHoras(TipoHoras tipoHoras) {
        exigirEdicaoPeloDiscente();
        if (tipoHoras == null) {
            throw new IllegalArgumentException("A comissão de destino é obrigatória.");
        }
        this.tipoHoras = tipoHoras;
    }

    public Integer getHorasSolicitadas() {
        return horasSolicitadas;
    }

    public void setHorasSolicitadas(Integer horasSolicitadas) {
        exigirEdicaoPeloDiscente();
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

    private void setHorasConcedidas(Integer horasConcedidas) {
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
        exigirEdicaoPeloDiscente();
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