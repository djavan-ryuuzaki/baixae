package br.com.djavanfernando.socialvideodownloader.core.fakes

import br.com.djavanfernando.socialvideodownloader.core.Acervo
import br.com.djavanfernando.socialvideodownloader.core.Baixador

/** Núcleo montado com todos os fakes, para testes que atravessam vários serviços. */
class Cenario {
    val armazenamento = ArmazenamentoFalso()
    val extrator = ExtratorFalso(armazenamento)
    val repositorio = RepositorioEmMemoria()
    val relogio = RelogioFixo()
    val acervo = Acervo(repositorio, armazenamento)
    val baixador = Baixador(extrator, armazenamento, repositorio, relogio)
}
