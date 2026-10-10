package com.seriando.app.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.seriando.app.data.cache.AbaCache
import com.seriando.app.data.cache.BuscaCache
import com.seriando.app.data.cache.CatalogoCache
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.model.PerfilPrivado
import com.seriando.app.model.PerfilPublico
import kotlinx.coroutines.tasks.await

/**
 * Autenticação com conta Google (RF01, RF02, RF27).
 */
class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val perfilRepository = PerfilRepository()

    /** Usuário autenticado no momento, ou null se não houver login. */
    fun usuarioAtual(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * Abre o seletor de contas Google, autentica no Firebase e, na primeira
     * vez do usuário, cria os documentos de perfil.
     * Falhas do login em si sobem para quem chama (que mostra o Toast).
     */
    suspend fun entrarComGoogle(context: Context, webClientId: String): FirebaseUser? {

        val opcaoGoogle = GetSignInWithGoogleOption.Builder(webClientId).build()

        val pedido = GetCredentialRequest.Builder()
            .addCredentialOption(opcaoGoogle)
            .build()

        val credentialManager = CredentialManager.create(context)
        val resultado = credentialManager.getCredential(context, pedido)

        val credencialGoogle = GoogleIdTokenCredential.createFrom(resultado.credential.data)
        val credencialFirebase = GoogleAuthProvider.getCredential(credencialGoogle.idToken, null)
        val resultadoLogin = firebaseAuth.signInWithCredential(credencialFirebase).await()
        val usuario = resultadoLogin.user

        // Primeira vez desse usuário: cria os dois documentos de perfil.
        // O try/catch impede que uma falha aqui (rede ou regra de segurança) faça o
        // login parecer ter falhado: o usuário já está autenticado, e a aba Usuário
        // recria o perfil público se ele não existir.
        if (usuario != null) {
            try {
                val perfilPublicoExistente = perfilRepository.buscarPerfilPublico(usuario.uid)
                if (perfilPublicoExistente == null) {
                    // ifBlank: nome vazio viraria apelido vazio, que a regra do Firestore recusa
                    val primeiroNome = (usuario.displayName ?: "").trim().split(" ").firstOrNull()
                        .orEmpty().ifBlank { "Usuário" }
                    perfilRepository.salvarPerfilPublico(
                        usuario.uid,
                        PerfilPublico(apelido = primeiroNome, avatarId = "google")
                    )
                    perfilRepository.salvarPerfilPrivado(
                        usuario.uid,
                        PerfilPrivado(email = usuario.email ?: "", aniversario = null)
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Falha ao criar o perfil no primeiro login", e)
            }
        }

        return usuario
    }

    // RF27: ao sair, além de encerrar a sessão do Firebase, limpa o cache
    // local (pesquisa, aba selecionada e catálogo da Home), para não vazar
    // dados de um usuário para a sessão do próximo que logar no mesmo aparelho.
    fun sair() {
        firebaseAuth.signOut()
        BuscaCache.textoPesquisa = ""
        BuscaCache.resultados = emptyList()
        AbaCache.abaSelecionada = 0
        CatalogoCache.limpar()
    }
}