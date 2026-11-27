package br.ufrrj.sgha.controller;

import br.ufrrj.sgha.dao.MembroComissaoDAO;
import br.ufrrj.sgha.dao.SolicitacaoDAO;
import br.ufrrj.sgha.model.AtuacaoComissao;
import br.ufrrj.sgha.model.PerfilComissao;
import br.ufrrj.sgha.model.Solicitacao;
import br.ufrrj.sgha.model.StatusSolicitacao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/comissao/painel")
public class PainelComissaoServlet extends HttpServlet {
    private final MembroComissaoDAO membroComissaoDAO = new MembroComissaoDAO();
    private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        AtuacaoComissao atuacao = obterAtuacaoSessao(req, res);
        if (atuacao == null) {
            return;
        }

        prepararPainel(req, atuacao);
        req.getRequestDispatcher("/WEB-INF/views/dashboard-comissao.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        AtuacaoComissao atuacao = obterAtuacaoSessao(req, res);
        if (atuacao == null) {
            return;
        }

        try {
            long id = Long.parseLong(req.getParameter("idSolicitacao"));
            int horasConcedidas = Integer.parseInt(req.getParameter("horasConcedidas"));
            String parecerPublico = req.getParameter("parecerPublico");
            String obsAdmin = req.getParameter("observacaoAdministrativa");

            Solicitacao solicitacao = solicitacaoDAO.buscarSolicitacaoPorId(id);
            if (solicitacao == null) {
                throw new IllegalArgumentException("Solicitação não encontrada.");
            }

            solicitacao.registrarParecer(
                    StatusSolicitacao.AVALIADA,
                    horasConcedidas,
                    parecerPublico,
                    obsAdmin,
                    atuacao
            );
            solicitacaoDAO.atualizar(solicitacao);

            res.sendRedirect(req.getContextPath() + "/comissao/painel");
        } catch (NumberFormatException e) {
            req.setAttribute("erro", "Informe um valor numérico válido para as horas concedidas.");
            prepararPainel(req, atuacao);
            req.getRequestDispatcher("/WEB-INF/views/dashboard-comissao.jsp").forward(req, res);
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("erro", e.getMessage());
            prepararPainel(req, atuacao);
            req.getRequestDispatcher("/WEB-INF/views/dashboard-comissao.jsp").forward(req, res);
        }
    }

    private void prepararPainel(HttpServletRequest req, AtuacaoComissao atuacao) {
        req.setAttribute("atuacao", atuacao);
        req.setAttribute("solicitacoes", filtrarParaPerfil(atuacao.getPerfil()));
    }

    private List<Solicitacao> filtrarParaPerfil(PerfilComissao perfil) {
        return solicitacaoDAO.listarTodasSolicitacoes().stream()
                .filter(s -> s.getStatus() != StatusSolicitacao.CRIADA
                          && s.getStatus() != StatusSolicitacao.CANCELADA)
                .filter(s -> perfil == PerfilComissao.COORDENADOR
                          || perfil == PerfilComissao.SECRETARIO
                          || perfil.podeAnalisar(s.getTipoHoras()))
                .collect(Collectors.toList());
    }

    private AtuacaoComissao obterAtuacaoSessao(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession sessao = req.getSession(false);
        Object id = sessao == null ? null : sessao.getAttribute("atuacaoId");
        if (!(id instanceof Long)) {
            res.sendRedirect(req.getContextPath() + "/comissao/login");
            return null;
        }

        AtuacaoComissao atuacao = membroComissaoDAO.buscarAtuacaoPorId((Long) id);
        if (atuacao == null || atuacao.getMembro() == null
                || !atuacao.getMembro().isAtivo()
                || !atuacao.isVigenteEm(LocalDate.now())) {
            sessao.invalidate();
            res.sendRedirect(req.getContextPath() + "/comissao/login");
            return null;
        }
        return atuacao;
    }
}
