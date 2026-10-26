package com.seriando.app.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.seriando.app.data.cache.AbaCache
import com.seriando.app.data.cache.BuscaCache
import com.seriando.app.data.cache.CatalogoCache
import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.sync.Sincronizador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.seriando.app.data.cache.VersaoDaIdade

/**
 * Autenticação com conta Google (RF01) e com e-mail e senha (RF32), identificação do usuário
 * (RF02) e logout (RF27).
 *
 * Contas de e-mail e senha só entram no app depois de confirmar o e-mail (link enviado pelo
 * Firebase). Contas Google já vêm com o e-mail confirmado.
 */
class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Usuário autenticado e liberado para usar o app, ou null.
     *
     * Quem criou conta com e-mail e senha e ainda não confirmou o e-mail NÃO conta como logado:
     * aqui ele aparece como null (visitante). Assim, todas as telas e repositórios do app, que
     * perguntam por este método, tratam a conta não confirmada como visitante, sem mudar nada.
     * A tela de confirmação usa [usuarioSemConfirmar] para enxergar esse usuário.
     */
    fun usuarioAtual(): FirebaseUser? = firebaseAuth.currentUser?.takeIf { contaConfirmada(it) }

    /** O usuário do Firebase mesmo sem e-mail confirmado (só para a tela de confirmação). */
    private fun usuarioSemConfirmar(): FirebaseUser? = firebaseAuth.currentUser

    /** E-mail da conta que está aguardando confirmação (mostrado na tela de confirmação). */
    fun emailDaContaAtual(): String? = usuarioSemConfirmar()?.email

    companion object {
        /**
         * true se a conta pode usar o app: contas Google (que já vêm com e-mail confirmado) e contas
         * de e-mail e senha com o e-mail confirmado. Usado também pelo Sincronizador, para nunca
         * sincronizar uma conta ainda não confirmada.
         */
        fun contaConfirmada(usuario: FirebaseUser?): Boolean {
            if (usuario == null) return false
            val usaSenha = usuario.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
            return !usaSenha || usuario.isEmailVerified
        }
    }

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

        if (usuario != null) concluirLogin(usuario)

        return usuario
    }

    /**
     * Passos que valem para qualquer login concluído (Google ou e-mail com e-mail confirmado):
     * avisa que a idade pode ter mudado, libera a sincronização e baixa os dados da nuvem.
     */
    private fun concluirLogin(usuario: FirebaseUser) {
        // Quem entra pode ter outra idade: apaga as listas guardadas com a idade anterior (RF50)
        VersaoDaIdade.idadeMudou()
        // Libera a sincronização (ela fica bloqueada depois de uma exclusão de conta)
        // e baixa os dados da nuvem para o banco local, em segundo plano
        Sincronizador.desbloquear()
        Sincronizador.disparar(usuario.uid)
    }

    // ==================== E-mail e senha (RF32) ====================

    /**
     * Cria uma conta com e-mail e senha e envia o e-mail de confirmação.
     *
     * O usuário fica autenticado no Firebase, mas AINDA NÃO pode usar o app: as telas conferem
     * [precisaConfirmarEmail] e mostram a tela "Confirme seu e-mail". A sincronização só é
     * liberada em [verificarConfirmacaoDoEmail].
     *
     * O apelido vai para o nome da conta (displayName), de onde o Sincronizador o lê para criar
     * o perfil. Se gravar o apelido ou enviar a confirmação falhar, a conta já existe: o erro é
     * só registrado, e o usuário pode pedir o reenvio na tela de confirmação.
     *
     * Falhas da criação (e-mail em uso, senha fraca, sem internet...) sobem para quem chama.
     */
    suspend fun criarContaComEmail(email: String, senha: String, apelido: String): FirebaseUser {
        val resultado = firebaseAuth.createUserWithEmailAndPassword(email.trim(), senha).await()
        val usuario = resultado.user ?: error("O Firebase não devolveu o usuário criado")

        try {
            val pedido = UserProfileChangeRequest.Builder().setDisplayName(apelido.trim()).build()
            usuario.updateProfile(pedido).await()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Falha ao gravar o apelido no cadastro", e)
        }
        try {
            usuario.sendEmailVerification().await()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Falha ao enviar o e-mail de confirmação", e)
        }
        return usuario
    }

    /**
     * Entra com e-mail e senha. Se o e-mail já foi confirmado, conclui o login e baixa os dados;
     * se não, devolve o usuário sem liberar nada, e a tela mostra a confirmação de e-mail.
     * Falhas (credencial inválida, sem internet...) sobem para quem chama.
     */
    suspend fun entrarComEmail(email: String, senha: String): FirebaseUser {
        val resultado = firebaseAuth.signInWithEmailAndPassword(email.trim(), senha).await()
        val usuario = resultado.user ?: error("O Firebase não devolveu o usuário")
        if (usuario.isEmailVerified) concluirLogin(usuario)
        return usuario
    }

    /**
     * true se o usuário atual entrou com e-mail e senha e ainda não confirmou o e-mail.
     * Contas Google nunca precisam de confirmação. Enquanto for true, o app só mostra a tela de
     * confirmação (sem lista, perfil, comunidade nem sincronização).
     */
    fun precisaConfirmarEmail(): Boolean {
        val usuario = usuarioSemConfirmar() ?: return false
        return !contaConfirmada(usuario)
    }

    /** Reenvia o e-mail de confirmação ao usuário atual. Falhas sobem para quem chama. */
    suspend fun reenviarEmailDeConfirmacao() {
        usuarioSemConfirmar()?.sendEmailVerification()?.await()
    }

    /**
     * Confere se o usuário já tocou no link do e-mail ("Já confirmei"). Recarrega a conta no
     * Firebase; se estiver confirmada, renova o token (para as regras do Firestore enxergarem
     * email_verified = true) e conclui o login.
     *
     * @return true se o e-mail está confirmado e o app já pode ser usado.
     */
    suspend fun verificarConfirmacaoDoEmail(): Boolean {
        val usuario = usuarioSemConfirmar() ?: return false
        usuario.reload().await()
        if (!usuario.isEmailVerified) return false
        usuario.getIdToken(true).await()
        concluirLogin(usuario)
        return true
    }

    /**
     * Envia o e-mail de "esqueci minha senha". Com a proteção contra enumeração ativada, o Firebase
     * responde com sucesso mesmo se o e-mail não tiver conta; por isso a tela deve dizer só
     * "se existir uma conta, enviamos o link". Quem tem conta Google com esse e-mail pode usar o
     * link para criar uma senha, passando a entrar pelos dois métodos na mesma conta.
     */
    suspend fun enviarRedefinicaoDeSenha(email: String) {
        firebaseAuth.sendPasswordResetEmail(email.trim()).await()
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
        VersaoDaIdade.idadeMudou() // quem sai vira visitante: as telas abertas recarregam com a idade restrita (RF50)

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