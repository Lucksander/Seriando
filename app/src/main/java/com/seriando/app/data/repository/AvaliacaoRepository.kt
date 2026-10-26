package com.seriando.app.data.repository

import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.seriando.app.model.Avaliacao
import com.seriando.app.model.VotoResumo
import kotlinx.coroutines.tasks.await

/**
 * Repositório de avaliações e opiniões (RF16 a RF26, RF48).
 *
 * Onde cada coisa fica no Firestore:
 *  - avaliacoes/{obra}/opinioes/{uid}: a NOTA do usuário e, se a opinião for
 *    PÚBLICA, o texto, spoiler e emoções. Legível por qualquer usuário autenticado.
 *  - usuarios/{uid}/opinioesPrivadas/{obra}: a opinião PRIVADA (RF48, RN12).
 *    Legível só pelo dono. Separar em outro documento é o que permite a regra
 *    de segurança esconder o texto privado, já que o Firestore não protege
 *    campos individuais de um documento (RNF03).
 *
 * As funções não tratam erros: quem chama (a tela) faz try/catch.
 */
class AvaliacaoRepository {

    private val db = FirebaseFirestore.getInstance()

    // Paginação das opiniões
    private val docsPorConsulta = 20L      // documentos lidos do Firestore em cada consulta
    private val minimoPorPagina = 10       // opiniões que uma página tenta reunir antes de parar
    private val maxConsultasPorPagina = 3  // limite de consultas seguidas para montar uma página

    // Id da obra usado nos caminhos, ex.: "tv_1399" ou "movie_550"
    private fun idObra(tipoMidia: String, tmdbId: Int) = "${tipoMidia}_$tmdbId"

    private fun colecaoOpinioes(tipoMidia: String, tmdbId: Int) =
        db.collection("avaliacoes").document(idObra(tipoMidia, tmdbId)).collection("opinioes")

    // Documento público do usuário nesta obra (nota + opinião pública)
    private fun docPublico(uid: String, tipoMidia: String, tmdbId: Int) =
        colecaoOpinioes(tipoMidia, tmdbId).document(uid)

    // Documento da opinião privada do usuário nesta obra (só o dono lê)
    private fun docPrivado(uid: String, tipoMidia: String, tmdbId: Int) =
        db.collection("usuarios").document(uid)
            .collection("opinioesPrivadas").document(idObra(tipoMidia, tmdbId))

    // Votos de uma opinião específica: avaliacoes/{obra}/opinioes/{autorUid}/votos/{votanteUid}
    private fun colecaoVotos(autorOpiniaoUid: String, tipoMidia: String, tmdbId: Int) =
        colecaoOpinioes(tipoMidia, tmdbId).document(autorOpiniaoUid).collection("votos")

    /** Grava a nota (1 a 5) sem tocar no texto da opinião (merge). */
    suspend fun salvarNota(uid: String, tipoMidia: String, tmdbId: Int, nota: Int) {
        val dados = mapOf("autorUid" to uid, "nota" to nota, "data" to System.currentTimeMillis())
        docPublico(uid, tipoMidia, tmdbId).set(dados, SetOptions.merge()).await()
    }

    /**
     * Salva a opinião, no lugar certo conforme a visibilidade (RF19, RF48).
     *  - publica = true : grava o texto no documento público e apaga a cópia privada.
     *  - publica = false: grava o texto no documento privado e limpa o texto do
     *    documento público, preservando a nota (RN12).
     * Tudo num WriteBatch: ou as duas operações acontecem, ou nenhuma, para o
     * texto nunca ficar nos dois lugares ao mesmo tempo.
     */
    suspend fun salvarOpiniao(
        uid: String,
        tipoMidia: String,
        tmdbId: Int,
        texto: String,
        spoiler: Boolean,
        emocoes: List<String>,
        publica: Boolean,
        apelidoAutor: String,
        avatarAutor: String
    ) {
        val publico = docPublico(uid, tipoMidia, tmdbId)
        val privado = docPrivado(uid, tipoMidia, tmdbId)
        val agora = System.currentTimeMillis()
        val lote = db.batch()

        if (publica) {
            // Opinião pública: texto vai para o documento público; some do privado
            val dados = mapOf(
                "autorUid" to uid,
                "apelidoAutor" to apelidoAutor,
                "avatarAutor" to avatarAutor,
                "texto" to texto,
                "spoiler" to spoiler,
                "emocoes" to emocoes,
                "publica" to true,
                "data" to agora
            )
            lote.set(publico, dados, SetOptions.merge())
            lote.delete(privado)
        } else {
            // Opinião privada: texto vai para o documento privado
            val dados = mapOf(
                "autorUid" to uid,
                "apelidoAutor" to apelidoAutor,
                "avatarAutor" to avatarAutor,
                "texto" to texto,
                "spoiler" to spoiler,
                "emocoes" to emocoes,
                "publica" to false,
                "data" to agora
            )
            lote.set(privado, dados) // sem merge: substitui qualquer versão anterior

            // Limpa o texto público só se o documento já existe (se só havia
            // opinião, sem nota, não faz sentido criar um documento vazio)
            if (publico.get().await().exists()) {
                lote.set(publico, camposDeOpiniaoLimpos(), SetOptions.merge())
            }
        }
        lote.commit().await()
    }

    /**
     * Remove a opinião (RF22), nos dois lugares, preservando a nota (RN12).
     * A nota tem sua própria remoção (removerMinhaAvaliacao).
     */
    suspend fun removerOpiniao(uid: String, tipoMidia: String, tmdbId: Int) {
        val publico = docPublico(uid, tipoMidia, tmdbId)
        val lote = db.batch()
        lote.delete(docPrivado(uid, tipoMidia, tmdbId))
        if (publico.get().await().exists()) {
            lote.set(publico, camposDeOpiniaoLimpos(), SetOptions.merge())
        }
        lote.commit().await()
    }

    // Valores que "esvaziam" a opinião no documento público, mantendo a nota
    private fun camposDeOpiniaoLimpos() = mapOf(
        "texto" to "",
        "spoiler" to false,
        "emocoes" to emptyList<String>(),
        "publica" to true
    )

    /**
     * Devolve a avaliação do próprio usuário: a nota (documento público)
     * combinada com a opinião privada, se existir. Devolve null se não há nada.
     */
    suspend fun buscarMinhaAvaliacao(uid: String, tipoMidia: String, tmdbId: Int): Avaliacao? {
        val docPub = docPublico(uid, tipoMidia, tmdbId).get().await()
        val publica = if (docPub.exists()) docPub.toObject(Avaliacao::class.java) else null

        val docPriv = docPrivado(uid, tipoMidia, tmdbId).get().await()
        val privada = if (docPriv.exists()) docPriv.toObject(Avaliacao::class.java) else null

        return when {
            // Só nota/opinião pública: devolve como estava
            privada == null -> publica
            // Há opinião privada: ela fornece o texto; a nota vem do público (ou 0)
            else -> privada.copy(nota = publica?.nota ?: 0)
        }
    }

    /**
     * Remove a nota (RF17). Mantém o comportamento anterior: apaga o documento
     * inteiro, o que leva junto a opinião pública; por coerência, apaga também
     * a opinião privada.
     */
    suspend fun removerMinhaAvaliacao(uid: String, tipoMidia: String, tmdbId: Int) {
        val lote = db.batch()
        lote.delete(docPublico(uid, tipoMidia, tmdbId))
        lote.delete(docPrivado(uid, tipoMidia, tmdbId))
        lote.commit().await()
    }

    /** Todos os documentos públicos da obra (notas e opiniões públicas). */
    suspend fun buscarTodasAvaliacoes(tipoMidia: String, tmdbId: Int): List<Avaliacao> {
        val resultado = colecaoOpinioes(tipoMidia, tmdbId).get().await()
        return resultado.toObjects(Avaliacao::class.java)
    }

    /**
     * Média e total das notas da comunidade (RF18), calculados pelo SERVIDOR do Firestore
     * (consulta de agregação): nenhum documento é baixado para o aparelho. Cobra-se cerca de
     * 1 leitura a cada 1000 documentos considerados, em vez de 1 leitura por avaliação.
     * Só entram documentos com nota maior que zero (quem só escreveu texto fica de fora).
     * Exige internet: a agregação sempre consulta o servidor.
     */
    suspend fun buscarMediaDaComunidade(tipoMidia: String, tmdbId: Int): MediaDaComunidade {
        val campoTotal = AggregateField.count()
        val campoMedia = AggregateField.average("nota")

        val resultado = colecaoOpinioes(tipoMidia, tmdbId)
            .whereGreaterThan("nota", 0)
            .aggregate(campoTotal, campoMedia)
            .get(AggregateSource.SERVER)
            .await()

        return MediaDaComunidade(
            media = resultado.getDouble(campoMedia) ?: 0.0, // nulo quando não há nenhuma nota
            total = resultado.getLong(campoTotal)?.toInt() ?: 0
        )
    }

    /** Média das notas da comunidade (RF18). Ignora documentos sem nota. */
    suspend fun calcularMedia(tipoMidia: String, tmdbId: Int): Double {
        val avaliacoes = buscarTodasAvaliacoes(tipoMidia, tmdbId).filter { it.nota > 0 }
        if (avaliacoes.isEmpty()) return 0.0
        return avaliacoes.map { it.nota }.average()
    }

    /**
     * Primeira página de opiniões da obra (RF21, RF48): a opinião do PRÓPRIO usuário no topo
     * (se existir, pública ou privada) seguida das opiniões públicas mais recentes dos outros.
     * A minha é buscada à parte para aparecer sempre, mesmo que seja antiga demais para cair
     * na primeira página das públicas.
     */
    suspend fun buscarPrimeiraPaginaDeOpinioes(tipoMidia: String, tmdbId: Int, uidAtual: String?): PaginaDeOpinioes {
        val minha = if (uidAtual != null) {
            buscarMinhaAvaliacao(uidAtual, tipoMidia, tmdbId)?.takeIf { it.texto.isNotBlank() }
        } else null

        val pagina = buscarMaisOpinioes(tipoMidia, tmdbId, uidAtual, aPartirDaData = null)
        return pagina.copy(opinioes = listOfNotNull(minha) + pagina.opinioes)
    }

    /**
     * Uma página de opiniões PÚBLICAS de outros usuários, da mais recente para a mais antiga.
     *
     * Lê [docsPorConsulta] documentos por vez, ordenados por data, e descarta os que não
     * servem: só nota sem texto, opiniões ocultadas por quem denunciou (RF68) e a do próprio
     * usuário (que vem à parte). Se sobrarem menos que [minimoPorPagina], consulta de novo,
     * até [maxConsultasPorPagina] vezes. O ponto de continuação é a data do último documento
     * LIDO (inclusive os descartados), para a próxima página não repetir nem pular nada.
     *
     * @param aPartirDaData data do último documento da página anterior; null = começar do início.
     */
    suspend fun buscarMaisOpinioes(
        tipoMidia: String,
        tmdbId: Int,
        uidAtual: String?,
        aPartirDaData: Long?
    ): PaginaDeOpinioes {
        // Autores que este usuário ocultou ao denunciar (RF68)
        val ocultos = if (uidAtual != null) buscarAutoresOcultos(uidAtual, tipoMidia, tmdbId) else emptySet()

        val encontradas = mutableListOf<Avaliacao>()
        var cursor = aPartirDaData
        var temMais = true
        var consultas = 0

        while (temMais && encontradas.size < minimoPorPagina && consultas < maxConsultasPorPagina) {
            var consulta = colecaoOpinioes(tipoMidia, tmdbId)
                .orderBy("data", Query.Direction.DESCENDING)
                .limit(docsPorConsulta)
            if (cursor != null) consulta = consulta.startAfter(cursor)

            val documentos = consulta.get().await().documents
            consultas++

            // Vieram menos documentos do que o pedido: não há mais nada depois
            if (documentos.size < docsPorConsulta) temMais = false

            // A próxima consulta recomeça depois do último documento lido
            documentos.lastOrNull()?.getLong("data")?.let { cursor = it }

            documentos
                .mapNotNull { it.toObject(Avaliacao::class.java) }
                .filter {
                    it.texto.isNotBlank() && it.publica &&
                        it.autorUid != uidAtual && it.autorUid !in ocultos
                }
                .let { encontradas.addAll(it) }
        }

        return PaginaDeOpinioes(encontradas, cursor, temMais)
    }

    /**
     * RF68: uids dos autores cujas opiniões o usuário ocultou ao denunciar, nesta obra.
     * Lê usuarios/{uid}/opinioesOcultas filtrando pela obra (consulta de um único campo,
     * sem índice composto). A ocultação vale só para quem denunciou; os outros usuários
     * continuam vendo a opinião normalmente.
     */
    private suspend fun buscarAutoresOcultos(uid: String, tipoMidia: String, tmdbId: Int): Set<String> {
        val docs = db.collection("usuarios").document(uid)
            .collection("opinioesOcultas")
            .whereEqualTo("obraKey", idObra(tipoMidia, tmdbId))
            .get().await()
        return docs.documents.mapNotNull { it.getString("autorUid") }.toSet()
    }

    // RF23, RF24, RF25: registra o voto do usuário numa opinião. Tocar de
    // novo no mesmo valor remove o voto (toggle), em vez de criar um novo.
    // RN04 (não votar na própria opinião) é checado por quem chama esta função
    // e também será imposto pelas regras do Firestore.
    suspend fun votar(autorOpiniaoUid: String, tipoMidia: String, tmdbId: Int, votanteUid: String, valor: Int) {
        val documento = colecaoVotos(autorOpiniaoUid, tipoMidia, tmdbId).document(votanteUid)
        val atual = documento.get().await()
        val valorAtual = if (atual.exists()) atual.getLong("valor")?.toInt() else null

        if (valorAtual == valor) {
            documento.delete().await() // mesmo voto de novo: desfaz
        } else {
            documento.set(mapOf("valor" to valor)).await() // voto novo, ou trocando de curtir para descurtir (ou vice-versa)
        }
    }

    // RF26: conta curtidas/descurtidas de uma opinião, e identifica o voto
    // do usuário atual, para destacar o botão já escolhido.
    suspend fun buscarResumoVotos(
        autorOpiniaoUid: String,
        tipoMidia: String,
        tmdbId: Int,
        votanteUidAtual: String?
    ): VotoResumo {
        val todos = colecaoVotos(autorOpiniaoUid, tipoMidia, tmdbId).get().await()
        var curtidas = 0
        var descurtidas = 0
        var meuVoto: Int? = null
        todos.documents.forEach { doc ->
            val valor = doc.getLong("valor")?.toInt() ?: return@forEach
            if (valor == 1) curtidas++ else if (valor == -1) descurtidas++
            if (doc.id == votanteUidAtual) meuVoto = valor
        }
        return VotoResumo(curtidas, descurtidas, meuVoto)
    }
}

/** Resultado da agregação das notas de uma obra: média (0.0 se não há notas) e quantidade de notas. */
data class MediaDaComunidade(val media: Double, val total: Int)

/**
 * Uma página de opiniões: os itens, a data a partir da qual a próxima página começa
 * (null se ainda não leu nada) e se ainda há mais opiniões a carregar.
 */
data class PaginaDeOpinioes(
    val opinioes: List<Avaliacao>,
    val proximaData: Long?,
    val temMais: Boolean
)
