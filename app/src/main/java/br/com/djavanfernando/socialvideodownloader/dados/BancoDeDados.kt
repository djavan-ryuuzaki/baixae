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
        )
    }
}

@Dao
interface VideoDao {
    @Upsert
    suspend fun salvar(video: VideoEntidade)

    @Query("SELECT * FROM videos")
    suspend fun todos(): List<VideoEntidade>

    @Query("SELECT * FROM videos")
    fun observarTodos(): Flow<List<VideoEntidade>>
}

@Database(entities = [VideoEntidade::class], version = 1, exportSchema = false)
abstract class BancoDeDados : RoomDatabase() {
    abstract fun videos(): VideoDao

    companion object {
        fun criar(context: Context): BancoDeDados =
            Room.databaseBuilder(context, BancoDeDados::class.java, "socialvideodownloader.db").build()
    }
}
