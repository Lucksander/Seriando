package com.seriando.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes unitários (JVM, sem emulador) da busca, do filtro e da ordenação da Minha Lista (RF66).
 */
class OrganizadorDaListaTest {

    /** Atalho para montar um item da lista. */
    private fun item(id: Int, nome: String, status: String, tipo: String = "tv", data: Long = id.toLong()) =
        SerieSalva(id = id, nome = nome, tipoMidia = tipo, status = status, dataAdicao = data)

    // Lista de exemplo: ids em ordem de adição (id maior = adicionado depois)
    private val lista = listOf(
        item(1, "Breaking Bad", "COMPLETO"),
        item(2, "Ação Explosiva", "QUERO_ASSISTIR", tipo = "movie"),
        item(3, "the Office", "ASSISTINDO"),
        item(4, "Zodíaco", "ASSISTIDO", tipo = "movie"),
        item(5, "Arcane", "ABANDONEI")
    )

    private fun ids(resultado: List<SerieSalva>) = resultado.map { it.id }

    // ---------- ordenação ----------

    @Test
    fun maisRecentesMostraOUltimoAdicionadoPrimeiro() {
        val r = organizarMinhaLista(lista, null, "", OrdemDaLista.RECENTES)
        assertEquals(listOf(5, 4, 3, 2, 1), ids(r))
    }

    @Test
    fun ordemPorNomeIgnoraMaiusculasEAcentos() {
        // "Ação Explosiva" (acao...) vem antes de "Arcane"; "the Office" minúsculo entra entre B e Z
        val r = organizarMinhaLista(lista, null, "", OrdemDaLista.NOME)
        assertEquals(listOf(2, 5, 1, 3, 4), ids(r))
    }

    @Test
    fun ordemPorStatusSegueOCicloDeAcompanhamento() {
        // Quero assistir, assistindo, terminado (maratonei/concluído), abandonei
        val r = organizarMinhaLista(lista, null, "", OrdemDaLista.STATUS)
        assertEquals(listOf(2, 3, 1, 4, 5), ids(r))
    }

    @Test
    fun dentroDoMesmoStatusDesempataPorNome() {
        val doisAssistindo = listOf(
            item(1, "Zeta", "ASSISTINDO"),
            item(2, "alfa", "ASSISTINDO")
        )
        val r = organizarMinhaLista(doisAssistindo, null, "", OrdemDaLista.STATUS)
        assertEquals(listOf(2, 1), ids(r))
    }

    // ---------- busca ----------

    @Test
    fun buscaEncontraPedacoDoNomeSemDiferenciarMaiusculas() {
        val r = organizarMinhaLista(lista, null, "OFFICE", OrdemDaLista.NOME)
        assertEquals(listOf(3), ids(r))
    }

    @Test
    fun buscaIgnoraAcentosNosDoisLados() {
        // Digitar sem acento acha "Ação" e "Zodíaco"; digitar com acento também
        assertEquals(listOf(2), ids(organizarMinhaLista(lista, null, "acao", OrdemDaLista.NOME)))
        assertEquals(listOf(4), ids(organizarMinhaLista(lista, null, "zodíaco", OrdemDaLista.NOME)))
    }

    @Test
    fun buscaIgnoraEspacosNasPontas() {
        assertEquals(listOf(5), ids(organizarMinhaLista(lista, null, "  arcane ", OrdemDaLista.NOME)))
    }

    @Test
    fun buscaVaziaOuSoComEspacosNaoFiltraNada() {
        assertEquals(5, organizarMinhaLista(lista, null, "", OrdemDaLista.NOME).size)
        assertEquals(5, organizarMinhaLista(lista, null, "   ", OrdemDaLista.NOME).size)
    }

    @Test
    fun buscaSemResultadoDevolveListaVazia() {
        assertEquals(emptyList<SerieSalva>(), organizarMinhaLista(lista, null, "xyz", OrdemDaLista.NOME))
    }

    // ---------- filtro de status junto com a busca ----------

    @Test
    fun filtroDeStatusUsaORotuloDoTipoDeMidia() {
        // "Concluído" é o rótulo de filme (ASSISTIDO); "Maratonei" é o de série (COMPLETO)
        assertEquals(listOf(4), ids(organizarMinhaLista(lista, "Concluído", "", OrdemDaLista.NOME)))
        assertEquals(listOf(1), ids(organizarMinhaLista(lista, "Maratonei", "", OrdemDaLista.NOME)))
    }

    @Test
    fun filtroEBuscaFuncionamJuntos() {
        // Só os "Quero assistir" cujo nome contém "ex": "Ação Explosiva" entra, os demais não
        val r = organizarMinhaLista(lista, "Quero assistir", "ex", OrdemDaLista.NOME)
        assertEquals(listOf(2), ids(r))
        // Mesmo texto com outro status: nada
        assertEquals(emptyList<SerieSalva>(), organizarMinhaLista(lista, "Assistindo", "ex", OrdemDaLista.NOME))
    }

    @Test
    fun listaVaziaContinuaVazia() {
        assertEquals(emptyList<SerieSalva>(), organizarMinhaLista(emptyList(), null, "a", OrdemDaLista.STATUS))
    }
}
