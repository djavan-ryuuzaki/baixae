package br.com.djavanfernando.socialvideodownloader.core.fakes

import br.com.djavanfernando.socialvideodownloader.core.Video
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class RepositorioEmMemoria : Repositorio {
    private val videos = MutableStateFlow<Map<String, Video>>(emptyMap())

    override suspend fun salvarVideo(video: Video) {
        videos.update { it + (video.id to video) }
    }

    override suspend fun videos(): List<Video> = videos.value.values.toList()

    override fun observarVideos(): Flow<List<Video>> = videos.map { it.values.toList() }
}
