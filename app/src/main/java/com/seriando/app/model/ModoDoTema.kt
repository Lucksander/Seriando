package com.seriando.app.model

/**
 * Modo do tema escolhido pelo usuário na aba Conta (RF80).
 *
 * É uma preferência só do aparelho (guardada no DataStore, nunca vai ao Firestore) e não
 * depende do login: vale também para o visitante.
 *
 * @param chave  texto gravado no DataStore (não mude depois de publicado, senão a escolha se perde).
 * @param rotulo texto mostrado no botão de escolha.
 */
enum class ModoDoTema(val chave: String, val rotulo: String) {
    /** Segue o modo claro/escuro do aparelho (comportamento padrão). */
    SISTEMA("sistema", "Aparelho"),

    /** Sempre claro, mesmo que o aparelho esteja no modo escuro. */
    CLARO("claro", "Claro"),

    /** Sempre escuro, mesmo que o aparelho esteja no modo claro. */
    ESCURO("escuro", "Escuro");

    /**
     * true se o app deve usar o tema escuro com esta escolha.
     *
     * @param sistemaEscuro true se o aparelho está no modo escuro; só vale para [SISTEMA].
     */
    fun escuro(sistemaEscuro: Boolean): Boolean = when (this) {
        SISTEMA -> sistemaEscuro
        CLARO -> false
        ESCURO -> true
    }

    companion object {
        /** Converte o texto gravado de volta no modo; ausente ou desconhecido vira [SISTEMA]. */
        fun deChave(chave: String?): ModoDoTema = entries.find { it.chave == chave } ?: SISTEMA
    }
}
