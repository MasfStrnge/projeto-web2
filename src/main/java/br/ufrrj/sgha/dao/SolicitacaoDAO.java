package br.ufrrj.sgha.dao;

import br.ufrrj.sgha.config.JPAUtil;
import br.ufrrj.sgha.model.Solicitacao;
import br.ufrrj.sgha.model.StatusSolicitacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.util.List;

public class SolicitacaoDAO {

    public SolicitacaoDAO() {
    }

    public void registrarSolicitacao(Solicitacao solicitacao) {
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(solicitacao);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Solicitacao atualizar(Solicitacao solicitacao) {
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Solicitacao atualizada = em.merge(solicitacao);
            tx.commit();
            return atualizada;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Solicitacao buscarSolicitacaoPorId(Long id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT s FROM Solicitacao s "
                    + "JOIN FETCH s.discente "
                    + "LEFT JOIN FETCH s.historicos "
                    + "WHERE s.id = :id",
                    Solicitacao.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Solicitacao> listarTodasSolicitacoes() {
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT s FROM Solicitacao s "
                    + "JOIN FETCH s.discente "
                    + "LEFT JOIN FETCH s.historicos "
                    + "ORDER BY s.dataCriacao DESC",
                    Solicitacao.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Solicitacao> listarPorDiscenteId(Long discenteId) {
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT s FROM Solicitacao s "
                    + "JOIN FETCH s.discente d "
                    + "LEFT JOIN FETCH s.historicos "
                    + "WHERE d.id = :discenteId "
                    + "ORDER BY s.dataCriacao DESC",
                    Solicitacao.class)
                    .setParameter("discenteId", discenteId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void removerSolicitacao(Long id) {
        if (id == null) {
            return;
        }
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Solicitacao gerenciada = em.find(Solicitacao.class, id);
            if (gerenciada != null) {
                if (gerenciada.getStatus() != StatusSolicitacao.CRIADA) {
                    throw new IllegalStateException("Somente solicitações com status CRIADA podem ser excluídas.");
                }
                em.remove(gerenciada);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void removerSolicitacao(Solicitacao solicitacao) {
        if (solicitacao != null) {
            removerSolicitacao(solicitacao.getId());
        }
    }
}