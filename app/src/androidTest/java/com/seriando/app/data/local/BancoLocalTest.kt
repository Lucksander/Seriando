package com.seriando.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes do banco local (Room) do modo offline (RF33, RN21 a RN25).
 * Usam um banco em memória, criado do zero a cada teste, então não mexem nos dados do app.
 * Rodam em emulador ou aparelho: botão direito na classe > Run 'BancoLocalTest'.
 */
@RunWith(AndroidJUnit4::class)
class BancoLocalTest {

    private lateinit var banco: SeriandoDatabase

    @Before
    fun criarBanco() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        banco = Room.inMemoryDatabaseBuilder(contexto, SeriandoDatabase::class.java)
            .allowMainThreadQueries() // só nos testes, para usar runBlocking sem complicação
            .build()
    }

    @After
    fun fecharBanco() {
        banco.close()
    }

    // ---------- Funções auxiliares para montar linhas de teste ----------

    private fun item(
        id: Int, atualizadoEm: Long, pendente: Boolean = true, removido: Boolean = false,
        tipo: String = "tv", status: String = "QUERO_ASSISTIR"
    ) =
        ListaEntity(
            id = id, nome = "Obra $id", sinopse = "", caminhoCapa = null, tipoMidia = tipo,
            status = status, dataAdicao = 1L, atualizadoEm = atualizadoEm,
            pendenteSincronizacao = pendente, removido = removido,
        )

    private fun episodio(serieId: Int, temporada: Int, numero: Int, atualizadoEm: Long = 10L, removido: Boolean = false) =
        EpisodioAssistidoEntity(
            serieId = serieId, temporada = temporada, numero = numero, atualizadoEm = atualizadoEm,
            pendenteSincronizacao = true, removido = removido,
        )

    // ---------- Lista ----------

    @Test
    fun itemRemovidoSomeDaTelaMasContinuaPendente() {
        runBlocking {
            val dao = banco.listaDao()
            dao.salvar(item(id = 1, atualizadoEm = 100))

            dao.marcarRemovido(id = 1, agora = 200)

            // A tela não o mostra (RN21)...
            assertTrue(dao.observarItens().first().isEmpty())
            // ...mas a lápide fica para a exclusão ir à nuvem (RN22)
            val pendentes = dao.pendentes()
            assertEquals(1, pendentes.size)
            assertTrue(pendentes[0].removido)
            assertEquals(200L, pendentes[0].atualizadoEm)
        }
    }

    @Test
    fun marcarSincronizadoSoLimpaSeOItemNaoMudouDuranteOEnvio() {
        runBlocking {
            val dao = banco.listaDao()
            dao.salvar(item(id = 1, atualizadoEm = 100))

            // O usuário mudou o item (atualizadoEm 150) depois de o envio ler a versão 100:
            // a confirmação do envio da versão 100 NÃO pode apagar a pendência da alteração nova
            dao.atualizarStatus(id = 1, status = "ASSISTINDO", agora = 150)
            dao.marcarSincronizado(id = 1, atualizadoEm = 100)
            assertTrue(dao.obter(1)!!.pendenteSincronizacao)

            // Confirmando a versão certa, a pendência sai
            dao.marcarSincronizado(id = 1, atualizadoEm = 150)
            assertFalse(dao.obter(1)!!.pendenteSincronizacao)
        }
    }

    @Test
    fun apagarLapideSoSeOItemNaoMudouDesdeOEnvio() {
        runBlocking {
            val dao = banco.listaDao()
            dao.salvar(item(id = 1, atualizadoEm = 100, removido = true))

            // Timestamp diferente: o item foi alterado depois, a lápide fica
            dao.apagarRemovidoSincronizado(id = 1, atualizadoEm = 99)
            assertNotNull(dao.obter(1))

            // Timestamp igual: exclusão confirmada na nuvem, a linha some de vez
            dao.apagarRemovidoSincronizado(id = 1, atualizadoEm = 100)
            assertNull(dao.obter(1))
        }
    }

    @Test
    fun alterarStatusMarcaOItemComoPendente() {
        runBlocking {
            val dao = banco.listaDao()
            dao.salvar(item(id = 1, atualizadoEm = 100, pendente = false))

            dao.atualizarStatus(id = 1, status = "COMPLETO", agora = 300)

            val salvo = dao.obter(1)!!
            assertEquals("COMPLETO", salvo.status)
            assertEquals(300L, salvo.atualizadoEm)
            assertTrue(salvo.pendenteSincronizacao)
        }
    }

    @Test
    fun quantidadeDePendentesAcompanhaAsAlteracoes() {
        runBlocking {
            val dao = banco.listaDao()
            assertEquals(0, dao.observarQuantidadePendentes().first())

            dao.salvar(item(id = 1, atualizadoEm = 100, pendente = true))
            dao.salvar(item(id = 2, atualizadoEm = 100, pendente = false))

            assertEquals(1, dao.observarQuantidadePendentes().first())
        }
    }

    // ---------- Estatísticas da aba Conta (RF67) ----------

    @Test
    fun contagemPorStatusConsideraTipoStatusEItensRemovidos() {
        runBlocking {
            val dao = banco.listaDao()
            dao.salvar(item(id = 1, atualizadoEm = 1, tipo = "movie", status = "ASSISTIDO"))       // conta (filme concluído)
            dao.salvar(item(id = 2, atualizadoEm = 1, tipo = "movie", status = "ASSISTIDO"))       // conta
            dao.salvar(item(id = 3, atualizadoEm = 1, tipo = "movie", status = "ASSISTIDO", removido = true)) // removido: não conta
            dao.salvar(item(id = 4, atualizadoEm = 1, tipo = "movie", status = "QUERO_ASSISTIR"))  // outro status: não conta
            dao.salvar(item(id = 5, atualizadoEm = 1, tipo = "tv", status = "COMPLETO"))           // conta (série maratonada)
            dao.salvar(item(id = 6, atualizadoEm = 1, tipo = "tv", status = "ASSISTIDO"))          // status de filme numa série: não conta como filme
            dao.salvar(item(id = 7, atualizadoEm = 1, tipo = "tv", status = "COMPLETO", removido = true))     // removida: não conta

            assertEquals(2, dao.observarQuantidadePorStatus("movie", "ASSISTIDO").first())
            assertEquals(1, dao.observarQuantidadePorStatus("tv", "COMPLETO").first())
            // Status sem nenhum item dá zero, e não erro
            assertEquals(0, dao.observarQuantidadePorStatus("movie", "ABANDONEI").first())
        }
    }

    @Test
    fun totalDeEpisodiosAssistidosIgnoraOsDesmarcados() {
        runBlocking {
            val dao = banco.episodioDao()
            assertEquals(0, dao.observarTotalAssistidos().first())

            // Duas séries diferentes entram na mesma soma; o desmarcado (lápide) fica de fora
            dao.salvarVarios(
                listOf(
                    episodio(7, 1, 1),
                    episodio(7, 1, 2),
                    episodio(8, 1, 1),
                    episodio(8, 1, 2, removido = true)
                )
            )

            assertEquals(3, dao.observarTotalAssistidos().first())
        }
    }

    // ---------- Episódios ----------

    @Test
    fun removerSerieDeixaLapidesSoNosEpisodiosAtivos() {
        runBlocking {
            val dao = banco.episodioDao()
            dao.salvarVarios(listOf(episodio(7, 1, 1), episodio(7, 1, 2), episodio(8, 1, 1)))

            // RN20: remover a série 7 desmarca o progresso inteiro dela, e só dela
            dao.marcarTodosRemovidosDaSerie(serieId = 7, agora = 500)

            assertTrue(dao.listarDaSerie(7).isEmpty())       // a tela não mostra progresso
            assertEquals(1, dao.listarDaSerie(8).size)        // outra série não é afetada
            // As duas lápides ficam pendentes para a exclusão ir à nuvem
            assertEquals(2, dao.pendentes().count { (it.serieId == 7) && it.removido })
        }
    }

    @Test
    fun desmarcarEpisodioEscondeDaTelaMasMantemPendente() {
        runBlocking {
            val dao = banco.episodioDao()
            dao.salvar(episodio(7, 1, 1))

            dao.marcarRemovido(serieId = 7, temporada = 1, numero = 1, agora = 600)

            assertTrue(dao.observarDaSerie(7).first().isEmpty())
            assertTrue(dao.obter(7, 1, 1)!!.removido)
        }
    }

    // ---------- Perfil (RN25) ----------

    @Test
    fun detectaPerfilDeOutroUsuarioNoBancoLocal() {
        runBlocking {
            val dao = banco.perfilDao()
            dao.salvar(PerfilEntity("usuarioA", "Ana", "google", null, 1L, pendenteSincronizacao = false))

            assertEquals(0, dao.contarDeOutrosUsuarios("usuarioA"))
            assertEquals(1, dao.contarDeOutrosUsuarios("usuarioB"))
        }
    }

    // ---------- Cache das obras ----------

    @Test
    fun limpezaDoCacheMantemSoAsObrasQueEstaoNaLista() {
        runBlocking {
            val lista = banco.listaDao()
            val cache = banco.obraCacheDao()

            lista.salvar(item(id = 10, atualizadoEm = 1, tipo = "tv"))                 // na lista
            lista.salvar(item(id = 12, atualizadoEm = 1, tipo = "tv", removido = true)) // removida

            cache.salvar(ObraCacheEntity("tv", 10, -1, "{}", 1))      // série 10: na lista
            cache.salvar(ObraCacheEntity("tv", 11, -1, "{}", 1))      // série 11: nunca esteve na lista
            cache.salvar(ObraCacheEntity("movie", 10, -1, "{}", 1))   // FILME 10: mesmo id, outro tipo
            cache.salvar(ObraCacheEntity("tv", 12, -1, "{}", 1))      // série 12: removida da lista

            cache.apagarForaDaLista()

            assertNotNull(cache.obter("tv", 10, -1))
            assertNull(cache.obter("tv", 11, -1))
            assertNull(cache.obter("movie", 10, -1))
            assertNull(cache.obter("tv", 12, -1))
        }
    }
}
