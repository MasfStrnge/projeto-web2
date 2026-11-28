package br.ufrrj.sgha.dao;

import br.ufrrj.sgha.config.JPAUtil;
import br.ufrrj.sgha.model.AtuacaoComissao;
import br.ufrrj.sgha.model.MembroComissao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class MembroComissaoDAO {

    public MembroComissaoDAO() {
    }

    public AtuacaoComissao autenticarComissao(String username, String senha) {
        if (username == null || username.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Informe usuário e senha.");
        }

        EntityManager em = JPAUtil.criarEntityManager();
        try {
            MembroComissao membro = em.createQuery(
                    "SELECT DISTINCT m FROM MembroComissao m "
                    + "LEFT JOIN FETCH m.atuacoes "
                    + "WHERE m.username = :user",
                    MembroComissao.class)
                    .setParameter("user", username.trim())
                    .getSingleResult();

            if (!membro.getSenha().equals(senha)) {
                throw new IllegalArgumentException("Usuário ou senha inválidos.");
            }

            AtuacaoComissao vigente = membro.getAtuacaoVigente();
            if (vigente == null) {
                throw new IllegalStateException("Conta inativa ou sem mandato vigente no sistema.");
            }
            return vigente;
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        } finally {
            em.close();
        }
    }

    public AtuacaoComissao buscarAtuacaoPorId(Long id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM AtuacaoComissao a JOIN FETCH a.membro WHERE a.id = :id",
                    AtuacaoComissao.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}