package br.com.djavanfernando.baixae.core.portas

import br.com.djavanfernando.baixae.core.Configuracoes
import kotlinx.coroutines.flow.Flow

interface Preferencias {
    suspend fun ler(): Configuracoes
    suspend fun salvar(configuracoes: Configuracoes)
    fun observar(): Flow<Configuracoes>
}
