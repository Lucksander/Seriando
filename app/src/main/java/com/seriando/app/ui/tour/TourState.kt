package com.seriando.app.ui.tour

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Um passo do tour guiado: qual elemento da tela destacar e o que dizer sobre ele.
 *
 * @param alvoId   identificador do elemento. Precisa ser o mesmo usado em Modifier.alvoDoTour(id).
 * @param titulo   título curto do balão.
 * @param texto    explicação de uma ou duas frases.
 * @param aoMostrar ação opcional executada ANTES de destacar o alvo, por exemplo rolar a tela
 *                  até ele (necessário em listas, que só compõem os itens visíveis).
 *                  É suspend para poder esperar a animação de rolagem terminar.
 */
class PassoDoTour(
    val alvoId: String,
    val titulo: String,
    val texto: String,
    val aoMostrar: (suspend () -> Unit)? = null
)

/**
 * Estado do tour: a lista de passos, o passo atual e a posição de cada elemento na tela.
 *
 * Não usa ViewModel porque o projeto guarda o estado das telas em remember (ver arquitetura.md).
 * Quem cria o estado é [rememberTourState], que também o preserva na rotação da tela.
 *
 * @param passos        passos do tour, na ordem em que aparecem.
 * @param indiceInicial -1 = tour parado; 0 em diante = passo em exibição.
 */
class TourState(
    val passos: List<PassoDoTour>,
    indiceInicial: Int = -1
) {
    /** Índice do passo atual, ou -1 quando o tour não está rodando. Leitura observável pela UI. */
    var indice by mutableIntStateOf(indiceInicial)
        private set

    /** Quantos passos chegaram a ser exibidos de fato (passos sem alvo na tela são pulados). */
    var mostrados by mutableIntStateOf(0)
        private set

    /** true enquanto o tour está rodando. */
    val ativo: Boolean get() = indice in passos.indices

    /** Passo em exibição, ou null se o tour está parado. */
    val passoAtual: PassoDoTour? get() = passos.getOrNull(indice)

    /**
     * Retângulo de cada alvo, em coordenadas da raiz da janela, por alvoId.
     * É um mapa observável: o overlay se redesenha sozinho quando um alvo aparece ou se move
     * (por exemplo, durante a rolagem da tela).
     */
    val alvos = mutableStateMapOf<String, Rect>()

    /** Começa o tour pelo primeiro passo (não faz nada se a lista estiver vazia). */
    fun iniciar() {
        mostrados = 0
        indice = if (passos.isEmpty()) -1 else 0
    }

    /**
     * Vai para o próximo passo.
     * @return true se havia próximo passo; false se o tour acabou (e o estado volta a "parado").
     */
    fun avancar(): Boolean {
        val proximo = indice + 1
        return if (proximo in passos.indices) {
            indice = proximo
            true
        } else {
            indice = -1
            false
        }
    }

    /** Para o tour imediatamente (botão Pular ou tecla Voltar). */
    fun encerrar() {
        indice = -1
    }

    /** Chamado pelo overlay quando o alvo do passo atual foi encontrado e está sendo destacado. */
    fun registrarPassoMostrado() {
        mostrados++
    }
}

/**
 * Cria o [TourState] e o preserva na rotação da tela (só o índice do passo é salvo).
 *
 * A lista de passos é lida na primeira composição e reaproveitada depois; por isso deve ser
 * montada de forma estável (os textos não devem mudar enquanto o tour está aberto).
 */
@Composable
fun rememberTourState(passos: List<PassoDoTour>): TourState =
    rememberSaveable(
        saver = listSaver<TourState, Int>(
            save = { estado -> listOf(estado.indice) },
            restore = { salvo -> TourState(passos, salvo[0]) }
        )
    ) { TourState(passos) }

/**
 * Entrega o [TourState] às telas por baixo de quem o criou, sem passar parâmetro em cada função.
 * Vale null fora do tour (telas em preview, por exemplo), e nesse caso alvoDoTour não faz nada.
 */
val LocalTour = staticCompositionLocalOf<TourState?> { null }

/**
 * Marca um elemento como alvo do tour: guarda a posição dele no [TourState] a cada layout.
 * Quando o elemento sai da composição (some da tela), o registro é removido.
 *
 * Uso: Modifier.alvoDoTour("busca") no elemento que deve ser destacado.
 *
 * @param id mesmo valor de [PassoDoTour.alvoId] do passo correspondente.
 */
@Composable
fun Modifier.alvoDoTour(id: String): Modifier {
    // Sem tour disponível, o modificador é neutro
    val tour = LocalTour.current ?: return this

    // Ao sair da composição, apaga a posição para o passo não destacar um elemento que não existe mais
    DisposableEffect(tour, id) {
        onDispose { tour.alvos.remove(id) }
    }

    return this.onGloballyPositioned { coordenadas ->
        if (coordenadas.isAttached) {
            // boundsInRoot já vem recortado pela área visível dos pais: um alvo totalmente
            // rolado para fora da tela chega com retângulo vazio
            val retangulo = coordenadas.boundsInRoot()
            // Só grava se mudou, para não provocar redesenho à toa
            if (tour.alvos[id] != retangulo) tour.alvos[id] = retangulo
        }
    }
}
