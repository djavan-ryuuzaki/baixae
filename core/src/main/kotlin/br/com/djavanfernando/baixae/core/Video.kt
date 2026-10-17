package br.com.djavanfernando.baixae.core

import java.time.Instant

/** Item baixado e registrado no app, ligado a um arquivo no celular. Seu nome é o nome do arquivo (sem extensão). */
data class Video(
    val id: String,
    val nome: String,
    val plataforma: Plataforma,
    val link: String,
    val autor: String,
    val legenda: String,
    val dataDoDownload: Instant,
    val duracaoEmSegundos: Long,
    val miniatura: String?,
    val pasta: String,
    val caminhoDoArquivo: String,
    val bibliotecaId: String,
    /** O arquivo foi apagado ou movido fora do app. */
    val arquivoAusente: Boolean = false,
    /** Vídeo de onde este foi derivado (ex.: Versão sem som). */
    val origemId: String? = null,
) {
    val nomeDoArquivo: String get() = nomeDoArquivo(nome)

    companion object {
        const val EXTENSAO = "mp4"

        fun nomeDoArquivo(nome: String) = "$nome.$EXTENSAO"

        fun nomeSemExtensao(nomeDoArquivo: String) = nomeDoArquivo.removeSuffix(".$EXTENSAO")
    }
}
