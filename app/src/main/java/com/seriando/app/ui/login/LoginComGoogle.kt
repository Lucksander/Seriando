package com.seriando.app.ui.login

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.seriando.app.R
import com.seriando.app.data.auth.AuthRepository
import kotlinx.coroutines.launch

/**
 * Resultado de rememberLoginComGoogle: o estado do botão e a ação de entrar.
 *
 * @property entrando true enquanto o login está em andamento (a tela desabilita o botão).
 * @property entrar   inicia o fluxo de login com Google; ignora toques enquanto já está entrando.
 */
class LoginComGoogle(
    val entrando: Boolean,
    val entrar: () -> Unit
)

/**
 * Concentra o fluxo de login com Google para ser reutilizado por mais de uma tela
 * (tela de entrada do RF53 e folha inferior do RF54).
 *
 * @param aoLogarComSucesso chamada depois que o login der certo, para a tela seguir adiante.
 */
@Composable
fun rememberLoginComGoogle(aoLogarComSucesso: () -> Unit): LoginComGoogle {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // remember: o repositório é criado uma vez, e não a cada recomposição
    val authRepository = remember { AuthRepository() }
    val webClientId = stringResource(R.string.default_web_client_id)

    // true enquanto o login está em andamento: evita dois toques seguidos
    var entrando by remember { mutableStateOf(false) }
    // Garante que o login chame sempre a versão mais recente do callback da tela
    val aoLogarAtual by rememberUpdatedState(aoLogarComSucesso)

    return LoginComGoogle(
        entrando = entrando,
        entrar = {
            if (!entrando) {
                entrando = true
                scope.launch {
                    try {
                        val usuario = authRepository.entrarComGoogle(context, webClientId)
                        if (usuario != null) {
                            aoLogarAtual()
                        } else {
                            // Login terminou sem usuário: raro, mas não pode passar em silêncio
                            Toast.makeText(context, "Não foi possível entrar.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: GetCredentialCancellationException) {
                        // O usuário fechou o seletor de contas de propósito: não é erro, sem Toast
                        Log.d("LoginComGoogle", "Login cancelado pelo usuário")
                    } catch (e: Exception) {
                        Log.e("LoginComGoogle", "Falha no login", e)
                        Toast.makeText(context, "Não foi possível entrar.", Toast.LENGTH_SHORT).show()
                    } finally {
                        entrando = false
                    }
                }
            }
        }
    )
}