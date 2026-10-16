package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio

/** Vídeos e Bibliotecas registrados no app. */
class Acervo(
    private val repositorio: Repositorio,
    private val armazenamento: ArmazenamentoDeArquivos,
) {
    /** Garante que exista uma Biblioteca padrão por Plataforma. */
    suspend fun iniciar() {
        val existentes = repositorio.bibliotecas().map { it.id }.toSet()
        Plataforma.entries
            .map(Biblioteca::padrao)
            .filter { it.id !in existentes }
            .forEach { repositorio.salvarBiblioteca(it) }
    }

    suspend fun bibliotecas(): List<Biblioteca> = repositorio.bibliotecas()

    suspend fun video(id: String): Video? = repositorio.video(id)

    suspend fun videosDa(bibliotecaId: String): List<Video> =
        repositorio.videos().filter { it.bibliotecaId == bibliotecaId }
}
