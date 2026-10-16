package br.com.djavanfernando.socialvideodownloader.adaptadores

import android.content.Context
import br.com.djavanfernando.socialvideodownloader.core.Configuracoes
import br.com.djavanfernando.socialvideodownloader.core.LimiteDeQualidade
import br.com.djavanfernando.socialvideodownloader.core.portas.Preferencias
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import androidx.core.content.edit

class PreferenciasAndroid(context: Context) : Preferencias {
    private val prefs = context.getSharedPreferences("configuracoes", Context.MODE_PRIVATE)
    private val padrao = Configuracoes()
    private val atual = MutableStateFlow(carregar())

    private fun carregar() = Configuracoes(
        pastaPadrao = prefs.getString(PASTA, null) ?: padrao.pastaPadrao,
        soWifi = prefs.getBoolean(SO_WIFI, padrao.soWifi),
        limiteDeQualidade = prefs.getString(QUALIDADE, null)
            ?.let { nome -> LimiteDeQualidade.entries.firstOrNull { it.name == nome } }
            ?: padrao.limiteDeQualidade,
        marcaDagua = prefs.getBoolean(MARCA_DAGUA, padrao.marcaDagua),
        ultimaVerificacaoDeExtratores = prefs.getLong(ULTIMA_VERIFICACAO, -1L)
            .takeIf { it >= 0 }?.let(Instant::ofEpochMilli),
    )

    override suspend fun ler(): Configuracoes = atual.value

    override suspend fun salvar(configuracoes: Configuracoes) {
        prefs.edit {
            putString(PASTA, configuracoes.pastaPadrao)
            putBoolean(SO_WIFI, configuracoes.soWifi)
            putString(QUALIDADE, configuracoes.limiteDeQualidade.name)
            putBoolean(MARCA_DAGUA, configuracoes.marcaDagua)
            val ultima = configuracoes.ultimaVerificacaoDeExtratores
            if (ultima == null) remove(ULTIMA_VERIFICACAO) else putLong(ULTIMA_VERIFICACAO, ultima.toEpochMilli())
        }
        atual.value = configuracoes
    }

    override fun observar(): Flow<Configuracoes> = atual

    private companion object {
        const val PASTA = "pasta_padrao"
        const val SO_WIFI = "so_wifi"
        const val QUALIDADE = "limite_de_qualidade"
        const val MARCA_DAGUA = "marca_dagua"
        const val ULTIMA_VERIFICACAO = "ultima_verificacao_de_extratores"
    }
}
