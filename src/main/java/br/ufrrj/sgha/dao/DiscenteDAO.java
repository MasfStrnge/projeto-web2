package br.ufrrj.sgha.dao;

import br.ufrrj.sgha.config.JPAUtil;
import br.ufrrj.sgha.model.Discente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import java.util.List;

public class DiscenteDAO {

    private static final int LIMITE_DA_BUSCA = 50;

    public DiscenteDAO() {
    }

    public Discente obterOuCadastrarDiscente(String nomeCompleto, String matricula) {
        String chaveMatricula = matricula == null ? "" : matricula.trim();
        if (chaveMatricula.isEmpty()) {
            throw new IllegalArgumentException("A matrícula é obrigatória.");
        }

        Discente existente = buscarPorMatricula(chaveMatricula);
        if (existente != null) {
            return existente;
        }
        try {
            return cadastrar(nomeCompleto, chaveMatricula);
        } catch (PersistenceException e) {
            Discente cadastradoPorOutroAcesso = buscarPorMatricula(chaveMatricula);
            if (cadastradoPorOutroAcesso != null) {
                return cadastradoPorOutroAcesso;
            }
            throw e;
        }
    }

    public Discente buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.find(Discente.class, id);
        } finally {
            em.close();
        }
    }

    public Discente buscarPorMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            return null;
        }
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.createQuery("SELECT d FROM Discente d WHERE d.matricula = :mat", Discente.class)
                    .setParameter("mat", matricula.trim())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Discente> buscarPorNomeOuMatricula(String termo) {
        if (termo == null || termo.isBlank()) {
            return List.of();
        }
        String padrao = "%" + termo.trim().toLowerCase() + "%";
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.createQuery(
                    "SELECT d FROM Discente d "
                    + "WHERE LOWER(d.nomeCompleto) LIKE :padrao OR d.matricula LIKE :padrao "
                    + "ORDER BY d.nomeCompleto",
                    Discente.class)
                    .setParameter("padrao", padrao)
                    .setMaxResults(LIMITE_DA_BUSCA)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    private Discente cadastrar(String nomeCompleto, String matricula) {
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Discente novo = new Discente(nomeCompleto, matricula);
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
}