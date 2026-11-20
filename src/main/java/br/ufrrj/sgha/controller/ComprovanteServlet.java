package br.ufrrj.sgha.controller;

import br.ufrrj.sgha.model.ArmazenamentoComprovantes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/comprovantes")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
public class ComprovanteServlet extends HttpServlet {
    private final ArmazenamentoComprovantes armazenamento = new ArmazenamentoComprovantes();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Part comprovante = req.getPart("comprovante");
        if (comprovante == null || comprovante.getSize() == 0) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Selecione um comprovante.");
            return;
        }

        String nomeOriginal = comprovante.getSubmittedFileName();
        if (!extensaoPermitida(nomeOriginal)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Envie um arquivo PDF, PNG ou JPEG.");
            return;
        }

        String caminho;
        try (InputStream conteudo = comprovante.getInputStream()) {
            caminho = armazenamento.salvar(conteudo, nomeOriginal);
        }
        req.setAttribute("caminhoComprovante", caminho);
        req.getRequestDispatcher("/index.jsp").forward(req, res);
    }

    private boolean extensaoPermitida(String nomeArquivo) {
        if (nomeArquivo == null) {
            return false;
        }
        String nome = nomeArquivo.toLowerCase(java.util.Locale.ROOT);
        return nome.endsWith(".pdf") || nome.endsWith(".png")
                || nome.endsWith(".jpg") || nome.endsWith(".jpeg");
    }
}
