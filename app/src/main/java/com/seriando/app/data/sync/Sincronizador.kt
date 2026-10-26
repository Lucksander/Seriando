package com.seriando.app.data.sync

import com.seriando.app.data.auth.AuthRepository
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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
import kotlinx.coroutines.withTimeoutOrNull
import com.seriando.app.data.cache.VersaoDaIdade

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

    // Conferência de episódios por diferença: margem para diferenças entre os relógios dos
    // aparelhos (o atualizadoEm é gravado com o relógio de quem alterou) e validade máxima
    // de uma leitura completa, depois da qual ela é refeita por segurança
    private const val MARGEM_RELOGIOS_MS = 60L * 60 * 1000                 // 1 hora
    private const val VALIDADE_LEITURA_COMPLETA_MS = 7L * 24 * 60 * 60 * 1000 // 7 dias

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val trava = Mutex()

    // true quando chegou um pedido enquanto outra rodada estava em andamento
    @Volatile
    private var repetir = false

    // true se algum dos pedidos que chegaram durante a rodada pediu o recebimento da nuvem
    @Volatile
    private var baixarNaProxima = false

    // true durante a exclusão da conta: nada pode ser enviado nem gravado no banco local
    @Volatile
    private var bloqueado = false
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
        if (bloqueado) return
        // Sem internet não há o que enviar: as pendências esperam, e o monitor de conexão
        // dispara a sincronização completa quando a internet voltar
        if (!MonitorConexao.online.value) {
            Log.d(TAG, "Sem conexão: sincronização adiada")
            return
        }
        escopo.launch { executar(uid, baixarDaNuvem) }
    }

    private suspend fun executar(uid: String, baixarDaNuvem: Boolean) {
        // Conta de e-mail e senha ainda não confirmada não sincroniza nada (RF32)
        if (!AuthRepository.contaConfirmada(FirebaseAuth.getInstance().currentUser)) return
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
                    garantirBancoDoUsuario(uid)
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

    /**
     * true se o usuário que pediu a sincronização ainda é o usuário logado e a sincronização não
     * foi bloqueada (exclusão de conta). Conferida antes de cada envio e de cada gravação local.
     */
    private fun aindaLogado(uid: String): Boolean =
        !bloqueado && FirebaseAuth.getInstance().currentUser?.uid == uid

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
        // Só das séries que estão ativas na lista local (a lista já foi sincronizada acima).
        // Para poupar leituras do Firestore, cada série tenta primeiro a conferência por
        // DIFERENÇA; só se ela não bastar é que todos os episódios da série são lidos.
        for (serie in Dependencias.listaDao.observarItens().first()) {
            if (!aindaLogado(uid)) return

            val colecao = series.document(serie.id.toString()).collection("episodiosAssistidos")
            if (!receberEpisodiosPorDiferenca(uid, serie.id, colecao)) {
                receberEpisodiosCompleto(uid, serie.id, colecao)
            }
        }
    }

    /** Lê o id "temporada_numero" e o atualizadoEm de um episódio da nuvem; null se o id não for válido. */
    private fun interpretarEpisodio(d: DocumentSnapshot): Triple<Int, Int, Long>? {
        val partes = d.id.split("_")
        val temporada = partes.getOrNull(0)?.toIntOrNull()
        val numero = partes.getOrNull(1)?.toIntOrNull()
        return if (temporada == null || numero == null) null
        else Triple(temporada, numero, d.getLong("atualizadoEm") ?: 0L)
    }

    /**
     * Conferência BARATA dos episódios de uma série. Devolve true se ela bastou (o banco local
     * está de acordo com a nuvem) e false se é preciso a leitura completa.
     *
     *  1. Nunca houve leitura completa, ou a última tem mais de 7 dias: devolve false.
     *  2. Lê só os episódios alterados na nuvem desde a última conferência (menos a margem de
     *     segurança dos relógios) e os aplica, exceto os que têm alteração local pendente.
     *  3. Compara o TOTAL de episódios na nuvem (consulta de agregação, cerca de 1 leitura) com
     *     o total local. Se diferirem, algo mudou sem que a leitura por diferença enxergasse
     *     (por exemplo, um episódio desmarcado em outro aparelho): devolve false.
     */
    private suspend fun receberEpisodiosPorDiferenca(uid: String, serieId: Int, colecao: CollectionReference): Boolean {
        val episodioDao = Dependencias.episodioDao
        val agora = System.currentTimeMillis()

        val ultimaCompleta = EstadoDaSincronizacao.ultimaLeituraCompletaEpisodios(uid, serieId)
        if (ultimaCompleta == 0L || agora - ultimaCompleta > VALIDADE_LEITURA_COMPLETA_MS) return false

        // Passo 2: só o que mudou na nuvem desde a última conferência
        val desde = EstadoDaSincronizacao.ultimaConferenciaEpisodios(uid, serieId) - MARGEM_RELOGIOS_MS
        val alterados = comTempo {
            colecao.whereGreaterThan("atualizadoEm", desde).get(Source.SERVER).await()
        }.documents.mapNotNull { interpretarEpisodio(it) }

        if (!aindaLogado(uid)) return true // saiu da conta no meio: nada a gravar, nem a ler de novo

        if (alterados.isNotEmpty()) {
            Dependencias.emTransacao {
                for ((temporada, numero, atualizadoEm) in alterados) {
                    val local = episodioDao.obter(serieId, temporada, numero)
                    if (local == null || !local.pendenteSincronizacao) {
                        episodioDao.salvar(
                            EpisodioAssistidoEntity(
                                serieId = serieId,
                                temporada = temporada,
                                numero = numero,
                                atualizadoEm = atualizadoEm,
                                pendenteSincronizacao = false,
                                removido = false
                            )
                        )
                    }
                }
            }
        }

        // Passo 3: o total na nuvem tem de bater com o total local (sem contar as lápides)
        val totalNaNuvem = comTempo { colecao.count().get(AggregateSource.SERVER).await() }.count
        val totalLocal = episodioDao.listarDaSerie(serieId).size
        if (totalNaNuvem != totalLocal.toLong()) {
            Log.d(TAG, "Episódios da série $serieId: nuvem $totalNaNuvem x local $totalLocal, lendo tudo")
            return false
        }

        EstadoDaSincronizacao.gravarConferenciaEpisodios(uid, serieId, agora)
        Log.d(TAG, "Episódios da série $serieId: conferidos por diferença (${alterados.size} alterado(s))")
        return true
    }

    /**
     * Leitura COMPLETA dos episódios de uma série: baixa todos os documentos da nuvem e deixa o
     * banco local igual a ela (exceto o que está pendente). Cara em leituras: só roda quando a
     * conferência por diferença não basta.
     */
    private suspend fun receberEpisodiosCompleto(uid: String, serieId: Int, colecao: CollectionReference) {
        val episodioDao = Dependencias.episodioDao
        val inicio = System.currentTimeMillis() // marcado ANTES de ler: o que mudar durante a leitura não se perde

        val daNuvem = comTempo { colecao.get(Source.SERVER).await() }
            .documents.mapNotNull { interpretarEpisodio(it) }
        val idsNaNuvem = daNuvem.map { idDoEpisodio(it.first, it.second) }.toSet()

        if (!aindaLogado(uid)) return

        // Uma transação curta por série: episódios novos entram, exceto os pendentes;
        // os que sumiram da nuvem (e não estão pendentes) saem do banco local
        Dependencias.emTransacao {
            for ((temporada, numero, atualizadoEm) in daNuvem) {
                val local = episodioDao.obter(serieId, temporada, numero)
                if (local == null || !local.pendenteSincronizacao) {
                    episodioDao.salvar(
                        EpisodioAssistidoEntity(
                            serieId = serieId,
                            temporada = temporada,
                            numero = numero,
                            atualizadoEm = atualizadoEm,
                            pendenteSincronizacao = false,
                            removido = false
                        )
                    )
                }
            }
            for (local in episodioDao.listarDaSerie(serieId)) {
                val id = idDoEpisodio(local.temporada, local.numero)
                if (id !in idsNaNuvem && !local.pendenteSincronizacao) {
                    episodioDao.apagar(serieId, local.temporada, local.numero)
                }
            }
        }

        EstadoDaSincronizacao.gravarLeituraCompletaEpisodios(uid, serieId, inicio)
        Log.d(TAG, "Episódios da série $serieId: leitura completa (${daNuvem.size} na nuvem)")
    }


    // ==================== PERFIL ====================

    private suspend fun sincronizarPerfil(uid: String, baixar: Boolean) {
        enviarPerfil(uid)
        if (baixar) baixarPerfil(uid)
    }

    /**
     * ENVIO do perfil: grava perfisPublicos/{uid} (apelido, avatar e atualizadoEm) e
     * usuarios/{uid} (e-mail, data de nascimento e marca de correção) numa única operação.
     * Se a nuvem tem uma alteração mais recente que a local, ela vence (RN23).
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
                                pendenteSincronizacao = false,
                                // A marca de correção também vem da nuvem (RN17)
                                aniversarioCorrigido = privadoNuvem.getBoolean("aniversarioAlterado") ?: false
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
                mapOf(
                    "email" to email,
                    "aniversario" to perfil.aniversario,
                    // As regras do Firestore conferem esta marca para permitir só uma correção (RN17)
                    "aniversarioAlterado" to perfil.aniversarioCorrigido
                ),
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

        // Documentos criados antes do Bloco K não têm a marca: valem como "correção não usada"
        val corrigidaNaNuvem = privadoNuvem.getBoolean("aniversarioAlterado") ?: false

        if (publicoNuvem.exists()) {
            val dataNova = privadoNuvem.getString("aniversario")
            // A transação devolve true se a data de nascimento local mudou (ou nem existia ainda)
            val dataMudou = Dependencias.emTransacao {
                val local = dao.obter(uid)
                if (local == null || !local.pendenteSincronizacao) {
                    dao.salvar(
                        PerfilEntity(
                            uid = uid,
                            apelido = publicoNuvem.getString("apelido").orEmpty().ifBlank { "Usuário" },
                            avatarId = publicoNuvem.getString("avatarId") ?: "google",
                            aniversario = dataNova,
                            atualizadoEm = publicoNuvem.getLong("atualizadoEm") ?: 0L,
                            pendenteSincronizacao = false,
                            aniversarioCorrigido = corrigidaNaNuvem
                        )
                    )
                    local?.aniversario != dataNova
                } else {
                    false
                }
            }
            // A data chegou da nuvem e muda a idade: as listas filtradas antes não valem mais (RF50)
            if (dataMudou) VersaoDaIdade.idadeMudou()
        } else {
            // Primeiro acesso: nem a nuvem nem o banco local têm perfil
            val criou = Dependencias.emTransacao {
                if (dao.obter(uid) == null) {
                    dao.salvar(perfilPadrao(uid, privadoNuvem.getString("aniversario"), corrigidaNaNuvem))
                    true
                } else {
                    false
                }
            }
            // Envia o perfil recém-criado já nesta rodada
            if (criou) enviarPerfil(uid)
        }
    }

    /**
     * Perfil inicial de um usuário novo.
     *  - Conta Google: apelido = primeiro nome da conta (ou "Usuário") e avatar = foto do Google.
     *  - Conta de e-mail e senha (RF32): apelido = o que a pessoa escolheu no cadastro (guardado no
     *    nome da conta), inteiro, e avatar = a primeira foto do app, já que não há foto do Google.
     */
    private fun perfilPadrao(uid: String, aniversario: String?, aniversarioCorrigido: Boolean): PerfilEntity {
        val usuario = FirebaseAuth.getInstance().currentUser
        val nomeCompleto = usuario?.displayName.orEmpty()
        val ehGoogle = usuario?.providerData?.any { it.providerId == "google.com" } == true
        // ifBlank: nome vazio viraria apelido vazio, que as regras do Firestore recusam
        val apelido = if (ehGoogle) {
            nomeCompleto.trim().split(" ").firstOrNull().orEmpty().ifBlank { "Usuário" }
        } else {
            nomeCompleto.trim().take(40).ifBlank { "Usuário" } // 40 = limite das regras
        }
        return PerfilEntity(
            uid = uid,
            apelido = apelido,
            avatarId = if (ehGoogle) "google" else "foto_01",
            aniversario = aniversario,
            atualizadoEm = System.currentTimeMillis(),
            pendenteSincronizacao = true,
            aniversarioCorrigido = aniversarioCorrigido
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


    // ==================== SAÍDA DA CONTA E EXCLUSÃO ====================

    /** Impede qualquer sincronização (usado durante a exclusão da conta, RF29). */
    fun bloquear() {
        bloqueado = true
    }

    /** Libera as sincronizações de novo (no login, ou se a exclusão da conta falhar). */
    fun desbloquear() {
        bloqueado = false
    }

    /** Quantas alterações ainda não foram enviadas à nuvem, agora. */
    suspend fun contarPendentes(): Int = observarPendentes().first()

    /**
     * Tenta enviar as pendências antes de o usuário sair da conta (RN27): com internet, pede o
     * envio e espera até [limiteMs] que as pendências cheguem a zero. Devolve quantas restam
     * (zero se tudo foi enviado; sem internet, nada é tentado).
     */
    suspend fun tentarEnviarPendencias(uid: String, limiteMs: Long = 10_000L): Int {
        if (MonitorConexao.online.value) {
            disparar(uid, baixarDaNuvem = false)
            withTimeoutOrNull(limiteMs) { observarPendentes().first { it == 0 } }
        }
        return contarPendentes()
    }

    /**
     * Defesa do RN25: o banco local só pode ter dados do usuário logado. Se aparecer um perfil
     * de outro usuário (por exemplo, o app fechou no meio do logout), apaga tudo antes de sincronizar.
     */
    private suspend fun garantirBancoDoUsuario(uid: String) {
        if (Dependencias.perfilDao.contarDeOutrosUsuarios(uid) > 0) {
            Log.w(TAG, "Banco local com dados de outro usuário: limpando antes de sincronizar")
            Dependencias.limparBancoLocal()
        }
    }
}
