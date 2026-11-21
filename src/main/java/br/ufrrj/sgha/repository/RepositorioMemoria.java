package br.ufrrj.sgha.repository;

import br.ufrrj.sgha.model.AtuacaoComissao;
import br.ufrrj.sgha.model.Discente;
import br.ufrrj.sgha.model.MembroComissao;
import br.ufrrj.sgha.model.PerfilComissao;
import br.ufrrj.sgha.model.Solicitacao;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Armazena discentes, solicitações e membros da comissão em memória.
public final class RepositorioMemoria {
    private static final Map<String, Discente> DISCENTES_POR_MATRICULA = new ConcurrentHashMap<>();
    private static final List<Solicitacao> TODAS_SOLICITACOES = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, MembroComissao> MEMBROS_POR_USERNAME = new ConcurrentHashMap<>();
    private static final AtomicLong SEQ_DISCENTE = new AtomicLong(1);
    private static final AtomicLong SEQ_SOLICITACAO = new AtomicLong(1);

    static {
        cadastrarMembroDemo("extensao", "123", "Prof. Tiago (Extensão)", PerfilComissao.COMISSAO_EXTENSAO, true);
        cadastrarMembroDemo("complementar", "123", "Profa. Ana (Complementares)", PerfilComissao.COMISSAO_COMPLEMENTAR, true);
        cadastrarMembroDemo("estagio", "123", "Prof. Marcos (Estágio)", PerfilComissao.COMISSAO_ESTAGIO, true);
        cadastrarMembroDemo("coordenador", "123", "Prof. Carlos (Coordenador)", PerfilComissao.COORDENADOR, true);
        cadastrarMembroDemo("secretario", "123", "João Silva (Secretário)", PerfilComissao.SECRETARIO, true);
        cadastrarMembroDemo("exmembro", "123", "Prof. Inativo (Ex-Membro)", PerfilComissao.COMISSAO_EXTENSAO, false);
    }

    private RepositorioMemoria() {
    }

    private static void cadastrarMembroDemo(String username, String senha, String nome,
                                            PerfilComissao perfil, boolean ativo) {
        MembroComissao membro = new MembroComissao(username, senha, nome);
        membro.setAtivo(ativo);
        AtuacaoComissao atuacao = new AtuacaoComissao(perfil, LocalDate.now().minusMonths(1), null);
        atuacao.setAtiva(ativo);
        membro.adicionarAtuacao(atuacao);
        MEMBROS_POR_USERNAME.put(username, membro);
    }

    //Recupera o discente pela matrícula ou cadastra automaticamente no primeiro acesso.
    public static Discente obterOuCadastrarDiscente(String nomeCompleto, String matricula) {
        String chaveMatricula = matricula == null ? "" : matricula.trim();
        if (chaveMatricula.isEmpty()) {
            throw new IllegalArgumentException("A matrícula é obrigatória.");
        }

        return DISCENTES_POR_MATRICULA.computeIfAbsent(chaveMatricula, mat -> {
            Discente novo = new Discente(nomeCompleto, mat);
            novo.setId(SEQ_DISCENTE.getAndIncrement());
            return novo;
        });
    }

    public static void registrarSolicitacao(Solicitacao solicitacao) {
        if (solicitacao.getId() == null) {
            solicitacao.setId(SEQ_SOLICITACAO.getAndIncrement());
        }
        TODAS_SOLICITACOES.add(solicitacao);
    }

    public static List<Solicitacao> listarTodasSolicitacoes() {
        synchronized (TODAS_SOLICITACOES) {
            return new ArrayList<>(TODAS_SOLICITACOES);
        }
    }

    public static Solicitacao buscarSolicitacaoPorId(Long id) {
        if (id == null) {
            return null;
        }
        synchronized (TODAS_SOLICITACOES) {
            return TODAS_SOLICITACOES.stream()
                    .filter(s -> id.equals(s.getId()))
                    .findFirst()
                    .orElse(null);
        }
    }

    // Valida usuário, senha e atuação vigente na data atual.
    public static AtuacaoComissao autenticarComissao(String username, String senha) {
        if (username == null || username.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Informe usuário e senha.");
        }
        MembroComissao membro = MEMBROS_POR_USERNAME.get(username.trim());
        if (membro == null || !membro.getSenha().equals(senha)) {
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        }
        AtuacaoComissao vigente = membro.getAtuacaoVigente();
        if (vigente == null) {
            throw new IllegalStateException("Conta inativa ou sem mandato vigente no sistema.");
        }
        return vigente;
    }
}