package com.seriando.app.model

import com.google.gson.annotations.SerializedName

// Detalhes de uma pessoa (ator/atriz), vindos do endpoint person/{id} da TMDB.
data class PessoaDetalhe(
    val id: Int,
    @SerializedName("name") val nome: String,
    @SerializedName("biography") val biografia: String = "",
    @SerializedName("profile_path") val caminhoFoto: String? = null,
    @SerializedName("birthday") val nascimento: String? = null, // formato "AAAA-MM-DD"
    @SerializedName("place_of_birth") val localNascimento: String? = null
)

// Resposta do endpoint person/{id}/combined_credits: tudo em que a pessoa
// atuou, misturando filmes e séries numa lista só.
data class CreditosPessoaResponse(
    val cast: List<CreditoPessoa> = emptyList()
)

// Um título da filmografia. Assim como na busca multi, filme usa "title" e
// série usa "name" — por isso os dois campos existem, e a propriedade
// "titulo" devolve o que estiver preenchido.
data class CreditoPessoa(
    val id: Int,
    @SerializedName("media_type") val tipoMidia: String? = null, // "movie" ou "tv"
    @SerializedName("title") private val tituloFilme: String? = null,
    @SerializedName("name") private val nomeSerie: String? = null,
    @SerializedName("poster_path") val caminhoCapa: String? = null,
    @SerializedName("character") val personagem: String? = null,
    val popularity: Double = 0.0
) {
    val titulo: String get() = tituloFilme ?: nomeSerie ?: "Sem título"
}