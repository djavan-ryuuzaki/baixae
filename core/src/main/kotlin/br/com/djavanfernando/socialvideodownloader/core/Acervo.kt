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

    /** Renomeia o Vídeo e o arquivo no celular; recusa nomes vazios ou já existentes na pasta. */
    suspend fun renomear(videoId: String, novoNome: String): Video {
        val video = repositorio.video(videoId) ?: throw VideoNaoEncontrado(videoId)
        val nome = NomeDeVideo.sanitizar(novoNome)
        if (nome.isEmpty()) throw NomeInvalido(novoNome)
        if (nome == video.nome) return video
        val nomeDoArquivo = "$nome.${Video.EXTENSAO}"
        if (armazenamento.existeNaPasta(video.pasta, nomeDoArquivo)) throw NomeJaExiste(nome)
        val caminho = armazenamento.renomear(video.caminhoDoArquivo, nomeDoArquivo)
        return video.copy(nome = nome, caminhoDoArquivo = caminho).also { repositorio.salvarVideo(it) }
    }
}

class VideoNaoEncontrado(id: String) : NoSuchElementException("Vídeo não encontrado: $id")

class NomeInvalido(nome: String) : IllegalArgumentException("Nome inválido: $nome")

class NomeJaExiste(val nome: String) : IllegalArgumentException("Já existe um arquivo chamado $nome")
