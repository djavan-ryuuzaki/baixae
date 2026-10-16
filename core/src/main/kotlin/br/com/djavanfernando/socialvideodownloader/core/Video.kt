package br.com.djavanfernando.socialvideodownloader.core

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
) {
    val nomeDoArquivo: String get() = "$nome.$EXTENSAO"

    companion object {
        const val EXTENSAO = "mp4"
    }
}
