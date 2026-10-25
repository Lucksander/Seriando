package com.seriando.app.ui.comum

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seriando.app.data.auth.AuthRepository
import kotlinx.coroutines.launch

/**
 * Botão de sair da conta, em destaque (botão cheio na cor da marca, largura total e ícone), que
 * SEMPRE pergunta antes de sair e protege o trabalho feito sem internet (RN25, RN27).
 *
 * Ao tocar, o app tenta enviar as alterações pendentes e confere se sobrou alguma. Depois
 * mostra uma pergunta de confirmação:
 *  - nada pendente: "Tem certeza de que quer sair?";
 *  - sobrou algo: o mesmo aviso, dizendo também quantas alterações serão perdidas (o banco
 *    local é apagado no logout).
 * Só sai se o usuário confirmar; "Cancelar" ou tocar fora fecha a pergunta e mantém a sessão.
 *
 * @param texto            texto do botão ("Sair" ou "Sair da conta").
 * @param aoSairConfirmado chamado depois que o logout aconteceu, para a tela atualizar seu estado.
 */
@Composable
fun BotaoSair(texto: String, aoSairConfirmado: () -> Unit, modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    var verificando by remember { mutableStateOf(false) } // enviando e conferindo as pendências

    // null = pergunta fechada; 0 = pergunta simples; n > 0 = pergunta avisando que n alterações se perdem
    var perdidas by remember { mutableStateOf<Int?>(null) }

    /** Faz o logout de fato (limpa a sessão, os caches e o banco local) e avisa a tela. */
    fun sairAgora() {
        perdidas = null
        authRepository.sair()
        aoSairConfirmado()
    }

    Button(
        enabled = !verificando,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        onClick = {
            verificando = true
            scope.launch {
                try {
                    // Tenta enviar o que está pendente e conta o que sobrou; a pergunta abre de qualquer jeito
                    perdidas = authRepository.alteracoesQueSeriamPerdidas()
                } catch (e: Exception) {
                    // Não deu para conferir as pendências: registra e mostra a pergunta simples
                    Log.e("BotaoSair", "Falha ao conferir as alterações pendentes", e)
                    perdidas = 0
                } finally {
                    verificando = false
                }
            }
        }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null, // decorativo: o texto do botão já diz a ação
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (verificando) "Verificando..." else texto)
    }

    val quantidade = perdidas
    if (quantidade != null) {
        AlertDialog(
            // Tocar fora ou voltar = cancelar: continua logado
            onDismissRequest = { perdidas = null },
            title = { Text("Sair da conta?") },
            text = {
                if (quantidade > 0) {
                    val descricao = if (quantidade == 1) "1 alteração que ainda não foi sincronizada"
                    else "$quantidade alterações que ainda não foram sincronizadas"
                    Text(
                        "Você tem $descricao. Elas só podem ser enviadas com internet. " +
                                "Se você sair agora, serão perdidas."
                    )
                } else {
                    Text(
                        "Tem certeza de que quer sair? Seus dados continuam salvos na sua conta, " +
                                "mas você precisará entrar de novo. A foto escolhida neste aparelho, " +
                                "se houver, será apagada."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { sairAgora() }) {
                    Text(if (quantidade > 0) "Sair mesmo assim" else "Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = { perdidas = null }) { Text("Cancelar") }
            }
        )
    }
}
