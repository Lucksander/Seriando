package com.seriando.app.data.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Aviso único de que a idade do usuário mudou (RF50).
 *
 * As listas da Home, da busca e da Descobrir são guardadas já filtradas pela idade. Quando a
 * idade muda (login, logout, data de nascimento salva ou baixada da nuvem), o que está guardado
 * deixa de valer: [idadeMudou] apaga os caches em memória e sobe o número da [versao].
 * As telas que estão abertas observam a [versao] e recarregam o que mostram.
 */
object VersaoDaIdade {

    private val _versao = MutableStateFlow(0)

    /** Sobe a cada mudança de idade. As telas usam como chave dos efeitos que carregam listas. */
    val versao: StateFlow<Int> = _versao.asStateFlow()

    /** Apaga as listas guardadas em memória e avisa as telas. Pode ser chamada de qualquer thread. */
    fun idadeMudou() {
        CatalogoCache.limpar()
        BuscaCache.textoPesquisa = ""
        BuscaCache.resultados = emptyList()
        _versao.update { it + 1 } // update é seguro entre threads
    }
}