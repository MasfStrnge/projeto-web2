package br.ufrrj.sgha.dao;

import br.ufrrj.sgha.config.JPAUtil;
import br.ufrrj.sgha.model.PerfilComissao;
import br.ufrrj.sgha.model.Solicitacao;
import br.ufrrj.sgha.model.StatusSolicitacao;
import br.ufrrj.sgha.model.TipoHoras;
import br.ufrrj.sgha.util.SimilaridadeDocumento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SolicitacaoDAO {

    private static final String SELECIONAR_COM_DADOS =
            "SELECT DISTINCT s FROM Solicitacao s "
            + "JOIN FETCH s.discente "
            + "LEFT JOIN FETCH s.historicos ";

    private static final String ORDENAR_POR_CRIACAO = " ORDER BY s.dataCriacao DESC";

    private static final List<StatusSolicitacao> FORA_DA_FILA =
            List.of(StatusSolicitacao.CRIADA, StatusSolicitacao.CANCELADA);

    private static final List<StatusSolicitacao> HOMOLOGADAS =
            List.of(StatusSolicitacao.ACEITA, StatusSolicitacao.CONCLUIDO);

    public SolicitacaoDAO() {
    }

    public void registrarSolicitacao(Solicitacao solicitacao) {
        executarEmTransacao(em -> {
            em.persist(solicitacao);
            return null;
        });
    }

    public Solicitacao atualizar(Solicitacao solicitacao) {
        return executarEmTransacao(em -> em.merge(solicitacao));
    }

    public void removerSolicitacao(Long id) {
        if (id == null) {
            return;
        }
        executarEmTransacao(em -> {
            Solicitacao gerenciada = em.find(Solicitacao.class, id);
            if (gerenciada != null) {
                if (gerenciada.getStatus() != StatusSolicitacao.CRIADA) {
                    throw new IllegalStateException("Somente solicitações com status CRIADA podem ser excluídas.");
                }
                em.remove(gerenciada);
            }
            return null;
        });
    }

    public void removerSolicitacao(Solicitacao solicitacao) {
        if (solicitacao != null) {
            removerSolicitacao(solicitacao.getId());
        }
    }

    public Solicitacao buscarSolicitacaoPorId(Long id) {
        if (id == null) {
            return null;
        }
        List<Solicitacao> encontradas = listar("WHERE s.id = :id", Map.of("id", id));
        return encontradas.isEmpty() ? null : encontradas.get(0);
    }

    public List<Solicitacao> listarTodasSolicitacoes() {
        return listar("", Map.of());
    }

    public List<Solicitacao> listarPorDiscenteId(Long discenteId) {
        if (discenteId == null) {
            return List.of();
        }
        return listar("WHERE s.discente.id = :discenteId", Map.of("discenteId", discenteId));
    }

    /**
     * fila de análise de um perfi de cada comissão que vê só o seu tipo de horas e o
     * coordenador vê todos, o secretário não analisa
     */
    public List<Solicitacao> listarFilaDaComissao(PerfilComissao perfil) {
        List<TipoHoras> tipos = tiposQuePodeAnalisar(perfil);
        if (tipos.isEmpty()) {
            return List.of();
        }
        return listar("WHERE s.tipoHoras IN (:tipos) AND s.status NOT IN (:foraDaFila)",
                Map.of("tipos", tipos, "foraDaFila", FORA_DA_FILA));
    }

    public List<Solicitacao> listarParaConsulta() {
        return listar("WHERE s.status <> :rascunho", Map.of("rascunho", StatusSolicitacao.CRIADA));
    }

    //tela do coordenador para restaurar solicitações canceladas.
    public List<Solicitacao> listarCanceladas() {
        return listar("WHERE s.status = :cancelada", Map.of("cancelada", StatusSolicitacao.CANCELADA));
    }

    /**
     * duplicidade: solicitações do mesmo discente com nome semelhante, ignora rascunhos e canceladas, que não contam horas
     * o histórico já vêm carregado
     */
    public List<Solicitacao> buscarPossiveisDuplicidades(Solicitacao solicitacao) {
        if (solicitacao == null || solicitacao.getDiscente() == null
                || solicitacao.getDiscente().getId() == null) {
            return List.of();
        }
        Long idAtual = solicitacao.getId() == null ? -1L : solicitacao.getId();
        List<Solicitacao> candidatas = listar(
                "WHERE s.discente.id = :discenteId AND s.id <> :idAtual AND s.status NOT IN (:foraDaFila)",
                Map.of("discenteId", solicitacao.getDiscente().getId(),
                        "idAtual", idAtual,
                        "foraDaFila", FORA_DA_FILA));

        List<Solicitacao> semelhantes = new ArrayList<>();
        for (Solicitacao candidata : candidatas) {
            if (SimilaridadeDocumento.saoSemelhantes(
                    solicitacao.getNomeDocumento(), candidata.getNomeDocumento())) {
                semelhantes.add(candidata);
            }
        }
        return semelhantes;
    }

    //total de horas concedidas e aceitas, somando todas as comissões
    public long somarHorasHomologadas(Long discenteId) {
        if (discenteId == null) {
            return 0L;
        }
        return consultar(em -> {
            Long total = em.createQuery(
                    "SELECT SUM(s.horasConcedidas) FROM Solicitacao s "
                    + "WHERE s.discente.id = :discenteId AND s.status IN (:homologadas)",
                    Long.class)
                    .setParameter("discenteId", discenteId)
                    .setParameter("homologadas", HOMOLOGADAS)
                    .getSingleResult();
            return total == null ? 0L : total;
        });
    }

    //mesmo total separado por tipo de horas todos os tipos aparecem mesmo com zero
    public Map<TipoHoras, Long> somarHorasHomologadasPorTipo(Long discenteId) {
        Map<TipoHoras, Long> totais = new EnumMap<>(TipoHoras.class);
        for (TipoHoras tipo : TipoHoras.values()) {
            totais.put(tipo, 0L);
        }
        if (discenteId == null) {
            return totais;
        }
        List<Object[]> linhas = consultar(em -> em.createQuery(
                "SELECT s.tipoHoras, SUM(s.horasConcedidas) FROM Solicitacao s "
                + "WHERE s.discente.id = :discenteId AND s.status IN (:homologadas) "
                + "GROUP BY s.tipoHoras",
                Object[].class)
                .setParameter("discenteId", discenteId)
                .setParameter("homologadas", HOMOLOGADAS)
                .getResultList());
        for (Object[] linha : linhas) {
            totais.put((TipoHoras) linha[0], linha[1] == null ? 0L : ((Number) linha[1]).longValue());
        }
        return totais;
    }

    private List<Solicitacao> listar(String filtro, Map<String, Object> parametros) {
        return consultar(em -> {
            TypedQuery<Solicitacao> consulta = em.createQuery(
                    SELECIONAR_COM_DADOS + filtro + ORDENAR_POR_CRIACAO, Solicitacao.class);
            parametros.forEach((nome, valor) -> consulta.setParameter(nome, valor));
            return consulta.getResultList();
        });
    }

    private List<TipoHoras> tiposQuePodeAnalisar(PerfilComissao perfil) {
        List<TipoHoras> tipos = new ArrayList<>();
        if (perfil == null) {
            return tipos;
        }
        for (TipoHoras tipo : TipoHoras.values()) {
            if (perfil.podeAnalisar(tipo)) {
                tipos.add(tipo);
            }
        }
        return tipos;
    }

    private <T> T consultar(Function<EntityManager, T> consulta) {
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return consulta.apply(em);
        } finally {
            em.close();
        }
    }

    private <T> T executarEmTransacao(Function<EntityManager, T> operacao) {
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T resultado = operacao.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            if (e instanceof OptimisticLockException || e.getCause() instanceof OptimisticLockException) {
                throw new IllegalStateException(
                        "A solicitação foi alterada por outra pessoa. Recarregue a página e tente novamente.", e);
            }
            throw e;
        } finally {
            em.close();
        }
    }
}