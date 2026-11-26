package br.ufrrj.sgha.config;

import br.ufrrj.sgha.model.AtuacaoComissao;
import br.ufrrj.sgha.model.MembroComissao;
import br.ufrrj.sgha.model.PerfilComissao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.logging.Logger;

/** Cadastra os membros de demonstração da comissão quando o banco ainda não tem nenhum. */
final class DadosIniciais {
    private static final Logger LOG = Logger.getLogger(DadosIniciais.class.getName());
    private static final String SENHA_DEMO = "123";

    private DadosIniciais() {
    }

    static void semear() {
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction transacao = em.getTransaction();
        try {
            Long total = em.createQuery("select count(m) from MembroComissao m", Long.class).getSingleResult();
            if (total > 0) {
                return;
            }

            transacao.begin();
            cadastrar(em, "extensao", "Prof. Tiago (Extensão)", PerfilComissao.COMISSAO_EXTENSAO, true, "Doutorado");
            cadastrar(em, "complementar", "Profa. Ana (Complementares)", PerfilComissao.COMISSAO_COMPLEMENTAR, true, "Doutorado");
            cadastrar(em, "estagio", "Prof. Marcos (Estágio)", PerfilComissao.COMISSAO_ESTAGIO, true, "Doutorado");
            cadastrar(em, "coordenador", "Prof. Carlos (Coordenador)", PerfilComissao.COORDENADOR, true, "Doutorado");
            cadastrar(em, "secretario", "João Silva (Secretário)", PerfilComissao.SECRETARIO, true, "Graduação");
            cadastrar(em, "exmembro", "Prof. Inativo (Ex-Membro)", PerfilComissao.COMISSAO_EXTENSAO, false, "Doutorado");
            transacao.commit();
            LOG.info("SGHA: 6 membros iniciais da comissão cadastrados.");
        } catch (RuntimeException e) {
            if (transacao.isActive()) {
                transacao.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private static void cadastrar(EntityManager em, String username, String nome,
                                  PerfilComissao perfil, boolean ativo, String formacao) {
        MembroComissao membro = new MembroComissao(username, SENHA_DEMO, nome);
        membro.setEmail(username + "@sgha.local");
        membro.setDepartamento("Departamento de Computação");
        membro.setFormacao(formacao);
        membro.setAtivo(ativo);

        AtuacaoComissao atuacao = new AtuacaoComissao(perfil, LocalDate.now().minusMonths(1), null);
        atuacao.setAtiva(ativo);
        membro.adicionarAtuacao(atuacao);

        em.persist(membro);
        em.persist(atuacao); // sem efeito se o mapeamento já propaga (cascade) a persistência
    }
}
