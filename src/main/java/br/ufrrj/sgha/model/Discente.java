package br.ufrrj.sgha.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Guarda a identificação do aluno e os pedidos que ele criou. */
public class Discente {
    private Long id;
    private String nomeCompleto;
    private String matricula;
    private final List<Solicitacao> solicitacoes = new ArrayList<>();

    public Discente() {
    }

    public Discente(String nomeCompleto, String matricula) {
        setNomeCompleto(nomeCompleto);
        setMatricula(matricula);
    }

    void adicionarSolicitacao(Solicitacao solicitacao) {
        if (solicitacao == null) {
            throw new IllegalArgumentException("A solicitação é obrigatória.");
        }
        solicitacoes.add(solicitacao);
    }

    public void removerSolicitacao(Solicitacao solicitacao) {
        if (solicitacao == null) {
            throw new IllegalArgumentException("A solicitação é obrigatória.");
        }
        if (solicitacao.getStatus() != StatusSolicitacao.CRIADA) {
            throw new IllegalStateException("Somente solicitações com status CRIADA podem ser excluídas.");
        }
        solicitacoes.remove(solicitacao);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome completo é obrigatório.");
        }
        this.nomeCompleto = nomeCompleto.trim();
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("A matrícula é obrigatória.");
        }
        this.matricula = matricula.trim();
    }

    public List<Solicitacao> getSolicitacoes() {
        return Collections.unmodifiableList(solicitacoes);
    }
}
