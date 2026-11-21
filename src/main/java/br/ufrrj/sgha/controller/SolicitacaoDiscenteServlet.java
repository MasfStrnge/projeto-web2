package br.ufrrj.sgha.controller;

import br.ufrrj.sgha.model.ArmazenamentoComprovantes;
import br.ufrrj.sgha.model.Discente;
import br.ufrrj.sgha.model.Solicitacao;
import br.ufrrj.sgha.model.TipoHoras;
import br.ufrrj.sgha.repository.RepositorioMemoria;
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
import java.util.Locale;

@WebServlet("/discente/solicitacoes")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,   // 1 MB
    maxFileSize = 1024 * 1024 * 5,     // 5 MB
    maxRequestSize = 1024 * 1024 * 10  // 10 MB
)
public class SolicitacaoDiscenteServlet extends HttpServlet {
    private final ArmazenamentoComprovantes armazenamento = new ArmazenamentoComprovantes();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        Discente discente = obterDiscenteSessao(req, res);
        if (discente == null) {
            return;
        }

        req.setAttribute("tiposHoras", TipoHoras.values());
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
            req.setAttribute("tiposHoras", TipoHoras.values());
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

        TipoHoras tipoHoras = TipoHoras.valueOf(tipoHorasStr);
        Solicitacao nova = new Solicitacao(
            discente,
            nomeDocumento,
            tipoHoras,
            horasSolicitadas,
            observacaoDiscente,
            caminhoSalvo
        );
        RepositorioMemoria.registrarSolicitacao(nova);
    }

    private void processarExclusao(HttpServletRequest req, Discente discente) {
        long id;
        try {
            id = Long.parseLong(req.getParameter("idSolicitacao"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identificador de solicitação inválido.");
        }

        Solicitacao alvo = discente.getSolicitacoes().stream()
                .filter(s -> s.getId() != null && s.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));

        discente.removerSolicitacao(alvo);
        RepositorioMemoria.removerSolicitacao(alvo);

        // Remove também o arquivo físico salvo em ~/sgha/uploads/
        if (alvo.getCaminhoArquivoComprovante() != null) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(alvo.getCaminhoArquivoComprovante()));
            } catch (IOException ignored) {
            }
        }
    }

    private void processarEnvio(HttpServletRequest req, Discente discente) {
        String idStr = req.getParameter("idSolicitacao");
        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identificador de solicitação inválido.");
        }

        Solicitacao alvo = discente.getSolicitacoes().stream()
                .filter(s -> s.getId() != null && s.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));

        alvo.enviar();
    }

    private Discente obterDiscenteSessao(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession sessao = req.getSession(false);
        if (sessao == null || sessao.getAttribute("discenteLogado") == null) {
            res.sendRedirect(req.getContextPath() + "/index.jsp?expirada=1");
            return null;
        }
        return (Discente) sessao.getAttribute("discenteLogado");
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