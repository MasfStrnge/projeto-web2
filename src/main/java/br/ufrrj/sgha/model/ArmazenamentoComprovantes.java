package br.ufrrj.sgha.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

/** Salva comprovantes fora da aplicação usando nomes internos aleatórios. */
public class ArmazenamentoComprovantes {
    private static final String PROPRIEDADE_DIRETORIO = "sgha.upload.dir";

    private final Path diretorio;

    public ArmazenamentoComprovantes() {
        String configurado = System.getProperty(PROPRIEDADE_DIRETORIO);
        if (configurado == null || configurado.trim().isEmpty()) {
            this.diretorio = Paths.get(System.getProperty("user.home"), "sgha", "uploads");
        } else {
            this.diretorio = Paths.get(configurado.trim());
        }
    }

    /** Copia o conteúdo recebido e devolve o caminho que deve ser associado à solicitação. */
    public String salvar(InputStream conteudo, String nomeOriginal) throws IOException {
        if (conteudo == null) {
            throw new IllegalArgumentException("O conteúdo do comprovante é obrigatório.");
        }

        Files.createDirectories(diretorio);
        Path destino = diretorio.resolve(UUID.randomUUID() + obterExtensao(nomeOriginal));
        Files.copy(conteudo, destino);
        return destino.toAbsolutePath().toString();
    }

    public Path getDiretorio() {
        return diretorio;
    }

    private String obterExtensao(String nomeOriginal) {
        if (nomeOriginal == null) {
            return "";
        }
        String nome = Paths.get(nomeOriginal).getFileName().toString();
        int ponto = nome.lastIndexOf('.');
        if (ponto < 0) {
            return "";
        }
        String extensao = nome.substring(ponto).toLowerCase(Locale.ROOT);
        if (extensao.matches("\\.(pdf|png|jpe?g)")) {
            return extensao;
        }
        return "";
    }
}
