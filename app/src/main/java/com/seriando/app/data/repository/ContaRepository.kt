package com.seriando.app.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.sync.Sincronizador
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Exclusão da conta e de todos os dados do usuário (RF29, LGPD).
 * As funções NÃO tratam erros: quem chama (a tela) faz try/catch, registra no Log
 * e avisa o usuário com Toast.
 *
 * Ordem segura de excluirConta:
 *  1. confirmarIdentidade (Google ou senha, RF32): se o usuário cancelar ou errar a senha, NADA foi apagado.
 *  2. Bloqueia a sincronização: uma alteração pendente não pode recriar na nuvem o que estamos apagando.
 *  3. excluirDados: apaga o Firestore. É idempotente: se falhar no meio, dá para tentar de novo.
 *  4. Exclui a conta do Authentication e limpa o banco local (RN25), só depois dos dados.
 *
 * Limitações conhecidas (registrar no modelo de ameaças e na nota LGPD):
 *  - Votos que OUTRAS pessoas deram nas opiniões do usuário ficam órfãos: as regras só deixam
 *    cada um apagar o próprio voto. Não aparecem em lugar nenhum do app.
 *  - Denúncias feitas pelo usuário são mantidas como registro de moderação.
 *  - A busca percorre todas as opiniões do app: serve para esta escala, mas fica lenta se o
 *    app crescer (a solução completa seria uma Cloud Function).
 */
class ContaRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // O Firestore aceita até 500 operações por lote; usamos 400 de folga
    private val tamanhoLote = 400

    /**
     * true se, para excluir a conta, o usuário precisa digitar a senha (RF32): conta de e-mail e
     * senha SEM vínculo com o Google. Quem tem o Google vinculado (mesmo com senha) confirma pelo
     * seletor de contas Google, como antes.
     */
    fun exigeSenhaParaExcluir(): Boolean {
        val provedores = firebaseAuth.currentUser?.providerData?.map { it.providerId }.orEmpty()
        return EmailAuthProvider.PROVIDER_ID in provedores && GoogleAuthProvider.PROVIDER_ID !in provedores
    }

    /**
     * Confirma a identidade do usuário de novo. O Firebase só exclui a conta de quem entrou há
     * pouco, e esta confirmação também evita exclusão por toque acidental.
     *  - [senha] != null (conta de e-mail e senha): reautentica com a senha digitada; senha errada
     *    lança FirebaseAuthInvalidCredentialsException.
     *  - [senha] == null (conta Google): pede o login do Google; se o usuário escolher outra
     *    conta, o reauthenticate lança exceção, e cancelar o seletor lança
     *    GetCredentialCancellationException.
     */
    suspend fun confirmarIdentidade(context: Context, webClientId: String, senha: String? = null) {
        val usuario = firebaseAuth.currentUser
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        if (senha != null) {
            val email = usuario.email ?: throw IllegalStateException("A conta não tem e-mail")
            usuario.reauthenticate(EmailAuthProvider.getCredential(email, senha)).await()
            return
        }

        val opcaoGoogle = GetSignInWithGoogleOption.Builder(webClientId).build()
        val pedido = GetCredentialRequest.Builder().addCredentialOption(opcaoGoogle).build()
        val resultado = CredentialManager.create(context).getCredential(context, pedido)

        val credencialGoogle = GoogleIdTokenCredential.createFrom(resultado.credential.data)
        val credencialFirebase = GoogleAuthProvider.getCredential(credencialGoogle.idToken, null)
        usuario.reauthenticate(credencialFirebase).await()
    }

    /**
     * Apaga todos os dados do usuário no Firestore, em etapas, dos filhos para os pais.
     * Se uma etapa falhar, o que sobrou continua alcançável (os pais ainda existem)
     * e uma nova tentativa termina o serviço.
     */
    suspend fun excluirDados(uid: String) {
        val usuarioDoc = db.collection("usuarios").document(uid)

        // Uma única consulta em todas as coleções "opinioes" do app (regra RF29 do Firestore)
        val todasOpinioes = db.collectionGroup("opinioes").get().await().documents

        // Séries da lista pessoal
        val series = usuarioDoc.collection("series").get().await().documents

        // ---- Etapa 1: documentos "filhos" ----
        val etapa1 = mutableListOf<DocumentReference>()

        // Votos que o usuário deu nas opiniões dos outros: votos/{uid} dentro de cada opinião.
        // Só apaga onde o voto existe, consultando em paralelo.
        val votos = coroutineScope {
            todasOpinioes
                .filter { it.id != uid }
                .map { opiniao ->
                    async {
                        val voto = opiniao.reference.collection("votos").document(uid)
                        if (voto.get().await().exists()) voto else null
                    }
                }
                .awaitAll()
                .filterNotNull()
        }
        etapa1.addAll(votos)

        // Progresso de episódios de cada série da lista
        val episodios = coroutineScope {
            series.map { serie ->
                async { serie.reference.collection("episodiosAssistidos").get().await().documents.map { it.reference } }
            }.awaitAll().flatten()
        }
        etapa1.addAll(episodios)

        // Opiniões privadas
        etapa1.addAll(usuarioDoc.collection("opinioesPrivadas").get().await().documents.map { it.reference })

        // ---- Etapa 2: documentos "pais" ----
        val etapa2 = mutableListOf<DocumentReference>()
        // Avaliações e opiniões públicas do próprio usuário (o id do documento é o uid do autor)
        etapa2.addAll(todasOpinioes.filter { it.id == uid }.map { it.reference })
        // Documentos das séries da lista (os filhos já foram apagados na etapa 1)
        etapa2.addAll(series.map { it.reference })

        // ---- Etapa 3: perfil público e documento principal ----
        val etapa3 = listOf(db.collection("perfisPublicos").document(uid), usuarioDoc)

        Log.d("ContaRepository", "Excluindo: ${etapa1.size} filhos, ${etapa2.size} pais")
        apagarEmLotes(etapa1)
        apagarEmLotes(etapa2)
        apagarEmLotes(etapa3)
    }

    /** Apaga os documentos em lotes de até [tamanhoLote]; cada lote é "tudo ou nada". */
    private suspend fun apagarEmLotes(documentos: List<DocumentReference>) {
        documentos.chunked(tamanhoLote).forEach { grupo ->
            val lote = db.batch()
            grupo.forEach { lote.delete(it) }
            lote.commit().await()
        }
    }

    /**
     * Fluxo completo: confirma a identidade, apaga os dados, exclui a conta do Authentication
     * e limpa o banco local (RN25). A tela chama isto e, no sucesso, limpa os caches
     * (AuthRepository.sair) e volta para a Home deslogada.
     */
    suspend fun excluirConta(context: Context, webClientId: String, senha: String? = null) {
        val usuario = firebaseAuth.currentUser
            ?: throw IllegalStateException("Nenhum usuário autenticado")
        val uid = usuario.uid

        confirmarIdentidade(context, webClientId, senha) // se falhar ou cancelar, nada foi apagado

        // A partir daqui nenhuma sincronização pode enviar nem gravar nada: uma pendência
        // enviada agora recriaria na nuvem dados que estamos apagando
        Sincronizador.bloquear()
        try {
            excluirDados(uid)
            usuario.delete().await()
            Dependencias.limparBancoLocal()
        } catch (e: Exception) {
            // A exclusão falhou: libera a sincronização para o app continuar funcionando
            Sincronizador.desbloquear()
            throw e
        }
    }
}