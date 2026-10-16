package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio
import java.util.UUID

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

    /** Vídeos de uma Biblioteca, do mais recente ao mais antigo, com filtro por Plataforma e busca opcionais. */
    suspend fun videosDa(bibliotecaId: String, plataforma: Plataforma? = null, busca: String = ""): List<Video> =
        ConsultaDeVideos(bibliotecaId, plataforma, busca).aplicar(repositorio.videos())

    /** Vídeo já baixado do mesmo Link (o mais recente), com a Biblioteca onde ele está agora. */
    suspend fun downloadAnterior(link: String): LinkRepetido? {
        val chave = ChaveDoLink.de(link)
        val video = repositorio.videos()
            .filter { ChaveDoLink.de(it.link) == chave }
            .maxByOrNull { it.dataDoDownload } ?: return null
        val biblioteca = repositorio.bibliotecas().firstOrNull { it.id == video.bibliotecaId } ?: return null
        return LinkRepetido(video, biblioteca)
    }

    /** Renomeia o Vídeo e o arquivo no celular; recusa nomes vazios ou já existentes na pasta. */
    suspend fun renomear(videoId: String, novoNome: String): Video {
        val video = repositorio.video(videoId) ?: throw VideoNaoEncontrado(videoId)
        val nome = NomeDeVideo.sanitizar(novoNome)
        if (nome.isEmpty()) throw NomeInvalido(novoNome)
        if (nome == video.nome) return video
        if (armazenamento.existeNaPasta(video.pasta, Video.nomeDoArquivo(nome))) throw NomeJaExiste(nome)
        val salvo = armazenamento.renomear(video.caminhoDoArquivo, Video.nomeDoArquivo(nome))
        return video.copy(nome = Video.nomeSemExtensao(salvo.nomeDoArquivo), caminhoDoArquivo = salvo.caminho)
            .also { repositorio.salvarVideo(it) }
    }

    /** Tira o Vídeo do app e, se pedido, apaga também o arquivo do celular. */
    suspend fun apagarVideo(videoId: String, apagarArquivo: Boolean) {
        val video = repositorio.video(videoId) ?: return
        if (apagarArquivo) armazenamento.apagar(video.caminhoDoArquivo)
        repositorio.removerVideo(videoId)
    }

    /** Confere os arquivos dos Vídeos da Biblioteca e marca (ou desmarca) os que estão como Arquivo ausente. */
    suspend fun verificarArquivos(bibliotecaId: String) {
        repositorio.videos().filter { it.bibliotecaId == bibliotecaId }.forEach { video ->
            val ausente = !armazenamento.existe(video.caminhoDoArquivo)
            if (ausente != video.arquivoAusente) repositorio.salvarVideo(video.copy(arquivoAusente = ausente))
        }
    }

    suspend fun criarBiblioteca(nome: String): Biblioteca {
        val limpo = nomeDeBibliotecaLivre(nome, ignorar = null)
        return Biblioteca(UUID.randomUUID().toString(), limpo).also { repositorio.salvarBiblioteca(it) }
    }

    suspend fun renomearBiblioteca(id: String, novoNome: String): Biblioteca {
        val biblioteca = bibliotecaPersonalizada(id)
        val limpo = nomeDeBibliotecaLivre(novoNome, ignorar = id)
        return biblioteca.copy(nome = limpo).also { repositorio.salvarBiblioteca(it) }
    }

    /** Apaga uma Biblioteca personalizada; cada Vídeo dela volta à Biblioteca padrão da sua Plataforma. */
    suspend fun apagarBiblioteca(id: String) {
        bibliotecaPersonalizada(id)
        repositorio.videos()
            .filter { it.bibliotecaId == id }
            .forEach { repositorio.salvarVideo(it.copy(bibliotecaId = Biblioteca.idPadrao(it.plataforma))) }
        repositorio.removerBiblioteca(id)
    }

    /** Move Vídeos para outra Biblioteca. Só a organização no app muda; os arquivos ficam onde estão. */
    suspend fun mover(videoIds: Collection<String>, bibliotecaId: String) {
        if (repositorio.bibliotecas().none { it.id == bibliotecaId }) throw BibliotecaNaoEncontrada(bibliotecaId)
        videoIds.mapNotNull { repositorio.video(it) }
            .forEach { repositorio.salvarVideo(it.copy(bibliotecaId = bibliotecaId)) }
    }

    private suspend fun bibliotecaPersonalizada(id: String): Biblioteca {
        val biblioteca = repositorio.bibliotecas().firstOrNull { it.id == id } ?: throw BibliotecaNaoEncontrada(id)
        if (biblioteca.padrao) throw BibliotecaPadraoImutavel(biblioteca.nome)
        return biblioteca
    }

    private suspend fun nomeDeBibliotecaLivre(nome: String, ignorar: String?): String {
        val limpo = nome.replace(Regex("""\s+"""), " ").trim()
        if (limpo.isEmpty()) throw NomeInvalido(nome)
        val emUso = repositorio.bibliotecas().any { it.id != ignorar && it.nome.equals(limpo, ignoreCase = true) }
        if (emUso) throw BibliotecaJaExiste(limpo)
        return limpo
    }
}

data class LinkRepetido(val video: Video, val biblioteca: Biblioteca)

class VideoNaoEncontrado(id: String) : NoSuchElementException("Vídeo não encontrado: $id")

class NomeInvalido(nome: String) : IllegalArgumentException("Nome inválido: $nome")

class NomeJaExiste(val nome: String) : IllegalArgumentException("Já existe um arquivo chamado $nome")

class BibliotecaNaoEncontrada(id: String) : NoSuchElementException("Biblioteca não encontrada: $id")

class BibliotecaJaExiste(val nome: String) : IllegalArgumentException("Já existe uma Biblioteca chamada $nome")

class BibliotecaPadraoImutavel(nome: String) :
    IllegalStateException("A Biblioteca padrão $nome não pode ser renomeada nem apagada")
