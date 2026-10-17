package br.com.djavanfernando.baixae.adaptadores

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import br.com.djavanfernando.baixae.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.baixae.core.portas.ArquivoSalvo
import br.com.djavanfernando.baixae.core.portas.ArquivoTemporario
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

/**
 * Salva Vídeos no armazenamento compartilhado via MediaStore, para que apareçam na galeria.
 * A Pasta de destino é um caminho relativo à raiz do armazenamento (ex.: "Movies/Baixaê").
 * O caminho de um Vídeo salvo é a URI `content://` do MediaStore.
 */
class ArmazenamentoMediaStore(private val context: Context) : ArmazenamentoDeArquivos {
    private val resolver get() = context.contentResolver
    /** Downloads em andamento e cópias do Compartilhamento; exposta a outros apps só via FileProvider. */
    private val pastaTemporaria get() = File(context.cacheDir, "temporarios").apply { mkdirs() }

    /** Apaga temporários esquecidos; chamado ao iniciar o processo, quando nada está em andamento. */
    fun limparTemporarios() {
        pastaTemporaria.listFiles()?.forEach { it.deleteRecursively() }
    }

    override fun criarTemporario(): ArquivoTemporario =
        ArquivoTemporario(File(pastaTemporaria, "${UUID.randomUUID()}.mp4").absolutePath)

    override fun descartarTemporario(temporario: ArquivoTemporario) {
        val base = File(temporario.caminho).nameWithoutExtension
        pastaTemporaria.listFiles { arquivo -> arquivo.name.startsWith(base) }?.forEach { it.delete() }
    }

    override fun existeNaPasta(pasta: String, nomeDoArquivo: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return arquivoPublico(pasta, nomeDoArquivo).exists()
        resolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
            arrayOf(caminhoRelativo(pasta), nomeDoArquivo),
            null,
        )?.use { return it.count > 0 }
        return false
    }

    override fun salvar(temporario: ArquivoTemporario, pasta: String, nomeDoArquivo: String): ArquivoSalvo {
        val origem = File(temporario.caminho)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            salvarComMediaStore(origem, pasta, nomeDoArquivo)
        } else {
            salvarComArquivoPublico(origem, pasta, nomeDoArquivo)
        }
        origem.delete()
        return ArquivoSalvo(uri.toString(), nomeReal(uri) ?: nomeDoArquivo)
    }

    /** Nome com que o MediaStore gravou o arquivo (ele acrescenta " (1)" se houver outro, invisível ao app, com o mesmo nome). */
    private fun nomeReal(uri: Uri): String? =
        resolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }

    override fun renomear(caminho: String, novoNomeDoArquivo: String): ArquivoSalvo {
        val uri = Uri.parse(caminho)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val atual = File(caminhoReal(uri) ?: error("Arquivo não encontrado"))
            val novo = File(atual.parentFile, novoNomeDoArquivo)
            check(atual.renameTo(novo)) { "Não foi possível renomear o arquivo" }
            @Suppress("DEPRECATION")
            resolver.update(
                uri,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DATA, novo.absolutePath)
                    put(MediaStore.MediaColumns.DISPLAY_NAME, novoNomeDoArquivo)
                },
                null,
                null,
            )
        } else {
            val valores = ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, novoNomeDoArquivo) }
            check(resolver.update(uri, valores, null, null) > 0) { "Não foi possível renomear o arquivo" }
        }
        return ArquivoSalvo(caminho, nomeReal(uri) ?: novoNomeDoArquivo)
    }

    /** Menor espaço livre entre o cache (onde fica o temporário) e o armazenamento compartilhado. */
    @Suppress("DEPRECATION")
    override fun espacoLivre(pasta: String): Long =
        minOf(StatFs(context.cacheDir.path).availableBytes, StatFs(Environment.getExternalStorageDirectory().path).availableBytes)

    override fun existe(caminho: String): Boolean {
        val uri = Uri.parse(caminho)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return caminhoReal(uri)?.let { File(it).exists() } == true
        // Abrir o arquivo (e não só consultar a linha do MediaStore) detecta também arquivos apagados por fora.
        return try {
            resolver.openFileDescriptor(uri, "r")?.use { true } == true
        } catch (_: FileNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    override fun apagar(caminho: String) {
        val uri = Uri.parse(caminho)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) caminhoReal(uri)?.let { File(it).delete() }
        resolver.delete(uri, null, null)
    }

    @Suppress("DEPRECATION")
    private fun caminhoReal(uri: Uri): String? =
        resolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }

    private fun salvarComMediaStore(origem: File, pasta: String, nomeDoArquivo: String): Uri {
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nomeDoArquivo)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME)
            put(MediaStore.MediaColumns.RELATIVE_PATH, caminhoRelativo(pasta))
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), valores)
            ?: error("Não foi possível criar o arquivo em $pasta")
        try {
            resolver.openOutputStream(uri)!!.use { saida -> origem.inputStream().use { it.copyTo(saida) } }
        } catch (erro: Exception) {
            resolver.delete(uri, null, null)
            throw erro
        }
        resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        return uri
    }

    @Suppress("DEPRECATION")
    private fun salvarComArquivoPublico(origem: File, pasta: String, nomeDoArquivo: String): Uri {
        val destino = arquivoPublico(pasta, nomeDoArquivo)
        destino.parentFile?.mkdirs()
        origem.copyTo(destino)
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DATA, destino.absolutePath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, nomeDoArquivo)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME)
        }
        return resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, valores)
            ?: Uri.fromFile(destino)
    }

    @Suppress("DEPRECATION")
    private fun arquivoPublico(pasta: String, nomeDoArquivo: String) =
        File(File(Environment.getExternalStorageDirectory(), pasta), nomeDoArquivo)

    private fun caminhoRelativo(pasta: String) = pasta.trim('/') + "/"

    private companion object {
        const val MIME = "video/mp4"
    }
}
