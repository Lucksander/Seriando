package com.seriando.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários (JVM, sem emulador) da escolha das bases e da combinação das
 * recomendações da Home (RF81).
 */
class RecomendacoesTest {

    private fun salva(id: Int, tipo: String, status: String, data: Long) =
        SerieSalva(id = id, nome = "Obra $id", tipoMidia = tipo, status = status, dataAdicao = data)

    private fun obra(id: Int, tipo: String = "movie") = ResultadoBusca(id = id, tipoMidia = tipo)

    // ---------- bases ----------

    @Test
    fun basesSaoSoObrasTerminadasDaMaisRecenteParaAMaisAntiga() {
        val lista = listOf(
            salva(1, "tv", "COMPLETO", 10),
            salva(2, "tv", "ASSISTINDO", 20),      // não terminou
            salva(3, "movie", "ASSISTIDO", 30),
            salva(4, "movie", "QUERO_ASSISTIR", 40), // não terminou
            salva(5, "tv", "ABANDONEI", 50)          // abandonada não conta
        )
        assertEquals(listOf(3, 1), Recomendacoes.escolherBases(lista).map { it.id })
    }

    @Test
    fun basesRespeitamOMaximo() {
        val lista = (1..8).map { salva(it, "tv", "COMPLETO", it.toLong()) }
        assertEquals(listOf(8, 7, 6), Recomendacoes.escolherBases(lista, maximo = 3).map { it.id })
    }

    @Test
    fun semObrasTerminadasNaoHaBases() {
        assertTrue(Recomendacoes.escolherBases(listOf(salva(1, "tv", "ASSISTINDO", 1))).isEmpty())
    }

    // ---------- combinação ----------

    @Test
    fun obraRecomendadaPorMaisBasesSobe() {
        // A obra 9 aparece nas duas listas e deve vir primeiro, mesmo chegando depois
        val r = Recomendacoes.combinar(
            listOf(listOf(obra(1), obra(9)), listOf(obra(2), obra(9))),
            jaNaLista = emptySet()
        )
        assertEquals(listOf(9, 1, 2), r.map { it.id })
    }

    @Test
    fun noEmpateVaiPelaOrdemDeChegada() {
        val r = Recomendacoes.combinar(listOf(listOf(obra(3), obra(1)), listOf(obra(2))), emptySet())
        assertEquals(listOf(3, 1, 2), r.map { it.id })
    }

    @Test
    fun tiraObrasQueJaEstaoNaLista() {
        val r = Recomendacoes.combinar(
            listOf(listOf(obra(1), obra(2, "tv"))),
            jaNaLista = setOf("movie" to 1)
        )
        assertEquals(listOf(2), r.map { it.id })
    }

    @Test
    fun filmeESerieComOMesmoIdSaoObrasDiferentes() {
        val r = Recomendacoes.combinar(
            listOf(listOf(obra(7, "movie"), obra(7, "tv"))),
            jaNaLista = setOf("movie" to 7)
        )
        assertEquals(listOf("tv"), r.map { it.tipoMidia })
    }

    @Test
    fun umaBaseSoVotaUmaVezEmCadaObra() {
        // A obra 5 repetida na mesma lista não pode ganhar de uma que duas bases recomendaram
        val r = Recomendacoes.combinar(
            listOf(listOf(obra(5), obra(5)), listOf(obra(6)), listOf(obra(6))),
            emptySet()
        )
        assertEquals(listOf(6, 5), r.map { it.id })
    }

    @Test
    fun respeitaOLimite() {
        val r = Recomendacoes.combinar(listOf((1..30).map { obra(it) }), emptySet(), limite = 20)
        assertEquals(20, r.size)
    }
}
