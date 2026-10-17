package br.com.djavanfernando.baixae.core

/** Motivo pelo qual o Extrator não conseguiu obter um vídeo. */
enum class TipoDeFalha {
    /** A Plataforma mudou e o Extrator precisa ser atualizado. */
    PLATAFORMA_MUDOU,

    /** Post privado, restrito por idade ou story: exige login, que não é suportado. */
    EXIGE_LOGIN,
    SEM_REDE,
    SEM_ESPACO,
    OUTRO,
}

class FalhaDoExtrator(val tipo: TipoDeFalha, mensagem: String? = null) : Exception(mensagem ?: tipo.name)
