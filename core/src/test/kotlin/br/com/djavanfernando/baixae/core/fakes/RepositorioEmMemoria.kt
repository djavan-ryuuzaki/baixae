package br.com.djavanfernando.baixae.core.fakes

import br.com.djavanfernando.baixae.core.Biblioteca
import br.com.djavanfernando.baixae.core.ItemDaFila
import br.com.djavanfernando.baixae.core.Video
import br.com.djavanfernando.baixae.core.portas.Repositorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class RepositorioEmMemoria : Repositorio {
    private val videos = MutableStateFlow<Map<String, Video>>(emptyMap())
    private val bibliotecas = MutableStateFlow<Map<String, Biblioteca>>(emptyMap())
    private val fila = MutableStateFlow<Map<String, ItemDaFila>>(emptyMap())

    override suspend fun salvarVideo(video: Video) = videos.update { it + (video.id to video) }
    override suspend fun removerVideo(id: String) = videos.update { it - id }
    override suspend fun video(id: String): Video? = videos.value[id]
    override suspend fun videos(): List<Video> = videos.value.values.toList()
    override fun observarVideos(): Flow<List<Video>> = videos.map { it.values.toList() }

    override suspend fun salvarBiblioteca(biblioteca: Biblioteca) =
        bibliotecas.update { it + (biblioteca.id to biblioteca) }
    override suspend fun removerBiblioteca(id: String) = bibliotecas.update { it - id }
    override suspend fun bibliotecas(): List<Biblioteca> = bibliotecas.value.values.toList()
    override fun observarBibliotecas(): Flow<List<Biblioteca>> = bibliotecas.map { it.values.toList() }

    override suspend fun salvarItem(item: ItemDaFila) = fila.update { it + (item.id to item) }
    override suspend fun removerItem(id: String) = fila.update { it - id }
    override suspend fun itemDaFila(id: String): ItemDaFila? = fila.value[id]
    override suspend fun itensDaFila(): List<ItemDaFila> = fila.value.values.toList()
    override fun observarFila(): Flow<List<ItemDaFila>> = fila.map { it.values.toList() }
}
