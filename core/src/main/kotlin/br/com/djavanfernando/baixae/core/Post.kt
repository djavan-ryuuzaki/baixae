package br.com.djavanfernando.baixae.core

/** Metadados de um Post, como devolvidos pelo Extrator. */
data class Post(
    val autor: String,
    val legenda: String,
    val duracaoEmSegundos: Long,
    val miniatura: String?,
    val midias: List<MidiaDoPost>,
)

data class MidiaDoPost(
    val indice: Int,
    val tipo: TipoDeMidia,
    val tamanhoEstimadoEmBytes: Long?,
)

enum class TipoDeMidia { VIDEO, FOTO }

/**
 * Resolução máxima do vídeo a baixar, medida no lado menor (assim 1080p vale para 1920×1080 e para 1080×1920);
 * `null` significa a melhor disponível.
 */
enum class LimiteDeQualidade(val resolucaoMaxima: Int?) {
    P480(480),
    P720(720),
    P1080(1080),
    MAXIMA(null),
}
