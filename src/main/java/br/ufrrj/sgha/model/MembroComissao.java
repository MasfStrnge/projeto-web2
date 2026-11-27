package br.ufrrj.sgha.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Conta de um servidor; os perfis que ele ocupa são registrados como atuações. */
@Entity
@Table(name = "membros_comissao")
public class MembroComissao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String senha;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    private String email;
    private String departamento;
    private String formacao;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "data_criacao_cadastro", nullable = false)
    private LocalDateTime dataCriacaoCadastro = LocalDateTime.now();

    @OneToMany(mappedBy = "membro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AtuacaoComissao> atuacoes = new ArrayList<>();

    public MembroComissao() {
    }

    public MembroComissao(String username, String senha, String nomeCompleto) {
        setUsername(username);
        setSenha(senha);
        setNomeCompleto(nomeCompleto);
    }

    public void adicionarAtuacao(AtuacaoComissao atuacao) {
        if (atuacao == null) {
            throw new IllegalArgumentException("A atuação é obrigatória.");
        }
        if (!atuacoes.contains(atuacao)) {
            atuacoes.add(atuacao);
            atuacao.setMembro(this);
        }
    }

    public AtuacaoComissao getAtuacaoVigente() {
        if (!ativo) {
            return null;
        }
        LocalDateTime agora = LocalDateTime.now();
        for (AtuacaoComissao atuacao : atuacoes) {
            if (atuacao.isVigenteEm(agora.toLocalDate())) {
                return atuacao;
            }
        }
        return null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = exigir(username, "O usuário é obrigatório.");
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = exigir(senha, "A senha é obrigatória.");
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = exigir(nomeCompleto, "O nome completo é obrigatório.");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = exigir(email, "O e-mail é obrigatório.");
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = exigir(departamento, "O departamento é obrigatório.");
    }

    public String getFormacao() {
        return formacao;
    }

    public void setFormacao(String formacao) {
        this.formacao = exigir(formacao, "A formação é obrigatória.");
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCriacaoCadastro() {
        return dataCriacaoCadastro;
    }

    public void setDataCriacaoCadastro(LocalDateTime dataCriacaoCadastro) {
        if (dataCriacaoCadastro == null) {
            throw new IllegalArgumentException("A data de cadastro é obrigatória.");
        }
        this.dataCriacaoCadastro = dataCriacaoCadastro;
    }

    public List<AtuacaoComissao> getAtuacoes() {
        return Collections.unmodifiableList(atuacoes);
    }

    private static String exigir(String valor, String mensagem) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }
}