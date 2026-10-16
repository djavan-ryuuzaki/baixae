package br.com.djavanfernando.socialvideodownloader.core.portas

import br.com.djavanfernando.socialvideodownloader.core.Video
import kotlinx.coroutines.flow.Flow

/** Persistência de Vídeos, Bibliotecas e Fila de Downloads. */
interface Repositorio {
    suspend fun salvarVideo(video: Video)
    suspend fun videos(): List<Video>
    fun observarVideos(): Flow<List<Video>>
}
