package com.seriando.app.ui.login

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.seriando.app.R
import com.seriando.app.data.auth.AuthRepository
import kotlinx.coroutines.launch

/**
 * Diálogo exibido quando o usuário tenta fazer algo que exige login
 * (salvar uma série, avaliar, votar, denunciar) mas ainda não está autenticado.
 *
 * @param aoFechar            chamado ao cancelar.
 * @param aoLogarComSucesso   chamado após o login dar certo, para a tela que
 *                            abriu o diálogo continuar a ação que o usuário queria fazer.
 */
@Composable
fun DialogoLoginObrigatorio(
    aoFechar: () -> Unit,
    aoLogarComSucesso: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // remember: o repositório é criado uma vez, e não a cada recomposição do diálogo
    val authRepository = remember { AuthRepository() }
    val webClientId = stringResource(R.string.default_web_client_id)

    // true enquanto o login está em andamento: evita dois toques seguidos no botão
    var entrando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Faça login para continuar") },
        text = { Text("Entre com sua conta Google para salvar suas séries e filmes, avaliar e participar da comunidade.") },
        confirmButton = {
            TextButton(
                enabled = !entrando,
                onClick = {
                    entrando = true
                    scope.launch {
                        try {
                            val usuario = authRepository.entrarComGoogle(context, webClientId)
                            if (usuario != null) {
                                aoLogarComSucesso()
                            } else {
                                // Login terminou sem usuário: raro, mas não pode passar em silêncio
                                Toast.makeText(context, "Não foi possível entrar.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: GetCredentialCancellationException) {
                            // O usuário fechou o seletor de contas de propósito: não é erro, sem Toast
                            Log.d("DialogoLogin", "Login cancelado pelo usuário")
                        } catch (e: Exception) {
                            Log.e("DialogoLogin", "Falha no login", e)
                            Toast.makeText(context, "Não foi possível entrar.", Toast.LENGTH_SHORT).show()
                        } finally {
                            entrando = false
                        }
                    }
                }
            ) {
                Text(if (entrando) "Entrando..." else "Entrar com Google")
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) {
                Text("Cancelar")
            }
        }
    )
}