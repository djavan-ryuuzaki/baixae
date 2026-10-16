package br.com.djavanfernando.socialvideodownloader.dados

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import br.com.djavanfernando.socialvideodownloader.core.Biblioteca
import br.com.djavanfernando.socialvideodownloader.core.EstadoNaFila
import br.com.djavanfernando.socialvideodownloader.core.ItemDaFila
import br.com.djavanfernando.socialvideodownloader.core.Plataforma
import br.com.djavanfernando.socialvideodownloader.core.TipoDeFalha
import br.com.djavanfernando.socialvideodownloader.core.Video
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Entity(tableName = "videos")
data class VideoEntidade(
    @PrimaryKey val id: String,
    val nome: String,
    val plataforma: String,
    val link: String,
    val autor: String,
    val legenda: String,
    val dataDoDownload: Long,
    val duracaoEmSegundos: Long,
    val miniatura: String?,
    val pasta: String,
    val caminhoDoArquivo: String,
    val bibliotecaId: String,
    val arquivoAusente: Boolean,
    val origemId: String?,
) {
    fun paraVideo() = Video(
        id = id,
        nome = nome,
        plataforma = Plataforma.valueOf(plataforma),
        link = link,
        autor = autor,
        legenda = legenda,
        dataDoDownload = Instant.ofEpochMilli(dataDoDownload),
        duracaoEmSegundos = duracaoEmSegundos,
        miniatura = miniatura,
        pasta = pasta,
        caminhoDoArquivo = caminhoDoArquivo,
        bibliotecaId = bibliotecaId,
        arquivoAusente = arquivoAusente,
        origemId = origemId,
    )

    companion object {
        fun de(video: Video) = VideoEntidade(
            id = video.id,
            nome = video.nome,
            plataforma = video.plataforma.name,
            link = video.link,
            autor = video.autor,
            legenda = video.legenda,
            dataDoDownload = video.dataDoDownload.toEpochMilli(),
            duracaoEmSegundos = video.duracaoEmSegundos,
            miniatura = video.miniatura,
            pasta = video.pasta,
            caminhoDoArquivo = video.caminhoDoArquivo,
            bibliotecaId = video.bibliotecaId,
            arquivoAusente = video.arquivoAusente,
            origemId = video.origemId,
        )
    }
}

@Entity(tableName = "bibliotecas")
data class BibliotecaEntidade(
    @PrimaryKey val id: String,
    val nome: String,
    val plataforma: String?,
) {
    fun paraBiblioteca() = Biblioteca(id, nome, plataforma?.let(Plataforma::valueOf))

    companion object {
        fun de(biblioteca: Biblioteca) = BibliotecaEntidade(biblioteca.id, biblioteca.nome, biblioteca.plataforma?.name)
    }
}

@Entity(tableName = "fila")
data class ItemDaFilaEntidade(
    @PrimaryKey val id: String,
    val link: String,
    val plataforma: String,
    val pasta: String,
    val estado: String,
    val criadoEm: Long,
    val titulo: String?,
    val falha: String?,
    val videoIds: String,
) {
    fun paraItem() = ItemDaFila(
        id = id,
        link = link,
        plataforma = Plataforma.valueOf(plataforma),
        pasta = pasta,
        estado = EstadoNaFila.valueOf(estado),
        criadoEm = Instant.ofEpochMilli(criadoEm),
        titulo = titulo,
        falha = falha?.let(TipoDeFalha::valueOf),
        videoIds = videoIds.split(',').filter { it.isNotEmpty() },
    )

    companion object {
        fun de(item: ItemDaFila) = ItemDaFilaEntidade(
            id = item.id,
            link = item.link,
            plataforma = item.plataforma.name,
            pasta = item.pasta,
            estado = item.estado.name,
            criadoEm = item.criadoEm.toEpochMilli(),
            titulo = item.titulo,
            falha = item.falha?.name,
            videoIds = item.videoIds.joinToString(","),
        )
    }
}

@Dao
interface FilaDao {
    @Upsert
    suspend fun salvar(item: ItemDaFilaEntidade)

    @Query("DELETE FROM fila WHERE id = :id")
    suspend fun remover(id: String)

    @Query("SELECT * FROM fila WHERE id = :id")
    suspend fun porId(id: String): ItemDaFilaEntidade?

    @Query("SELECT * FROM fila")
    suspend fun todos(): List<ItemDaFilaEntidade>

    @Query("SELECT * FROM fila")
    fun observarTodos(): Flow<List<ItemDaFilaEntidade>>
}

@Dao
interface VideoDao {
    @Upsert
    suspend fun salvar(video: VideoEntidade)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun remover(id: String)

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun porId(id: String): VideoEntidade?

    @Query("SELECT * FROM videos")
    suspend fun todos(): List<VideoEntidade>

    @Query("SELECT * FROM videos")
    fun observarTodos(): Flow<List<VideoEntidade>>
}

@Dao
interface BibliotecaDao {
    @Upsert
    suspend fun salvar(biblioteca: BibliotecaEntidade)

    @Query("DELETE FROM bibliotecas WHERE id = :id")
    suspend fun remover(id: String)

    @Query("SELECT * FROM bibliotecas")
    suspend fun todas(): List<BibliotecaEntidade>

    @Query("SELECT * FROM bibliotecas")
    fun observarTodas(): Flow<List<BibliotecaEntidade>>
}

@Database(
    entities = [VideoEntidade::class, BibliotecaEntidade::class, ItemDaFilaEntidade::class],
    version = 1,
    exportSchema = false,
)
abstract class BancoDeDados : RoomDatabase() {
    abstract fun videos(): VideoDao
    abstract fun bibliotecas(): BibliotecaDao
    abstract fun fila(): FilaDao

    companion object {
        fun criar(context: Context): BancoDeDados =
            Room.databaseBuilder(context, BancoDeDados::class.java, "socialvideodownloader.db").build()
    }
}
