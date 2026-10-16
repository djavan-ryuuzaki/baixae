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
import br.com.djavanfernando.socialvideodownloader.core.Plataforma
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
    entities = [VideoEntidade::class, BibliotecaEntidade::class],
    version = 1,
    exportSchema = false,
)
abstract class BancoDeDados : RoomDatabase() {
    abstract fun videos(): VideoDao
    abstract fun bibliotecas(): BibliotecaDao

    companion object {
        fun criar(context: Context): BancoDeDados =
            Room.databaseBuilder(context, BancoDeDados::class.java, "socialvideodownloader.db").build()
    }
}
