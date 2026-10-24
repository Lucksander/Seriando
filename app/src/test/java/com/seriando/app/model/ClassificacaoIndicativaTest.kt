package com.seriando.app.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Testes unitários (JVM, sem emulador) da leitura da classificação indicativa do Brasil (RF49, RN14).
 */
class ClassificacaoIndicativaTest {

    /** Monta uma resposta de filme com as certificações informadas para um país. */
    private fun filme(pais: String, vararg certificacoes: String?) = ReleaseDatesResponse(
        resultados = listOf(
            ReleaseDatesPais(pais = pais, datas = certificacoes.map { ReleaseDateItem(it) })
        )
    )

    /** Monta uma resposta de série com uma classificação por país. */
    private fun serie(vararg paisEClassificacao: Pair<String, String?>) = ContentRatingsResponse(
        resultados = paisEClassificacao.map { ContentRatingItem(it.first, it.second) }
    )

    // ---------- filmes (release_dates) ----------

    @Test
    fun filmeComUmaCertificacaoNoBrasil() {
        val resultado = ClassificacaoIndicativa.doFilme(filme("BR", "12"))
        assertEquals(ClassificacaoIndicativa("12", 12), resultado)
    }

    @Test
    fun filmeComVariosLancamentosUsaAMaisRestritiva() {
        // Vários lançamentos no Brasil: vale a maior idade; certificação vazia é ignorada
        val resultado = ClassificacaoIndicativa.doFilme(filme("BR", "", "12", "16", null))
        assertEquals(16, resultado?.idadeMinima)
        assertEquals("16", resultado?.rotulo)
    }

    @Test
    fun filmeIgnoraOutrosPaises() {
        // Só existe classificação dos EUA: para o Brasil, é como se não houvesse
        assertNull(ClassificacaoIndicativa.doFilme(filme("US", "R")))
    }

    @Test
    fun filmeSoComCertificacaoVaziaNoBrasilNaoTemClassificacao() {
        assertNull(ClassificacaoIndicativa.doFilme(filme("BR", "", null)))
    }

    @Test
    fun filmeComRespostaNulaOuVaziaNaoTemClassificacao() {
        assertNull(ClassificacaoIndicativa.doFilme(null))
        assertNull(ClassificacaoIndicativa.doFilme(ReleaseDatesResponse()))
    }

    // ---------- séries (content_ratings) ----------

    @Test
    fun serieComClassificacaoNoBrasil() {
        val resultado = ClassificacaoIndicativa.daSerie(serie("US" to "TV-MA", "BR" to "14"))
        assertEquals(ClassificacaoIndicativa("14", 14), resultado)
    }

    @Test
    fun serieSemBrasilNaoTemClassificacao() {
        assertNull(ClassificacaoIndicativa.daSerie(serie("US" to "TV-MA")))
    }

    @Test
    fun serieComRespostaNulaOuVaziaNaoTemClassificacao() {
        assertNull(ClassificacaoIndicativa.daSerie(null))
        assertNull(ClassificacaoIndicativa.daSerie(ContentRatingsResponse()))
    }

    // ---------- rótulos aceitos e ignorados ----------

    @Test
    fun rotuloLTemIdadeMinimaZero() {
        val resultado = ClassificacaoIndicativa.daSerie(serie("BR" to "L"))
        assertEquals(ClassificacaoIndicativa("L", 0), resultado)
    }

    @Test
    fun rotuloEmMinusculasEComEspacosEhNormalizado() {
        val resultado = ClassificacaoIndicativa.daSerie(serie("BR" to " l "))
        assertEquals("L", resultado?.rotulo)
    }

    @Test
    fun rotuloForaDaListaDoBrasilEhIgnorado() {
        // "13" e "R" não existem na classificação brasileira
        assertNull(ClassificacaoIndicativa.daSerie(serie("BR" to "13")))
        assertNull(ClassificacaoIndicativa.daSerie(serie("BR" to "R")))
        assertNull(ClassificacaoIndicativa.daSerie(serie("BR" to null)))
    }

    @Test
    fun todosOsRotulosValidosTemAIdadeCerta() {
        val esperado = mapOf("L" to 0, "10" to 10, "12" to 12, "14" to 14, "16" to 16, "18" to 18)
        for ((rotulo, idade) in esperado) {
            val resultado = ClassificacaoIndicativa.daSerie(serie("BR" to rotulo))
            assertEquals("idade de $rotulo", idade, resultado?.idadeMinima)
        }
    }

    // ---------- leitura do JSON da TMDB (confere os @SerializedName) ----------

    @Test
    fun lerJsonDeFilmeDaTmdb() {
        val json = """
            {"results":[
              {"iso_3166_1":"US","release_dates":[{"certification":"PG-13"}]},
              {"iso_3166_1":"BR","release_dates":[{"certification":""},{"certification":"14"}]}
            ]}
        """.trimIndent()
        val resposta = Gson().fromJson(json, ReleaseDatesResponse::class.java)
        assertNotNull(resposta)
        assertEquals(14, ClassificacaoIndicativa.doFilme(resposta)?.idadeMinima)
    }

    @Test
    fun lerJsonDeSerieDaTmdb() {
        val json = """{"results":[{"iso_3166_1":"BR","rating":"16"},{"iso_3166_1":"US","rating":"TV-14"}]}"""
        val resposta = Gson().fromJson(json, ContentRatingsResponse::class.java)
        assertEquals(16, ClassificacaoIndicativa.daSerie(resposta)?.idadeMinima)
    }
}
