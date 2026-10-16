package br.com.djavanfernando.socialvideodownloader.dados

import br.com.djavanfernando.socialvideodownloader.core.Video
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RepositorioRoom(private val banco: BancoDeDados) : Repositorio {
    override suspend fun salvarVideo(video: Video) = banco.videos().salvar(VideoEntidade.de(video))

    override suspend fun videos(): List<Video> = banco.videos().todos().map { it.paraVideo() }

    override fun observarVideos(): Flow<List<Video>> =
        banco.videos().observarTodos().map { lista -> lista.map { it.paraVideo() } }
}
