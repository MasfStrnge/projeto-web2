package br.ufrrj.sgha.util;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * decide se dois nomes de documento provavelmente se referem à mesma ação
 * ignora acentos, maiúsculas, pontuação e palavras sem significado (de, para, com etc)
 * o resultado é só um alerta para a comissão ela decide se há duplicidade de fato.
 */
public final class SimilaridadeDocumento {

    //mínima de palavras em comum
    private static final double LIMIAR = 0.5;

    private static final int TAMANHO_MINIMO_PALAVRA = 3;

    private static final Set<String> PALAVRAS_IRRELEVANTES = Set.of(
            "das", "dos", "para", "com", "uma", "que", "nas", "nos", "por", "aos");

    private SimilaridadeDocumento() {
    }

    public static boolean saoSemelhantes(String nomeA, String nomeB) {
        Set<String> palavrasA = extrairPalavras(nomeA);
        Set<String> palavrasB = extrairPalavras(nomeB);
        if (palavrasA.isEmpty() || palavrasB.isEmpty()) {
            return false;
        }
        if (palavrasA.equals(palavrasB)) {
            return true;
        }
        Set<String> emComum = new HashSet<>(palavrasA);
        emComum.retainAll(palavrasB);
        Set<String> uniao = new HashSet<>(palavrasA);
        uniao.addAll(palavrasB);
        return (double) emComum.size() / uniao.size() >= LIMIAR;
    }

    static Set<String> extrairPalavras(String texto) {
        Set<String> palavras = new HashSet<>();
        if (texto == null) {
            return palavras;
        }
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        for (String palavra : semAcentos.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (palavra.length() >= TAMANHO_MINIMO_PALAVRA && !PALAVRAS_IRRELEVANTES.contains(palavra)) {
                palavras.add(palavra);
            }
        }
        return palavras;
    }
}