package com.seriando.app.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.local.EpisodioAssistidoEntity
import com.seriando.app.data.local.paraEntity
import com.seriando.app.data.local.paraSerieSalva
import com.seriando.app.model.SerieSalva
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import com.google.firebase.firestore.SetOptions
import com.seriando.app.data.local.PerfilEntity
import com.seriando.app.data.repository.ObraCacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Sincroniza o banco local (Room) com o Firestore (RF33, RN21 a RN23).
 *
 * Sincroniza a lista pessoal e os episódios assistidos; o perfil entra no passo seguinte.
 * Cada parte tem duas fases:
 *  1. ENVIO: para cada alteração pendente, confere a nuvem. Se a nuvem tem uma alteração mais
 *     recente, ela vence e a pendência local é descartada (RN23); senão, envia.
 *  2. RECEBIMENTO (só quando pedido): baixa os dados da nuvem para o banco local, sem tocar
 *     no que ainda estiver pendente, e apaga localmente o que foi removido em outro aparelho.
 *
 * O recebimento lê muitos documentos, então só roda ao abrir o app e ao fazer login
 * (baixarDaNuvem = true). As alterações feitas na tela só disparam o ENVIO.
 *
 * Regras de segurança do funcionamento:
 *  - Roda em segundo plano, nunca na tela: sem conexão, uma gravação no Firestore não
 *    termina até a internet voltar, e a tela não pode ficar esperando por isso.
 *  - Só uma sincronização roda por vez. Pedidos que chegam durante uma rodada pedem
 *    uma nova rodada no fim, em vez de rodar em paralelo.
 *  - Toda chamada à nuvem tem tempo limite; se estourar, a pendência continua e é
 *    tentada de novo na próxima rodada.
 *  - As leituras usam Source.SERVER: sem conexão elas falham em vez de devolver o cache do
 *    próprio Firestore, que poderia estar incompleto e apagar itens por engano.
 *  - Nunca se espera a rede com uma transação do banco aberta: primeiro lê da nuvem, depois
 *    grava no banco local numa transação curta.
 *  - Se o usuário sair da conta no meio, nada é gravado no banco local.
 */
object Sincronizador {

    private const val TAG = "Sincronizador"
    private const val TEMPO_LIMITE_MS = 15_000L

    // O Firestore aceita até 30 valores numa consulta com whereIn
    private const val LIMITE_CONSULTA_IDS = 30

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val trava = Mutex()

    // true quando chegou um pedido enquanto outra rodada estava em andamento
    @Volatile
    private var repetir = false

    // true se algum dos pedidos que chegaram durante a rodada pediu o recebimento da nuvem
    @Volatile
    private var baixarNaProxima = false
    private val _sincronizando = MutableStateFlow(false)

    /** true enquanto uma sincronização está em andamento (usado pela faixa de conexão). */
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    /**
     * Pede uma sincronização em segundo plano. Pode ser chamada à vontade.
     *
     * @param baixarDaNuvem true para também baixar os dados da nuvem (abertura do app e login);
     *                      false para só enviar o que está pendente (alterações feitas na tela).
     */
    fun disparar(uid: String, baixarDaNuvem: Boolean = true) {
        // Sem internet não há o que enviar: as pendências esperam, e o monitor de conexão
        // dispara a sincronização completa quando a internet voltar
        if (!MonitorConexao.online.value) {
            Log.d(TAG, "Sem conexão: sincronização adiada")
            return
        }
        escopo.launch { executar(uid, baixarDaNuvem) }
    }

    private suspend fun executar(uid: String, baixarDaNuvem: Boolean) {
        // Já há uma rodada em andamento: só pede que ela se repita no fim
        if (!trava.tryLock()) {
            repetir = true
            if (baixarDaNuvem) baixarNaProxima = true
            return
        }
        _sincronizando.value = true
        try {
            var baixar = baixarDaNuvem
            do {
                repetir = false
                try {
                    sincronizarLista(uid, baixar)
                    sincronizarEpisodios(uid, baixar)
                    sincronizarPerfil(uid, baixar)
                    if (baixar) cuidarDoCacheDasObras()
                } catch (e: Exception) {
                    // Sem internet, tempo esgotado ou regra recusada: as pendências ficam para a próxima
                    Log.w(TAG, "Sincronização não concluída", e)
                }
                baixar = baixarNaProxima
                baixarNaProxima = false
            } while (repetir)
        } finally {
            _sincronizando.value = false
            trava.unlock()
        }
        // Um pedido pode ter chegado entre o último teste e o desbloqueio
        if (repetir) {
            val baixar = baixarNaProxima
            baixarNaProxima = false
            executar(uid, baixar)
        }
    }

    /** true se o usuário que pediu a sincronização ainda é o usuário logado. */
    private fun aindaLogado(uid: String): Boolean =
        FirebaseAuth.getInstance().currentUser?.uid == uid

    /** Executa uma chamada à nuvem com tempo limite (ver o aviso sobre gravações offline acima). */
    private suspend fun <T> comTempo(bloco: suspend () -> T): T =
        withTimeout(TEMPO_LIMITE_MS) { bloco() }

    /** Id do documento de um episódio na nuvem, igual ao que o app sempre usou. */
    private fun idDoEpisodio(temporada: Int, numero: Int) = "${temporada}_$numero"

    // ==================== LISTA PESSOAL ====================

    private suspend fun sincronizarLista(uid: String, baixar: Boolean) {
        val dao = Dependencias.listaDao
        val colecao = db.collection("usuarios").document(uid).collection("series")

        // ---------- Fase 1: ENVIO das alterações pendentes ----------
        val pendentes = dao.pendentes()
        Log.d(TAG, "Lista: ${pendentes.size} alteração(ões) pendente(s) para enviar")

        for (item in pendentes) {
            if (!aindaLogado(uid)) return

            val doc = colecao.document(item.id.toString())
            val nuvem = comTempo { doc.get(Source.SERVER).await() }
            val atualizadoNaNuvem = nuvem.getLong("atualizadoEm") ?: 0L

            if (nuvem.exists() && atualizadoNaNuvem > item.atualizadoEm) {
                // A nuvem tem uma alteração mais recente que a local: ela vence (RN23).
                // Só substitui se o item local não mudou desde que foi lido aqui.
                val daNuvem = nuvem.toObject(SerieSalva::class.java)
                if (daNuvem != null) {
                    Dependencias.emTransacao {
                        val atual = dao.obter(item.id)
                        if (atual != null && atual.atualizadoEm == item.atualizadoEm) {
                            dao.salvar(daNuvem.paraEntity(pendente = false))
                        }
                    }
                }
                continue
            }

            if (item.removido) {
                // Remoção feita no aparelho: apaga na nuvem (episódios e documento) e depois a lápide local
                comTempo { apagarNaNuvem(doc) }
                dao.apagarRemovidoSincronizado(item.id, item.atualizadoEm)
            } else {
                // Criação ou alteração: grava o documento inteiro e limpa a marca de pendente
                comTempo { doc.set(item.paraSerieSalva()).await() }
                dao.marcarSincronizado(item.id, item.atualizadoEm)
            }
        }

        if (!baixar) return

        // ---------- Fase 2: RECEBIMENTO da lista da nuvem ----------
        val daNuvem = comTempo { colecao.get(Source.SERVER).await() }
            .documents
            .mapNotNull { it.toObject(SerieSalva::class.java) }
        Log.d(TAG, "Lista: ${daNuvem.size} item(ns) na nuvem")

        if (!aindaLogado(uid)) return

        // Itens novos ou alterados na nuvem entram no banco local, exceto os que ainda têm
        // alteração pendente (a decisão sobre eles já foi tomada na fase 1)
        for (item in daNuvem) {
            Dependencias.emTransacao {
                val local = dao.obter(item.id)
                if (local == null || !local.pendenteSincronizacao) {
                    dao.salvar(item.paraEntity(pendente = false))
                }
            }
        }

        // Itens que existem localmente (sem pendência) mas sumiram da nuvem foram removidos
        // em outro aparelho: saem do banco local junto com o progresso de episódios deles
        val idsNaNuvem = daNuvem.map { it.id }.toSet()
        for (local in dao.observarItens().first()) {
            if (local.id !in idsNaNuvem) {
                Dependencias.emTransacao {
                    val atual = dao.obter(local.id)
                    if (atual != null && !atual.pendenteSincronizacao) {
                        dao.apagar(local.id)
                        Dependencias.episodioDao.apagarTodosDaSerie(local.id)
                    }
                }
            }
        }
    }

    /**
     * Apaga um item da lista na nuvem junto com o progresso de episódios dele (RN20).
     * O Firestore não apaga subcoleções sozinho: primeiro os episódios, depois o documento.
     */
    private suspend fun apagarNaNuvem(doc: DocumentReference) {
        val episodios = doc.collection("episodiosAssistidos").get().await().documents
        episodios.chunked(400).forEach { grupo ->
            val lote = db.batch()
            grupo.forEach { lote.delete(it.reference) }
            lote.commit().await()
        }
        doc.delete().await()
    }

    // ==================== EPISÓDIOS ASSISTIDOS ====================

    private suspend fun sincronizarEpisodios(uid: String, baixar: Boolean) {
        val episodioDao = Dependencias.episodioDao
        val series = db.collection("usuarios").document(uid).collection("series")

        // ---------- Fase 1: ENVIO das alterações pendentes ----------
        val pendentes = episodioDao.pendentes()
        Log.d(TAG, "Episódios: ${pendentes.size} alteração(ões) pendente(s) para enviar")

        for ((serieId, daSerie) in pendentes.groupBy { it.serieId }) {
            val colecao = series.document(serieId.toString()).collection("episodiosAssistidos")

            // Em grupos de até 30, para ler da nuvem só os documentos que estão pendentes
            for (grupo in daSerie.chunked(LIMITE_CONSULTA_IDS)) {
                if (!aindaLogado(uid)) return

                val idsDoGrupo = grupo.map { idDoEpisodio(it.temporada, it.numero) }
                val naNuvem = comTempo {
                    colecao.whereIn(FieldPath.documentId(), idsDoGrupo).get(Source.SERVER).await()
                }.documents.associateBy { it.id }

                val lote = db.batch()
                val noLote = mutableListOf<EpisodioAssistidoEntity>()

                for (ep in grupo) {
                    val id = idDoEpisodio(ep.temporada, ep.numero)
                    val docNuvem = naNuvem[id]
                    val atualizadoNaNuvem = docNuvem?.getLong("atualizadoEm") ?: 0L

                    if (docNuvem != null && atualizadoNaNuvem > ep.atualizadoEm) {
                        // A nuvem tem uma alteração mais recente: ela vence (RN23).
                        // Só substitui se o episódio local não mudou desde que foi lido aqui.
                        Dependencias.emTransacao {
                            val atual = episodioDao.obter(ep.serieId, ep.temporada, ep.numero)
                            if (atual != null && atual.atualizadoEm == ep.atualizadoEm) {
                                episodioDao.salvar(
                                    EpisodioAssistidoEntity(
                                        serieId = ep.serieId,
                                        temporada = ep.temporada,
                                        numero = ep.numero,
                                        atualizadoEm = atualizadoNaNuvem,
                                        pendenteSincronizacao = false,
                                        removido = false
                                    )
                                )
                            }
                        }
                        continue
                    }

                    val ref = colecao.document(id)
                    if (ep.removido) {
                        lote.delete(ref)
                    } else {
                        lote.set(
                            ref,
                            mapOf(
                                "temporada" to ep.temporada,
                                "numero" to ep.numero,
                                "atualizadoEm" to ep.atualizadoEm
                            )
                        )
                    }
                    noLote.add(ep)
                }

                if (noLote.isNotEmpty()) {
                    comTempo { lote.commit().await() }
                    // Confirmado na nuvem: limpa a marca de pendente (ou apaga a lápide)
                    for (ep in noLote) {
                        if (ep.removido) {
                            episodioDao.apagarRemovidoSincronizado(ep.serieId, ep.temporada, ep.numero, ep.atualizadoEm)
                        } else {
                            episodioDao.marcarSincronizado(ep.serieId, ep.temporada, ep.numero, ep.atualizadoEm)
                        }
                    }
                }
            }
        }

        if (!baixar) return

        // ---------- Fase 2: RECEBIMENTO dos episódios, série por série ----------
        // Só das séries que estão ativas na lista local (a lista já foi sincronizada acima)
        for (serie in Dependencias.listaDao.observarItens().first()) {
            if (!aindaLogado(uid)) return

            val colecao = series.document(serie.id.toString()).collection("episodiosAssistidos")
            val documentos = comTempo { colecao.get(Source.SERVER).await() }.documents

            // Interpreta os documentos (id "temporada_numero") antes de abrir a transação
            val daNuvem = documentos.mapNotNull { d ->
                val partes = d.id.split("_")
                val temporada = partes.getOrNull(0)?.toIntOrNull()
                val numero = partes.getOrNull(1)?.toIntOrNull()
                if (temporada == null || numero == null) null
                else Triple(temporada, numero, d.getLong("atualizadoEm") ?: 0L)
            }
            val idsNaNuvem = daNuvem.map { idDoEpisodio(it.first, it.second) }.toSet()

            // Uma transação curta por série: episódios novos entram, exceto os pendentes;
            // os que sumiram da nuvem (e não estão pendentes) saem do banco local
            Dependencias.emTransacao {
                for ((temporada, numero, atualizadoEm) in daNuvem) {
                    val local = episodioDao.obter(serie.id, temporada, numero)
                    if (local == null || !local.pendenteSincronizacao) {
                        episodioDao.salvar(
                            EpisodioAssistidoEntity(
                                serieId = serie.id,
                                temporada = temporada,
                                numero = numero,
                                atualizadoEm = atualizadoEm,
                                pendenteSincronizacao = false,
                                removido = false
                            )
                        )
                    }
                }
                for (local in episodioDao.listarDaSerie(serie.id)) {
                    val id = idDoEpisodio(local.temporada, local.numero)
                    if (id !in idsNaNuvem && !local.pendenteSincronizacao) {
                        episodioDao.apagar(serie.id, local.temporada, local.numero)
                    }
                }
            }
        }
    }


    // ==================== PERFIL ====================

    private suspend fun sincronizarPerfil(uid: String, baixar: Boolean) {
        enviarPerfil(uid)
        if (baixar) baixarPerfil(uid)
    }

    /**
     * ENVIO do perfil: grava perfisPublicos/{uid} (apelido, avatar e atualizadoEm) e
     * usuarios/{uid} (e-mail e aniversário) numa única operação. Se a nuvem tem uma
     * alteração mais recente que a local, ela vence (RN23).
     */
    private suspend fun enviarPerfil(uid: String) {
        val dao = Dependencias.perfilDao
        val publicoRef = db.collection("perfisPublicos").document(uid)
        val privadoRef = db.collection("usuarios").document(uid)

        val pendentes = dao.pendentes().filter { it.uid == uid }
        Log.d(TAG, "Perfil: ${pendentes.size} alteração(ões) pendente(s) para enviar")

        for (perfil in pendentes) {
            if (!aindaLogado(uid)) return

            // As regras do Firestore recusam apelido vazio; não adianta tentar enviar
            if (perfil.apelido.isBlank()) {
                Log.w(TAG, "Perfil com apelido vazio não é enviado")
                continue
            }

            val publicoNuvem = comTempo { publicoRef.get(Source.SERVER).await() }
            val atualizadoNaNuvem = publicoNuvem.getLong("atualizadoEm") ?: 0L

            if (publicoNuvem.exists() && atualizadoNaNuvem > perfil.atualizadoEm) {
                // A nuvem tem um perfil mais recente: ele vence (RN23).
                // Só substitui se o perfil local não mudou desde que foi lido aqui.
                val privadoNuvem = comTempo { privadoRef.get(Source.SERVER).await() }
                Dependencias.emTransacao {
                    val atual = dao.obter(uid)
                    if (atual != null && atual.atualizadoEm == perfil.atualizadoEm) {
                        dao.salvar(
                            PerfilEntity(
                                uid = uid,
                                apelido = publicoNuvem.getString("apelido").orEmpty().ifBlank { perfil.apelido },
                                avatarId = publicoNuvem.getString("avatarId") ?: "google",
                                aniversario = privadoNuvem.getString("aniversario"),
                                atualizadoEm = atualizadoNaNuvem,
                                pendenteSincronizacao = false
                            )
                        )
                    }
                }
                continue
            }

            // O e-mail vem do login do Google (não fica no banco local)
            val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
            val lote = db.batch()
            lote.set(
                publicoRef,
                mapOf(
                    "apelido" to perfil.apelido,
                    "avatarId" to perfil.avatarId,
                    "atualizadoEm" to perfil.atualizadoEm
                ),
                SetOptions.merge() // merge: não apaga campos que não estão no mapa
            )
            lote.set(
                privadoRef,
                mapOf("email" to email, "aniversario" to perfil.aniversario),
                SetOptions.merge()
            )
            comTempo { lote.commit().await() }
            dao.marcarSincronizado(uid, perfil.atualizadoEm)
        }
    }

    /**
     * RECEBIMENTO do perfil. Se a nuvem tem o perfil, ele entra no banco local (exceto se houver
     * alteração local pendente). Se a nuvem NÃO tem e o banco local também não, é o primeiro
     * acesso do usuário: cria o perfil padrão, com o primeiro nome da conta Google.
     * É aqui, e não no login, que o perfil padrão nasce: só a nuvem sabe se o perfil já existia.
     */
    private suspend fun baixarPerfil(uid: String) {
        val dao = Dependencias.perfilDao
        val publicoNuvem = comTempo { db.collection("perfisPublicos").document(uid).get(Source.SERVER).await() }
        val privadoNuvem = comTempo { db.collection("usuarios").document(uid).get(Source.SERVER).await() }

        if (!aindaLogado(uid)) return

        if (publicoNuvem.exists()) {
            Dependencias.emTransacao {
                val local = dao.obter(uid)
                if (local == null || !local.pendenteSincronizacao) {
                    dao.salvar(
                        PerfilEntity(
                            uid = uid,
                            apelido = publicoNuvem.getString("apelido").orEmpty().ifBlank { "Usuário" },
                            avatarId = publicoNuvem.getString("avatarId") ?: "google",
                            aniversario = privadoNuvem.getString("aniversario"),
                            atualizadoEm = publicoNuvem.getLong("atualizadoEm") ?: 0L,
                            pendenteSincronizacao = false
                        )
                    )
                }
            }
        } else {
            // Primeiro acesso: nem a nuvem nem o banco local têm perfil
            val criou = Dependencias.emTransacao {
                if (dao.obter(uid) == null) {
                    dao.salvar(perfilPadrao(uid, privadoNuvem.getString("aniversario")))
                    true
                } else {
                    false
                }
            }
            // Envia o perfil recém-criado já nesta rodada
            if (criou) enviarPerfil(uid)
        }
    }

    /** Perfil inicial de um usuário novo: apelido = primeiro nome da conta Google (ou "Usuário"). */
    private fun perfilPadrao(uid: String, aniversario: String?): PerfilEntity {
        val nomeCompleto = FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
        // ifBlank: nome vazio viraria apelido vazio, que as regras do Firestore recusam
        val primeiroNome = nomeCompleto.trim().split(" ").firstOrNull().orEmpty().ifBlank { "Usuário" }
        return PerfilEntity(
            uid = uid,
            apelido = primeiroNome,
            avatarId = "google",
            aniversario = aniversario,
            atualizadoEm = System.currentTimeMillis(),
            pendenteSincronizacao = true
        )
    }


    // ==================== CACHE DAS OBRAS ====================

    /**
     * Cuida das cópias das obras guardadas no aparelho (RF33): apaga as de obras que não estão
     * mais na lista e baixa, em segundo plano, as das obras da lista que ainda não têm cópia.
     * Só roda depois de a lista ter sido baixada da nuvem, para não apagar nada por engano.
     */
    private suspend fun cuidarDoCacheDasObras() {
        Dependencias.obraCacheDao.apagarForaDaLista()
        ObraCacheRepository().baixarObrasDaListaEmSegundoPlano()
    }


    /**
     * Quantidade total de alterações ainda não enviadas à nuvem (lista, episódios e perfil),
     * atualizada sozinha. Alimenta o aviso de alterações não sincronizadas (RF62).
     */
    fun observarPendentes(): Flow<Int> = combine(
        Dependencias.listaDao.observarQuantidadePendentes(),
        Dependencias.episodioDao.observarQuantidadePendentes(),
        Dependencias.perfilDao.observarQuantidadePendentes()
    ) { lista, episodios, perfil -> lista + episodios + perfil }
}
