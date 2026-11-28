package br.ufrrj.sgha.controller;

import br.ufrrj.sgha.model.Discente;
import br.ufrrj.sgha.dao.DiscenteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/discente/acesso", "/discente/logout"})
public class AcessoDiscenteServlet extends HttpServlet {
    private static final int TEMPO_INATIVIDADE_SEGUNDOS = 120;
    private final DiscenteDAO discenteDAO = new DiscenteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if ("/discente/logout".equals(req.getServletPath())) {
            HttpSession sessao = req.getSession(false);
            if (sessao != null) {
                sessao.invalidate();
            }
        }
        res.sendRedirect(req.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String nomeCompleto = req.getParameter("nomeCompleto");
        String matricula = req.getParameter("matricula");

        try {
            Discente discente = discenteDAO.obterOuCadastrarDiscente(nomeCompleto, matricula);
            HttpSession sessaoAnterior = req.getSession(false);
            if (sessaoAnterior != null) {
                sessaoAnterior.invalidate();
            }
            HttpSession sessao = req.getSession(true);
            sessao.setAttribute("discenteId", discente.getId());
            sessao.setMaxInactiveInterval(TEMPO_INATIVIDADE_SEGUNDOS);

            res.sendRedirect(req.getContextPath() + "/discente/solicitacoes");
        } catch (IllegalArgumentException e) {
            req.setAttribute("erro", e.getMessage());
            req.getRequestDispatcher("/index.jsp").forward(req, res);
        }
    }
}
