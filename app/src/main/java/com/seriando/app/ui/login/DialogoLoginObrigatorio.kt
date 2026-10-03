package com.seriando.app.ui.login

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.seriando.app.R
import com.seriando.app.data.auth.AuthRepository
import kotlinx.coroutines.launch

// Diálogo exibido quando o usuário tenta fazer algo que exige login
// (como salvar uma série) mas ainda não está autenticado.
// aoFechar é chamado ao cancelar.
// aoLogarComSucesso é chamado após o login dar certo, para a tela que
// abriu o diálogo continuar a ação que o usuário queria fazer.
@Composable
fun DialogoLoginObrigatorio(
    aoFechar: () -> Unit,
    aoLogarComSucesso: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = AuthRepository()
    val webClientId = stringResource(R.string.default_web_client_id)

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Faça login para salvar suas séries e filmes") },
        text = { Text("Entre com sua conta Google para continuar.") },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    try {
                        val usuario = authRepository.entrarComGoogle(context, webClientId)
                        if (usuario != null) {
                            aoLogarComSucesso()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Não foi possível entrar.", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Entrar com Google")
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) {
                Text("Cancelar")
            }
        }
    )
}