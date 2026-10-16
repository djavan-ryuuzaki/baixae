package br.com.djavanfernando.socialvideodownloader.core

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

/** Altura máxima do vídeo a baixar; `null` significa a melhor disponível. */
enum class LimiteDeQualidade(val alturaMaxima: Int?) {
    P480(480),
    P720(720),
    P1080(1080),
    MAXIMA(null),
}
