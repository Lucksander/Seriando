package com.seriando.app.ui.comum

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.seriando.app.data.auth.AuthRepository
import kotlinx.coroutines.launch

/**
 * Botão de sair da conta que protege o trabalho feito sem internet (RN25, RN27).
 *
 * Ao tocar, o app tenta enviar as alterações pendentes e confere se sobrou alguma:
 *  - nada pendente: sai na hora;
 *  - sobrou algo: avisa quantas alterações serão perdidas (o banco local é apagado no logout)
 *    e só sai se o usuário confirmar.
 *
 * @param texto            texto do botão ("Sair" ou "Sair da conta").
 * @param aoSairConfirmado chamado depois que o logout aconteceu, para a tela atualizar seu estado.
 */
@Composable
fun BotaoSair(texto: String, aoSairConfirmado: () -> Unit) {

    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    var verificando by remember { mutableStateOf(false) }   // enviando e conferindo as pendências
    var perdidas by remember { mutableStateOf<Int?>(null) } // quantas seriam perdidas (null = sem diálogo)

    /** Faz o logout de fato (limpa a sessão, os caches e o banco local) e avisa a tela. */
    fun sairAgora() {
        perdidas = null
        authRepository.sair()
        aoSairConfirmado()
    }

    TextButton(
        enabled = !verificando,
        onClick = {
            verificando = true
            scope.launch {
                try {
                    val quantidade = authRepository.alteracoesQueSeriamPerdidas()
                    if (quantidade > 0) perdidas = quantidade else sairAgora()
                } catch (e: Exception) {
                    // Não deu para conferir as pendências: registra e sai, para o usuário não ficar preso
                    Log.e("BotaoSair", "Falha ao conferir as alterações pendentes", e)
                    sairAgora()
                } finally {
                    verificando = false
                }
            }
        }
    ) {
        Text(if (verificando) "Verificando..." else texto)
    }

    val quantidade = perdidas
    if (quantidade != null) {
        val descricao = if (quantidade == 1) "1 alteração que ainda não foi sincronizada"
        else "$quantidade alterações que ainda não foram sincronizadas"

        AlertDialog(
            onDismissRequest = { perdidas = null },
            title = { Text("Sair da conta?") },
            text = {
                Text(
                    "Você tem $descricao. Elas só podem ser enviadas com internet. " +
                            "Se você sair agora, serão perdidas."
                )
            },
            confirmButton = {
                TextButton(onClick = { sairAgora() }) { Text("Sair mesmo assim") }
            },
            dismissButton = {
                TextButton(onClick = { perdidas = null }) { Text("Cancelar") }
            }
        )
    }
}