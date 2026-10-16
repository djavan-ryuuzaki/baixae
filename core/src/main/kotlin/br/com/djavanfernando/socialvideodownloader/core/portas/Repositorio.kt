package br.com.djavanfernando.socialvideodownloader.core.portas

import br.com.djavanfernando.socialvideodownloader.core.Biblioteca
import br.com.djavanfernando.socialvideodownloader.core.ItemDaFila
import br.com.djavanfernando.socialvideodownloader.core.Video
import kotlinx.coroutines.flow.Flow

/** Persistência de Vídeos, Bibliotecas e Fila de Downloads. */
interface Repositorio {
    suspend fun salvarVideo(video: Video)
    suspend fun removerVideo(id: String)
    suspend fun video(id: String): Video?
    suspend fun videos(): List<Video>
    fun observarVideos(): Flow<List<Video>>

    suspend fun salvarBiblioteca(biblioteca: Biblioteca)
    suspend fun removerBiblioteca(id: String)
    suspend fun bibliotecas(): List<Biblioteca>
    fun observarBibliotecas(): Flow<List<Biblioteca>>

    suspend fun salvarItem(item: ItemDaFila)
    suspend fun removerItem(id: String)
    suspend fun itemDaFila(id: String): ItemDaFila?
    suspend fun itensDaFila(): List<ItemDaFila>
    fun observarFila(): Flow<List<ItemDaFila>>
}
