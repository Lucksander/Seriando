package com.seriando.app.data.remote

import android.content.Context
import android.util.Log
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException

/**
 * Ponto único de acesso à API da TMDB (Retrofit).
 *
 * Cache em disco (RNF13): as respostas de algumas rotas de catálogo ficam guardadas por um
 * tempo (ver [validadeEmSegundos]) e, enquanto valem, a TMDB não é consultada de novo. Os
 * detalhes das obras NÃO entram aqui: eles têm o próprio cache no Room (ObraCacheRepository).
 * A chave do cache é a URL completa, então parâmetros diferentes (página, gênero, filtros)
 * nunca se misturam, e só dados públicos da TMDB são guardados.
 */
object RetrofitInstance {

    private const val TAG = "RetrofitInstance"
    private const val TAMANHO_CACHE_BYTES = 10L * 1024 * 1024 // 10 MB em disco
    private const val HORA = 60 * 60 // uma hora, em segundos

    // Erro 429 (muitas requisições): quantas vezes tentar de novo e a espera máxima aceita
    private const val MAX_TENTATIVAS_429 = 2
    private const val ESPERA_MAXIMA_429_MS = 5_000L

    // Cache HTTP em disco. Fica nulo até iniciar() ser chamado; sem ele o app funciona
    // normalmente, só que sem guardar respostas.
    private var cacheHttp: Cache? = null

    /**
     * Cria o cache em disco. Chamado uma única vez em SeriandoApp.onCreate, ANTES de qualquer
     * uso de [api] (que é criado de forma preguiçosa, justamente para enxergar o cache).
     */
    fun iniciar(contexto: Context) {
        cacheHttp = Cache(File(contexto.applicationContext.cacheDir, "tmdb_http"), TAMANHO_CACHE_BYTES)
    }

    /**
     * Esvazia o cache em disco. Usado pelo "puxar para atualizar" da Home, para a próxima
     * busca trazer dados novos da TMDB em vez de respostas guardadas.
     */
    fun limparCache() {
        try {
            cacheHttp?.evictAll()
        } catch (e: IOException) {
            Log.d(TAG, "Não foi possível limpar o cache HTTP: ${e.message}")
        }
    }

    /**
     * Por quantos segundos a resposta de uma rota vale sem consultar a TMDB.
     * Devolve null para as rotas que NÃO devem ser guardadas aqui (busca, detalhes da obra,
     * temporadas), seja porque mudam a cada consulta, seja porque o Room já as guarda.
     */
    private fun validadeEmSegundos(caminho: String): Int? = when {
        caminho.startsWith("/3/trending/") -> 3 * HORA          // Em alta (Home)
        caminho.startsWith("/3/discover/") -> 3 * HORA          // categorias e aba Descobrir
        caminho.contains("/recommendations") -> 6 * HORA        // recomendações (RF81)
        caminho.startsWith("/3/genre/") -> 24 * HORA            // lista de gêneros
        caminho.startsWith("/3/watch/providers/") -> 24 * HORA  // lista de streamings da Descobrir
        caminho.startsWith("/3/person/") -> 24 * HORA           // página do ator/atriz
        caminho.endsWith("/release_dates") -> 24 * HORA         // classificação indicativa (filme)
        caminho.endsWith("/content_ratings") -> 24 * HORA       // classificação indicativa (série)
        else -> null
    }

    /**
     * Interceptador de rede: reescreve o cabeçalho Cache-Control da resposta antes de ela ir
     * para o cache. Rotas da lista acima recebem "max-age" (guardar pelo tempo definido); todas
     * as outras, e qualquer resposta com erro, recebem "no-store" (nunca guardar). Assim o
     * comportamento é sempre o nosso, não o que a TMDB resolver mandar.
     */
    private val politicaDeCache = Interceptor { chain ->
        val pedido = chain.request()
        val resposta = chain.proceed(pedido)
        val segundos = if (pedido.method == "GET") validadeEmSegundos(pedido.url.encodedPath) else null

        val controle = if (segundos != null && resposta.isSuccessful) {
            "public, max-age=$segundos"
        } else {
            "no-store"
        }
        resposta.newBuilder()
            .removeHeader("Pragma")
            .header("Cache-Control", controle)
            .build()
    }

    /**
     * Interceptador de aplicação: se a TMDB responder 429 (muitas requisições), espera e tenta
     * de novo, até [MAX_TENTATIVAS_429] vezes. A espera é a do cabeçalho Retry-After (em
     * segundos); sem ele, 1 s na primeira tentativa e 2 s na segunda. Se a espera pedida passar
     * de [ESPERA_MAXIMA_429_MS], não vale a pena segurar a tela: devolve o 429 como está e o
     * app trata como falha comum.
     */
    private val novaTentativaEm429 = Interceptor { chain ->
        val pedido = chain.request()
        var resposta = chain.proceed(pedido)
        var tentativa = 0

        while (resposta.code == 429 && tentativa < MAX_TENTATIVAS_429) {
            val segundos = resposta.header("Retry-After")?.toLongOrNull() ?: (1L shl tentativa)
            val esperaMs = segundos * 1000
            if (esperaMs > ESPERA_MAXIMA_429_MS) break

            resposta.close() // libera a resposta antiga antes de pedir de novo
            try {
                Thread.sleep(esperaMs)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt() // preserva o sinal de interrupção
                throw IOException("Espera interrompida antes de tentar de novo")
            }
            tentativa++
            resposta = chain.proceed(pedido)
        }
        resposta
    }

    // Cliente HTTP criado de forma preguiçosa (só no primeiro uso de api), depois de iniciar()
    private val cliente by lazy {
        val construtor = OkHttpClient.Builder().addInterceptor(novaTentativaEm429)
        cacheHttp?.let { construtor.cache(it).addNetworkInterceptor(politicaDeCache) }
        construtor.build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: TmdbApiService by lazy {
        retrofit.create(TmdbApiService::class.java)
    }
}