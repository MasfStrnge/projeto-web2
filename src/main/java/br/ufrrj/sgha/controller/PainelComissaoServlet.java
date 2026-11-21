package br.ufrrj.sgha.controller;

import br.ufrrj.sgha.model.AtuacaoComissao;
import br.ufrrj.sgha.model.PerfilComissao;
import br.ufrrj.sgha.model.Solicitacao;
import br.ufrrj.sgha.model.StatusSolicitacao;
import br.ufrrj.sgha.repository.RepositorioMemoria;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/comissao/painel")
public class PainelComissaoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        AtuacaoComissao atuacao = obterAtuacaoSessao(req, res);
        if (atuacao == null) {
            return;
        }

        req.setAttribute("solicitacoes", filtrarParaPerfil(atuacao.getPerfil()));
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

            Solicitacao solicitacao = RepositorioMemoria.buscarSolicitacaoPorId(id);
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

            res.sendRedirect(req.getContextPath() + "/comissao/painel");
        } catch (NumberFormatException e) {
            req.setAttribute("erro", "Informe um valor numérico válido para as horas concedidas.");
            req.setAttribute("solicitacoes", filtrarParaPerfil(atuacao.getPerfil()));
            req.getRequestDispatcher("/WEB-INF/views/dashboard-comissao.jsp").forward(req, res);
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("erro", e.getMessage());
            req.setAttribute("solicitacoes", filtrarParaPerfil(atuacao.getPerfil()));
            req.getRequestDispatcher("/WEB-INF/views/dashboard-comissao.jsp").forward(req, res);
        }
    }

    private List<Solicitacao> filtrarParaPerfil(PerfilComissao perfil) {
        return RepositorioMemoria.listarTodasSolicitacoes().stream()
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
        if (sessao == null || sessao.getAttribute("atuacaoLogada") == null) {
            res.sendRedirect(req.getContextPath() + "/comissao/login");
            return null;
        }
        return (AtuacaoComissao) sessao.getAttribute("atuacaoLogada");
    }
}