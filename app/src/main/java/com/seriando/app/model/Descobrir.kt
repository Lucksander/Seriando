package com.seriando.app.model

import com.google.gson.annotations.SerializedName

/**
 * Tipo de obra escolhido no primeiro menu da aba Descobrir.
 *
 * @param valorTmdb valor usado pela TMDB nos endpoints ("movie" ou "tv");
 *                  também é o tipoMidia que o app grava nos itens.
 * @param rotulo    texto exibido no menu, em português.
 */
enum class TipoDescoberta(val valorTmdb: String, val rotulo: String) {
    FILMES("movie", "Filmes"),
    SERIES("tv", "Séries")
}

/**
 * Catálogo escolhido no segundo menu da aba Descobrir.
 * É uma escolha única: Popular, Novos ou um serviço de streaming.
 */
sealed class CatalogoDescoberta(val rotulo: String) {
    // Ordenado por popularidade, sem filtro de streaming
    data object Popular : CatalogoDescoberta("Popular")

    // Ordenado por data de lançamento, do mais recente para o mais antigo
    data object Novos : CatalogoDescoberta("Novos")

    // Popular dentro de um serviço de streaming do Brasil (só assinatura)
    data class Streaming(val provedor: ProvedorStreaming) : CatalogoDescoberta(provedor.nome)
}

/** Gênero devolvido por genre/movie/list e genre/tv/list (terceiro menu). */
data class Genero(
    val id: Int,
    @SerializedName("name") val nome: String
)

/** Envelope da lista de gêneros da TMDB. */
data class GenerosResponse(
    @SerializedName("genres") val generos: List<Genero> = emptyList()
)

/** Serviço de streaming (Netflix, Prime Video etc.) devolvido por watch/provider. */
data class ProvedorStreaming(
    @SerializedName("provider_id") val id: Int,
    @SerializedName("provider_name") val nome: String,
    @SerializedName("logo_path") val caminhoLogo: String? = null,
    // Quanto menor, mais em destaque o serviço está na região consultada
    @SerializedName("display_priority") val prioridade: Int = 0
)

/** Envelope da lista de provedores da TMDB. */
data class ProvedoresResponse(
    @SerializedName("results") val resultados: List<ProvedorStreaming> = emptyList()
)