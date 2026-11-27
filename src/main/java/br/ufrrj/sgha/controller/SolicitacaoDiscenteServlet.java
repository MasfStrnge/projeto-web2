package br.ufrrj.sgha.controller;

import br.ufrrj.sgha.dao.DiscenteDAO;
import br.ufrrj.sgha.dao.SolicitacaoDAO;
import br.ufrrj.sgha.model.ArmazenamentoComprovantes;
import br.ufrrj.sgha.model.Discente;
import br.ufrrj.sgha.model.Solicitacao;
import br.ufrrj.sgha.model.StatusSolicitacao;
import br.ufrrj.sgha.model.TipoHoras;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@WebServlet("/discente/solicitacoes")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,   // 1 MB
    maxFileSize = 1024 * 1024 * 5,     // 5 MB
    maxRequestSize = 1024 * 1024 * 10  // 10 MB
)
public class SolicitacaoDiscenteServlet extends HttpServlet {
    private final DiscenteDAO discenteDAO = new DiscenteDAO();
    private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();
    private final ArmazenamentoComprovantes armazenamento = new ArmazenamentoComprovantes();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        Discente discente = obterDiscenteSessao(req, res);
        if (discente == null) {
            return;
        }

        prepararTela(req, discente);
        req.getRequestDispatcher("/WEB-INF/views/dashboard-discente.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        Discente discente = obterDiscenteSessao(req, res);
        if (discente == null) {
            return;
        }

        String acao = req.getParameter("acao");
        try {
            if ("enviar".equals(acao)) {
                processarEnvio(req, discente);
            } else if ("excluir".equals(acao)) {
                processarExclusao(req, discente);
            } else {
                processarCriacao(req, discente);
            }
            res.sendRedirect(req.getContextPath() + "/discente/solicitacoes");
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.setAttribute("erro", e.getMessage());
            prepararTela(req, discente);
            req.getRequestDispatcher("/WEB-INF/views/dashboard-discente.jsp").forward(req, res);
        }
    }

    private void processarCriacao(HttpServletRequest req, Discente discente)
            throws IOException, ServletException {
        String nomeDocumento = req.getParameter("nomeDocumento");
        String tipoHorasStr = req.getParameter("tipoHoras");
        String horasStr = req.getParameter("horasSolicitadas");
        String observacaoDiscente = req.getParameter("observacaoDiscente");

        if (tipoHorasStr == null || tipoHorasStr.isBlank()) {
            throw new IllegalArgumentException("Selecione a comissão de destino.");
        }

        int horasSolicitadas;
        try {
            horasSolicitadas = Integer.parseInt(horasStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe uma quantidade válida de horas pretendidas.");
        }

        TipoHoras tipoHoras;
        try {
            tipoHoras = TipoHoras.valueOf(tipoHorasStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Selecione uma categoria válida.");
        }

        Part comprovante = req.getPart("arquivoComprovante");
        if (comprovante == null || comprovante.getSize() == 0) {
            throw new IllegalArgumentException("O envio do comprovante é obrigatório.");
        }

        String nomeOriginal = comprovante.getSubmittedFileName();
        if (!extensaoPermitida(nomeOriginal)) {
            throw new IllegalArgumentException("Formato inválido. Envie apenas PDF, PNG, JPG ou JPEG.");
        }

        String caminhoSalvo;
        try (InputStream conteudo = comprovante.getInputStream()) {
            caminhoSalvo = armazenamento.salvar(conteudo, nomeOriginal);
        }

        try {
            Solicitacao nova = new Solicitacao(
                discente,
                nomeDocumento,
                tipoHoras,
                horasSolicitadas,
                observacaoDiscente,
                caminhoSalvo
            );
            solicitacaoDAO.registrarSolicitacao(nova);
        } catch (RuntimeException e) {
            apagarComprovante(caminhoSalvo);
            throw e;
        }
    }

    private void processarExclusao(HttpServletRequest req, Discente discente) {
        Solicitacao alvo = buscarSolicitacaoDoDiscente(req, discente);
        if (alvo.getStatus() != StatusSolicitacao.CRIADA) {
            throw new IllegalStateException("Somente solicitações com status CRIADA podem ser excluídas.");
        }

        solicitacaoDAO.removerSolicitacao(alvo.getId());
        apagarComprovante(alvo.getCaminhoArquivoComprovante());
    }

    private void processarEnvio(HttpServletRequest req, Discente discente) {
        Solicitacao alvo = buscarSolicitacaoDoDiscente(req, discente);
        alvo.enviar();
        solicitacaoDAO.atualizar(alvo);
    }

    private Solicitacao buscarSolicitacaoDoDiscente(HttpServletRequest req, Discente discente) {
        long id;
        try {
            id = Long.parseLong(req.getParameter("idSolicitacao"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identificador de solicitação inválido.");
        }

        Solicitacao solicitacao = solicitacaoDAO.buscarSolicitacaoPorId(id);
        if (solicitacao == null || solicitacao.getDiscente() == null
                || !Objects.equals(solicitacao.getDiscente().getId(), discente.getId())) {
            throw new IllegalArgumentException("Solicitação não encontrada.");
        }
        return solicitacao;
    }

    private Discente obterDiscenteSessao(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession sessao = req.getSession(false);
        Object id = sessao == null ? null : sessao.getAttribute("discenteId");
        if (!(id instanceof Long)) {
            res.sendRedirect(req.getContextPath() + "/index.jsp?expirada=1");
            return null;
        }

        Discente discente = discenteDAO.buscarPorId((Long) id);
        if (discente == null) {
            sessao.invalidate();
            res.sendRedirect(req.getContextPath() + "/index.jsp?expirada=1");
            return null;
        }
        return discente;
    }

    private void prepararTela(HttpServletRequest req, Discente discente) {
        List<Solicitacao> solicitacoes = solicitacaoDAO.listarPorDiscenteId(discente.getId());
        req.setAttribute("discente", discente);
        req.setAttribute("solicitacoes", solicitacoes);
        req.setAttribute("tiposHoras", TipoHoras.values());
    }

    private void apagarComprovante(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(caminho));
        } catch (IOException ignored) {
            // A remoção do arquivo não deve desfazer a remoção já concluída no banco.
        }
    }

    private boolean extensaoPermitida(String nomeArquivo) {
        if (nomeArquivo == null) {
            return false;
        }
        String nome = nomeArquivo.toLowerCase(Locale.ROOT);
        return nome.endsWith(".pdf") || nome.endsWith(".png")
                || nome.endsWith(".jpg") || nome.endsWith(".jpeg");
    }
}
