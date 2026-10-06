package com.seriando.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.seriando.app.data.cache.AbaCache
import com.seriando.app.data.cache.BuscaCache
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.model.PerfilPrivado
import com.seriando.app.model.PerfilPublico
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val perfilRepository = PerfilRepository()

    fun usuarioAtual(): FirebaseUser? = firebaseAuth.currentUser

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
        if (usuario != null) {
            val perfilPublicoExistente = perfilRepository.buscarPerfilPublico(usuario.uid)
            if (perfilPublicoExistente == null) {
                val nomeCompleto = usuario.displayName ?: "Usuário"
                val primeiroNome = nomeCompleto.trim().split(" ").firstOrNull() ?: "Usuário"
                perfilRepository.salvarPerfilPublico(
                    usuario.uid,
                    PerfilPublico(apelido = primeiroNome, avatarId = "google")
                )
                perfilRepository.salvarPerfilPrivado(
                    usuario.uid,
                    PerfilPrivado(email = usuario.email ?: "", aniversario = null)
                )
            }
        }

        return usuario
    }

    // RF27: ao sair, além de encerrar a sessão do Firebase, limpa o cache
    // local (pesquisa e aba selecionada), para não vazar dados de um usuário
    // para a sessão do próximo que logar no mesmo aparelho.
    fun sair() {
        firebaseAuth.signOut()
        BuscaCache.textoPesquisa = ""
        BuscaCache.resultados = emptyList()
        AbaCache.abaSelecionada = 0
    }
}