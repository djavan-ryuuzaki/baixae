package br.com.djavanfernando.socialvideodownloader.dados

import br.com.djavanfernando.socialvideodownloader.core.Biblioteca
import br.com.djavanfernando.socialvideodownloader.core.ItemDaFila
import br.com.djavanfernando.socialvideodownloader.core.Video
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RepositorioRoom(private val banco: BancoDeDados) : Repositorio {
    override suspend fun salvarVideo(video: Video) = banco.videos().salvar(VideoEntidade.de(video))
    override suspend fun removerVideo(id: String) = banco.videos().remover(id)
    override suspend fun video(id: String): Video? = banco.videos().porId(id)?.paraVideo()
    override suspend fun videos(): List<Video> = banco.videos().todos().map { it.paraVideo() }
    override fun observarVideos(): Flow<List<Video>> =
        banco.videos().observarTodos().map { lista -> lista.map { it.paraVideo() } }

    override suspend fun salvarBiblioteca(biblioteca: Biblioteca) =
        banco.bibliotecas().salvar(BibliotecaEntidade.de(biblioteca))
    override suspend fun removerBiblioteca(id: String) = banco.bibliotecas().remover(id)
    override suspend fun bibliotecas(): List<Biblioteca> = banco.bibliotecas().todas().map { it.paraBiblioteca() }
    override fun observarBibliotecas(): Flow<List<Biblioteca>> =
        banco.bibliotecas().observarTodas().map { lista -> lista.map { it.paraBiblioteca() } }

    override suspend fun salvarItem(item: ItemDaFila) = banco.fila().salvar(ItemDaFilaEntidade.de(item))
    override suspend fun removerItem(id: String) = banco.fila().remover(id)
    override suspend fun itemDaFila(id: String): ItemDaFila? = banco.fila().porId(id)?.paraItem()
    override suspend fun itensDaFila(): List<ItemDaFila> = banco.fila().todos().map { it.paraItem() }
    override fun observarFila(): Flow<List<ItemDaFila>> =
        banco.fila().observarTodos().map { lista -> lista.map { it.paraItem() } }
}
