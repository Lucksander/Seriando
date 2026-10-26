package com.seriando.app.data.repository

import com.seriando.app.data.auth.AuthRepository
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.seriando.app.BuildConfig
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.ClassificacaoIndicativa
import com.seriando.app.model.ControleDeIdade
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap

/**
 * Filtro por idade das listas de obras (RF50, RN13 a RN16). É o ÚNICO ponto da camada de
 * dados que decide o que fica oculto: Home, "Ver mais", Descobrir, busca e filmografia
 * chamam [filtrar] e não repetem a regra (RN15).
 *
 * A idade vem do perfil no banco local (Room), então não precisa de internet para ser calculada.
 * As listas da TMDB não trazem classificação; por isso, para quem é menor, a classificação de
 * cada obra é consultada separadamente, em paralelo e com limite de chamadas simultâneas, e
 * guardada em cache de memória (RNF13). Para quem tem 18 anos ou mais, nada é consultado.
 */
class FiltroDeIdadeRepository(
    private val perfilRepository: PerfilRepository = PerfilRepository()
) {

    /**
     * Idade efetiva do usuário atual: a real, se houver data de nascimento válida; senão 12,
     * a faixa restrita do visitante e de quem não informou a data (RN16).
     */
    suspend fun idadeEfetiva(): Int {
        // usuarioAtual() já trata conta de e-mail não confirmada como visitante (RF32)
        val uid = AuthRepository().usuarioAtual()?.uid
            ?: return ControleDeIdade.IDADE_RESTRITA // visitante
        val data = perfilRepository.buscarPerfilPrivado(uid)?.aniversario
        return ControleDeIdade.idadeEfetiva(data)
    }

    /**
     * Devolve só os itens que a idade do usuário permite, na mesma ordem.
     * Funciona com qualquer tipo de item (resultados de busca, filmografia...): quem chama
     * diz como ler o tipo ("movie" ou "tv") e o id de cada um.
     *
     * Se a classificação de uma obra não puder ser consultada (sem internet, por exemplo),
     * ela conta como "sem classificação" e fica oculta para menores: na dúvida, esconde.
     */
    suspend fun <T> filtrar(
        itens: List<T>,
        tipoDe: (T) -> String?,
        idDe: (T) -> Int
    ): List<T> {
        val idade = idadeEfetiva()
        // Maior de idade: tudo é permitido, sem consultar nada
        if (idade >= ControleDeIdade.MAIORIDADE) return itens

        // Consulta as classificações em paralelo (o semáforo limita quantas ao mesmo tempo)
        val classificacoes = coroutineScope {
            itens.map { item -> async { buscarClassificacao(tipoDe(item), idDe(item)) } }.awaitAll()
        }
        return itens.filterIndexed { posicao, _ ->
            ControleDeIdade.permitida(classificacoes[posicao], idade)
        }
    }

    /**
     * Controle de idade da tela de Detalhes (RF51): diz se esta obra pode ser aberta pelo usuário.
     * Usa a mesma regra do filtro das listas (ControleDeIdade), então os dois nunca discordam.
     *
     * @param classificacaoConhecida classificação que já veio nos detalhes da obra (pode ser null).
     * @param jaConsultada true se os detalhes trouxeram o campo da classificação, mesmo vazio
     *                     (null aí significa "a TMDB não tem classificação do Brasil", RN14).
     *                     false se a cópia dos detalhes é antiga e não traz o campo: nesse caso a
     *                     classificação é consultada à parte, com o cache de memória.
     */
    suspend fun obraPermitida(
        tipoMidia: String,
        id: Int,
        classificacaoConhecida: ClassificacaoIndicativa?,
        jaConsultada: Boolean
    ): Boolean {
        val idade = idadeEfetiva()
        // Maior de idade: tudo é permitido, sem consultar nada
        if (idade >= ControleDeIdade.MAIORIDADE) return true

        val classificacao = if (jaConsultada) classificacaoConhecida else buscarClassificacao(tipoMidia, id)
        return ControleDeIdade.permitida(classificacao, idade)
    }

    /**
     * Classificação do Brasil de uma obra, ou null se não houver (ou se a consulta falhar).
     * Respostas bem-sucedidas, inclusive "sem classificação", ficam no cache; falhas não,
     * para serem tentadas de novo na próxima vez.
     */
    private suspend fun buscarClassificacao(tipoMidia: String?, id: Int): ClassificacaoIndicativa? {
        if (tipoMidia != "movie" && tipoMidia != "tv") return null

        val chave = "${tipoMidia}_$id"
        cache[chave]?.let { return it.classificacao }

        return semaforo.withPermit {
            // Outra consulta pode ter guardado a resposta enquanto esta esperava a vez
            cache[chave]?.let { return@withPermit it.classificacao }
            try {
                val classificacao = if (tipoMidia == "movie") {
                    ClassificacaoIndicativa.doFilme(
                        RetrofitInstance.api.buscarClassificacaoFilme(id, BuildConfig.TMDB_API_KEY)
                    )
                } else {
                    ClassificacaoIndicativa.daSerie(
                        RetrofitInstance.api.buscarClassificacaoSerie(id, BuildConfig.TMDB_API_KEY)
                    )
                }
                cache[chave] = Consultada(classificacao)
                classificacao
            } catch (e: CancellationException) {
                throw e // a tela saiu: não é falha
            } catch (e: Exception) {
                Log.d(TAG, "Sem classificação de $chave por enquanto: ${e.message}")
                null
            }
        }
    }

    /** Resposta já consultada; o null dentro dela quer dizer "a TMDB não tem classificação do Brasil". */
    private class Consultada(val classificacao: ClassificacaoIndicativa?)

    private companion object {
        const val TAG = "FiltroDeIdade"

        // No máximo 10 consultas ao mesmo tempo, para não disparar centenas de chamadas
        // quando a Home carrega todas as categorias de uma vez
        val semaforo = Semaphore(10)

        // Cache em memória (não persistido) das classificações já consultadas
        val cache = ConcurrentHashMap<String, Consultada>()
    }
}