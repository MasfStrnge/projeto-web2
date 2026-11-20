package br.ufrrj.sgha.repository;

import br.ufrrj.sgha.model.Discente;
import br.ufrrj.sgha.model.Solicitacao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//Armazena discentes e solicitações em memória durante a execução do servidor.
public final class RepositorioMemoria {
    private static final Map<String, Discente> DISCENTES_POR_MATRICULA = new ConcurrentHashMap<>();
    private static final List<Solicitacao> TODAS_SOLICITACOES = Collections.synchronizedList(new ArrayList<>());
    private static final AtomicLong SEQ_DISCENTE = new AtomicLong(1);
    private static final AtomicLong SEQ_SOLICITACAO = new AtomicLong(1);

    private RepositorioMemoria() {
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

    public static Discente buscarDiscentePorMatricula(String matricula) {
        if (matricula == null) {
            return null;
        }
        return DISCENTES_POR_MATRICULA.get(matricula.trim());
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
}