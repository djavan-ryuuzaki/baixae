package br.com.djavanfernando.baixae.core.fakes

import br.com.djavanfernando.baixae.core.Acervo
import br.com.djavanfernando.baixae.core.AtualizacoesDoApp
import br.com.djavanfernando.baixae.core.Baixador
import br.com.djavanfernando.baixae.core.VerificacaoSemanal
import br.com.djavanfernando.baixae.core.Compartilhamento
import br.com.djavanfernando.baixae.core.Extratores
import br.com.djavanfernando.baixae.core.FilaDeDownloads
import br.com.djavanfernando.baixae.core.VersaoSemSom

/** Núcleo montado com todos os fakes, para testes que atravessam vários serviços. */
class Cenario {
    val armazenamento = ArmazenamentoFalso()
    val extrator = ExtratorFalso(armazenamento)
    val repositorio = RepositorioEmMemoria()
    val relogio = RelogioFixo()
    val conectividade = ConectividadeFalsa()
    val preferencias = PreferenciasEmMemoria()
    val acervo = Acervo(repositorio, armazenamento)
    val baixador = Baixador(extrator, armazenamento, repositorio, relogio)
    val processador = ProcessadorFalso(armazenamento)
    val compartilhamento = Compartilhamento(repositorio, armazenamento, preferencias, processador)
    val versaoSemSom = VersaoSemSom(repositorio, armazenamento, processador, relogio)
    val fila = FilaDeDownloads(repositorio, baixador, conectividade, preferencias, relogio)
    val extratores = Extratores(extrator, fila, preferencias, relogio)
    val verificadorDeVersoes = VerificadorDeVersoesFalso()
    val atualizacoesDoApp = AtualizacoesDoApp(verificadorDeVersoes, preferencias, versaoInstalada = "0.1.0")
    val verificacaoSemanal = VerificacaoSemanal(extratores, atualizacoesDoApp)
}
