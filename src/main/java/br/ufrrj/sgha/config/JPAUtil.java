package br.ufrrj.sgha.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Guarda o EntityManagerFactory da aplicação (um por contexto web).
 * Os DAOs obtêm um EntityManager por operação com {@link #criarEntityManager()}.
 */
public final class JPAUtil {
    private static final String UNIDADE = "sghaPU";

    private static EntityManagerFactory fabrica;
    private static Exception falhaInicializacao;

    private JPAUtil() {
    }

    /** Abre a conexão com o banco. Chamado pelo PersistenceListener na subida da aplicação. */
    static synchronized void iniciar() {
        if (fabrica != null && fabrica.isOpen()) {
            return;
        }

        Map<String, String> configuracao = new HashMap<>();
        configuracao.put("jakarta.persistence.jdbc.url",
                ler("sgha.db.url", "SGHA_DB_URL", "jdbc:mysql://localhost:3306/sgha?useSSL=false&allowPublicKeyRetrieval=true"));
        configuracao.put("jakarta.persistence.jdbc.user",
                ler("sgha.db.user", "SGHA_DB_USER", "sgha"));
        configuracao.put("jakarta.persistence.jdbc.password",
                ler("sgha.db.password", "SGHA_DB_PASSWORD", "sgha123"));

        try {
            fabrica = Persistence.createEntityManagerFactory(UNIDADE, configuracao);
            falhaInicializacao = null;
        } catch (RuntimeException e) {
            falhaInicializacao = e;
            throw e;
        }
    }

    /** Fecha o EntityManagerFactory. Chamado quando a aplicação é parada. */
    static synchronized void encerrar() {
        if (fabrica != null && fabrica.isOpen()) {
            fabrica.close();
        }
        fabrica = null;
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (fabrica == null || !fabrica.isOpen()) {
            throw new IllegalStateException(
                    "O banco de dados não está disponível. Confira se o MySQL está no ar "
                            + "e se a conexão (SGHA_DB_URL, SGHA_DB_USER, SGHA_DB_PASSWORD) está correta.",
                    falhaInicializacao);
        }
        return fabrica;
    }

    /** Cria um EntityManager novo. Quem chama deve fechá-lo (try/finally). */
    public static EntityManager criarEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /** Propriedade Java (-Dnome) tem prioridade, depois a variável de ambiente, depois o padrão. */
    private static String ler(String propriedade, String variavel, String padrao) {
        String valor = System.getProperty(propriedade);
        if (valor == null || valor.trim().isEmpty()) {
            valor = System.getenv(variavel);
        }
        return valor == null || valor.trim().isEmpty() ? padrao : valor.trim();
    }
}
