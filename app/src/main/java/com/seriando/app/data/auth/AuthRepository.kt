package com.seriando.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.model.PerfilUsuario
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

        // Se for a primeira vez desse usuário (nenhum perfil salvo ainda),
        // cria o perfil com o primeiro nome extraído do nome completo do Google.
        if (usuario != null) {
            val perfilExistente = perfilRepository.buscarPerfil(usuario.uid)
            if (perfilExistente == null) {
                val nomeCompleto = usuario.displayName ?: "Usuário"
                val primeiroNome = nomeCompleto.trim().split(" ").firstOrNull() ?: "Usuário"
                perfilRepository.salvarPerfil(
                    usuario.uid,
                    PerfilUsuario(apelido = primeiroNome, email = usuario.email ?: "")
                )
            }
        }

        return usuario
    }

    fun sair() {
        firebaseAuth.signOut()
    }
}