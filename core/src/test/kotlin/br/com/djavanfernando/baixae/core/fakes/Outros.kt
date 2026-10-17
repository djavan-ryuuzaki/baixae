package br.com.djavanfernando.baixae.core.fakes

import br.com.djavanfernando.baixae.core.Configuracoes
import br.com.djavanfernando.baixae.core.portas.Conectividade
import br.com.djavanfernando.baixae.core.portas.EstadoDaRede
import br.com.djavanfernando.baixae.core.portas.Preferencias
import br.com.djavanfernando.baixae.core.portas.VerificadorDeVersoes
import br.com.djavanfernando.baixae.core.portas.VersaoPublicada
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class VerificadorDeVersoesFalso : VerificadorDeVersoes {
    var publicada: VersaoPublicada? = null
    var falhar = false

    override suspend fun ultimaVersao(): VersaoPublicada? {
        if (falhar) throw java.io.IOException("sem rede")
        return publicada
    }
}

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
