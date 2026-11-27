package br.ufrrj.sgha.dao;

import br.ufrrj.sgha.config.JPAUtil;
import br.ufrrj.sgha.model.Discente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

public class DiscenteDAO {

    public DiscenteDAO() {
    }

    public Discente obterOuCadastrarDiscente(String nomeCompleto, String matricula) {
        String chaveMatricula = matricula == null ? "" : matricula.trim();
        if (chaveMatricula.isEmpty()) {
            throw new IllegalArgumentException("A matrícula é obrigatória.");
        }

        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Discente existente = buscarPorMatriculaInterno(em, chaveMatricula);
            if (existente != null) {
                tx.commit();
                return existente;
            }

            Discente novo = new Discente(nomeCompleto, chaveMatricula);
            em.persist(novo);
            tx.commit();
            return novo;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Discente buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT d FROM Discente d LEFT JOIN FETCH d.solicitacoes WHERE d.id = :id",
                    Discente.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    private Discente buscarPorMatriculaInterno(EntityManager em, String matricula) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT d FROM Discente d LEFT JOIN FETCH d.solicitacoes WHERE d.matricula = :mat",
                    Discente.class)
                    .setParameter("mat", matricula)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}