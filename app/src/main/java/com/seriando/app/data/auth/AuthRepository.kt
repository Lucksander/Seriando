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
import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.sync.Sincronizador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Autenticação com conta Google (RF01, RF02, RF27).
 */
class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    /** Usuário autenticado no momento, ou null se não houver login. */
    fun usuarioAtual(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * Abre o seletor de contas Google, autentica no Firebase e pede a primeira sincronização,
     * que traz a lista e o perfil da nuvem para o banco local (e cria o perfil padrão se o
     * usuário for novo).
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

        if (usuario != null) {
            // Libera a sincronização (ela fica bloqueada depois de uma exclusão de conta)
            // e baixa os dados da nuvem para o banco local, em segundo plano
            Sincronizador.desbloquear()
            Sincronizador.disparar(usuario.uid)
        }

        return usuario
    }

    /**
     * Quantas alterações seriam perdidas se o usuário saísse agora (RN27). Se há pendências,
     * tenta enviá-las primeiro (com internet, espera até 10 segundos). Devolve o que ainda
     * restar: zero significa que pode sair sem perder nada.
     */
    suspend fun alteracoesQueSeriamPerdidas(): Int {
        val uid = usuarioAtual()?.uid ?: return 0
        if (Sincronizador.contarPendentes() == 0) return 0
        return Sincronizador.tentarEnviarPendencias(uid)
    }

    // RF27: ao sair, além de encerrar a sessão do Firebase, limpa o cache
    // local (pesquisa, aba selecionada e catálogo da Home) e o banco local do modo
    // offline (RN25), para não vazar dados de um usuário para o próximo que logar
    // no mesmo aparelho.
    // Atenção: alterações feitas sem conexão e ainda não enviadas são descartadas junto;
    // por isso as telas chamam alteracoesQueSeriamPerdidas() antes e pedem confirmação (RN27).
    fun sair() {
        firebaseAuth.signOut()
        BuscaCache.textoPesquisa = ""
        BuscaCache.resultados = emptyList()
        AbaCache.abaSelecionada = 0
        CatalogoCache.limpar()

        // O Room só apaga fora da thread principal
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Dependencias.limparBancoLocal()
            } catch (e: Exception) {
                Log.e("AuthRepository", "Falha ao limpar o banco local", e)
            }
        }
    }
}