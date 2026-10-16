package br.com.djavanfernando.socialvideodownloader.core.fakes

import br.com.djavanfernando.socialvideodownloader.core.Configuracoes
import br.com.djavanfernando.socialvideodownloader.core.portas.Conectividade
import br.com.djavanfernando.socialvideodownloader.core.portas.EstadoDaRede
import br.com.djavanfernando.socialvideodownloader.core.portas.Preferencias
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ConectividadeFalsa(var estado: EstadoDaRede = EstadoDaRede.WIFI) : Conectividade {
    override fun estado(): EstadoDaRede = estado
}

class PreferenciasEmMemoria(inicial: Configuracoes = Configuracoes()) : Preferencias {
    private val atual = MutableStateFlow(inicial)
    override suspend fun ler(): Configuracoes = atual.value
    override suspend fun salvar(configuracoes: Configuracoes) {
        atual.value = configuracoes
    }
    override fun observar(): Flow<Configuracoes> = atual
}
