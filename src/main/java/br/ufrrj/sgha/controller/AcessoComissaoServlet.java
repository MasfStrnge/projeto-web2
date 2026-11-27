package br.ufrrj.sgha.controller;

import br.ufrrj.sgha.model.AtuacaoComissao;
import br.ufrrj.sgha.dao.MembroComissaoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/comissao/login", "/comissao/logout"})
public class AcessoComissaoServlet extends HttpServlet {
    private final MembroComissaoDAO membroComissaoDAO = new MembroComissaoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if ("/comissao/logout".equals(req.getServletPath())) {
            HttpSession sessao = req.getSession(false);
            if (sessao != null) {
                sessao.invalidate();
            }
            res.sendRedirect(req.getContextPath() + "/comissao/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login-comissao.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String senha = req.getParameter("senha");

        try {
            AtuacaoComissao atuacao = membroComissaoDAO.autenticarComissao(username, senha);
            HttpSession sessaoAnterior = req.getSession(false);
            if (sessaoAnterior != null) {
                sessaoAnterior.invalidate();
            }
            HttpSession sessao = req.getSession(true);
            sessao.setAttribute("atuacaoId", atuacao.getId());

            res.sendRedirect(req.getContextPath() + "/comissao/painel");
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("erro", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/login-comissao.jsp").forward(req, res);
        }
    }
}
