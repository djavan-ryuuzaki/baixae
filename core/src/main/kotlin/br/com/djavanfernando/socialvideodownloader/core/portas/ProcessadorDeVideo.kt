package br.com.djavanfernando.socialvideodownloader.core.portas

/** Transformações de arquivos de vídeo. Ambas são canceláveis pelo cancelamento da coroutine. */
interface ProcessadorDeVideo {
    /** Grava em [destino] uma cópia de [origem] com [texto] no canto inferior direito (recodifica o vídeo). */
    suspend fun aplicarMarcaDagua(origem: String, destino: ArquivoTemporario, texto: String, aoProgredir: (Float) -> Unit)

    /** Grava em [destino] uma cópia de [origem] sem as trilhas de áudio, sem recodificar o vídeo. */
    suspend fun removerAudio(origem: String, destino: ArquivoTemporario)
}
