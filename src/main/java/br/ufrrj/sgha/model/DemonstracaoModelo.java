package br.ufrrj.sgha.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public final class DemonstracaoModelo {
    private DemonstracaoModelo() {
    }

    public static void main(String[] args) throws IOException {
        Discente discente = new Discente("Leticia de Oliveira Paes", "20240015954");
        String caminhoComprovante = new ArmazenamentoComprovantes().salvar(
                new ByteArrayInputStream("Comprovante de demonstração do SGHA.".getBytes(StandardCharsets.UTF_8)),
                "comprovante-demo.txt");
        Solicitacao solicitacao = new Solicitacao(discente,
                "Certificado de diplomacia audiovisual: projetor funcionando na Semana Nacional de Ciência Tecnologia",
                TipoHoras.EXTENSAO, 12,
                "Apoiou a organização da feira, orientou visitantes e negociou pacificamente com o projetor antes das apresentações.",
                caminhoComprovante);

        MembroComissao membro = new MembroComissao("tcruzfranca", "senha-demo", "Tiago Cruz de Franca");
        membro.setEmail("tcruzfranca@ufrrj.br");
        membro.setDepartamento("Departamento de Computação");
        membro.setFormacao("Doutorado");
        // Perfil e período abaixo são apenas dados de exemplo para demonstrar a vigência.
        AtuacaoComissao atuacao = new AtuacaoComissao(PerfilComissao.COMISSAO_EXTENSAO,
                LocalDate.now().minusMonths(1), null);
        membro.adicionarAtuacao(atuacao);
        solicitacao.enviar();
        solicitacao.registrarParecer(StatusSolicitacao.AVALIADA, 12,
                "Atividade comprovada; horas aprovadas.", "Conferir certificado original na próxima etapa.", atuacao);

        System.out.println("Discente: " + solicitacao.getDiscente().getNomeCompleto());
        System.out.println("Documento: " + solicitacao.getNomeDocumento());
        System.out.println("Comprovante salvo em: " + solicitacao.getCaminhoArquivoComprovante());
        System.out.println("Horas solicitadas: " + solicitacao.getHorasSolicitadas());
        System.out.println("Horas concedidas: " + solicitacao.getHorasConcedidas());
        System.out.println("Destino: " + solicitacao.getTipoHoras().getDescricao());
        System.out.println("Membro: " + atuacao.getMembro().getNomeCompleto());
        System.out.println("Usuário: " + membro.getUsername());
        System.out.println("E-mail: " + membro.getEmail());
        System.out.println("Departamento: " + membro.getDepartamento());
        System.out.println("Formação: " + membro.getFormacao());
        System.out.println("Mandato vigente: " + atuacao.isVigenteEm(LocalDate.now()));
        System.out.println("Status atual: " + solicitacao.getStatus());
        System.out.println("Eventos de histórico: " + solicitacao.getHistoricos().size());
    }
}
