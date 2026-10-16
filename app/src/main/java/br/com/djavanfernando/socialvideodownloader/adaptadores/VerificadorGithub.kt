package br.com.djavanfernando.socialvideodownloader.adaptadores

import br.com.djavanfernando.socialvideodownloader.core.portas.VerificadorDeVersoes
import br.com.djavanfernando.socialvideodownloader.core.portas.VersaoPublicada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Última release de [repositorio] (`dono/nome`) no GitHub; sem repositório configurado, não há verificação. */
class VerificadorGithub(private val repositorio: String) : VerificadorDeVersoes {
    override suspend fun ultimaVersao(): VersaoPublicada? {
        if (repositorio.isBlank()) return null
        return withContext(Dispatchers.IO) {
            val conexao = URL("https://api.github.com/repos/$repositorio/releases/latest").openConnection() as HttpURLConnection
            try {
                conexao.connectTimeout = TEMPO_LIMITE_MS
                conexao.readTimeout = TEMPO_LIMITE_MS
                conexao.setRequestProperty("Accept", "application/vnd.github+json")
                if (conexao.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                val json = JSONObject(conexao.inputStream.bufferedReader().use { it.readText() })
                VersaoPublicada(versao = json.getString("tag_name"), endereco = json.getString("html_url"))
            } finally {
                conexao.disconnect()
            }
        }
    }

    private companion object {
        const val TEMPO_LIMITE_MS = 15_000
    }
}
