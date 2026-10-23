package com.seriando.app.ui.usuario

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.GetCredentialCancellationException
import coil3.compose.AsyncImage
import com.seriando.app.R
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.repository.ContaRepository
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.data.repository.ResultadoDataNascimento
import com.seriando.app.model.DataNascimento
import com.seriando.app.model.PerfilPublico
import com.seriando.app.ui.comum.BotaoSair
import com.seriando.app.ui.home.DialogoEditarApelido
import com.seriando.app.ui.login.AvisoDeLoginNecessario
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Aba Conta (RF12 a RF15, RF29, RF33, RF59): apelido, foto/avatar, e-mail, data de nascimento
 * e exclusão da conta.
 * O perfil vem do banco local (Room), que a tela observa: funciona sem internet e se atualiza
 * sozinho quando a sincronização traz mudanças da nuvem (RN21).
 * Toda gravação tem try/catch com Log e Toast, porque a falha não pode derrubar o app.
 */
@Composable
fun UsuarioScreen(
    // Chamado depois do logout ou da exclusão da conta; quem navega é o SeriandoNavHost (RF53)
    aoSair: () -> Unit = {}
) {

    val authRepository = remember { AuthRepository() }
    val contaRepository = remember { ContaRepository() }
    val context = LocalContext.current
    val perfilRepository = remember { PerfilRepository() }
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.default_web_client_id)
    // Sobe quando o visitante faz login pela folha; como é lido nos remember abaixo,
    // força a tela a recompor e trocar o aviso pelo perfil
    var loginFeito by remember { mutableIntStateOf(0) }
    val usuario = remember(loginFeito) { authRepository.usuarioAtual() }
    val uid = usuario?.uid

    // O perfil observado do banco local. null = ainda não existe (primeira sincronização em andamento).
    val perfil by remember(uid) {
        if (uid != null) perfilRepository.observarPerfil(uid) else flowOf(null)
    }.collectAsState(initial = null)

    var mostrarDialogoApelido by remember { mutableStateOf(false) }

    // Data de nascimento (RF59): o seletor de data e a confirmação antes de gravar
    var mostrarSeletorData by remember { mutableStateOf(false) }
    var dataEscolhida by remember { mutableStateOf<String?>(null) } // "AAAA-MM-DD" aguardando confirmação

    // Exclusão de conta (RF29)
    var mostrarDialogoExcluir by remember { mutableStateOf(false) } // diálogo de confirmação aberto
    var excluindo by remember { mutableStateOf(false) }             // exclusão em andamento

    /**
     * Grava apelido e avatar no banco local. A tela atualiza sozinha, porque observa o banco.
     */
    fun salvarPublico(uidAtual: String, novo: PerfilPublico) {
        scope.launch {
            try {
                perfilRepository.salvarPerfilPublico(uidAtual, novo)
            } catch (e: Exception) {
                Log.e("Seriando", "Erro ao salvar perfil público", e)
                Toast.makeText(context, "Não foi possível salvar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Grava a data de nascimento no banco local. A trava de uma única correção (RN17) é aplicada
     * pelo PerfilRepository; aqui só se mostra o resultado ao usuário.
     */
    fun salvarDataNascimento(uidAtual: String, data: String) {
        scope.launch {
            try {
                val mensagem = when (perfilRepository.salvarDataNascimento(uidAtual, data)) {
                    ResultadoDataNascimento.SALVA -> "Data de nascimento salva."
                    ResultadoDataNascimento.CORRIGIDA -> "Data corrigida. Ela agora está travada."
                    ResultadoDataNascimento.TRAVADA -> "A data já foi corrigida e não pode mais ser alterada."
                    ResultadoDataNascimento.INVALIDA -> "Data inválida."
                }
                Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("Seriando", "Erro ao salvar data de nascimento", e)
                Toast.makeText(context, "Não foi possível salvar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Exclui a conta e todos os dados (RF29).
     * O ContaRepository pede o login do Google de novo ANTES de apagar qualquer coisa:
     * se o usuário cancelar ali, nada foi apagado.
     * No sucesso, limpa os caches e o banco local (AuthRepository.sair).
     */
    fun excluirConta() {
        excluindo = true
        scope.launch {
            try {
                contaRepository.excluirConta(context, webClientId)
                authRepository.sair() // limpa os caches locais e o banco local
                aoSair() // volta para a tela de entrada
                Toast.makeText(context, "Conta excluída.", Toast.LENGTH_LONG).show()
            } catch (e: GetCredentialCancellationException) {
                // O usuário fechou o seletor de contas do Google: nada foi apagado
                Log.d("Seriando", "Exclusão de conta cancelada pelo usuário")
                Toast.makeText(context, "Exclusão cancelada. Nada foi apagado.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Pode ter apagado só parte dos dados; tentar de novo termina o serviço
                Log.e("Seriando", "Erro ao excluir a conta", e)
                Toast.makeText(context, "Não foi possível excluir a conta. Tente novamente.", Toast.LENGTH_LONG).show()
            } finally {
                excluindo = false
            }
        }
    }

    // Cópia local do perfil, para o Kotlin permitir testar null e usar o valor em seguida
    val perfilAtual = perfil
    // Data já salva (pode ser null, ou o texto antigo em formato livre)
    val dataSalva = perfilAtual?.privado?.aniversario

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = "Conta", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (usuario == null || uid == null) {
            // Visitante (RF54): aviso com botão "Entrar", que abre a folha de login
            AvisoDeLoginNecessario(
                mensagem = "Entre com sua conta para ver e editar seu perfil.",
                aoLogarComSucesso = { loginFeito++ }
            )
        } else if (perfilAtual == null) {
            // O perfil ainda não chegou ao banco local; na primeira abertura depois de entrar
            // ele vem da nuvem, o que precisa de internet
            Text(text = "Carregando perfil...", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Na primeira vez, o perfil é baixado da nuvem e precisa de conexão com a internet.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            val publico = perfilAtual.publico

            // Avatar: foto do Google, ou o emoji escolhido pelo usuário.
            if (publico.avatarId == "google") {
                AsyncImage(
                    model = usuario.photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = publico.avatarId, style = MaterialTheme.typography.displayMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Escolher avatar do app:", style = MaterialTheme.typography.labelMedium)
            SeletorAvatar(
                avatarSelecionado = publico.avatarId,
                aoSelecionar = { novoAvatar ->
                    salvarPublico(uid, publico.copy(avatarId = novoAvatar))
                }
            )
            TextButton(onClick = {
                salvarPublico(uid, publico.copy(avatarId = "google"))
            }) {
                Text("Usar foto do Google")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = publico.apelido, style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { mostrarDialogoApelido = true }) {
                    Text("Editar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "E-mail", style = MaterialTheme.typography.labelMedium)
            Text(text = usuario.email.orEmpty(), style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- Data de nascimento (RF59) ----------
            val temData = DataNascimento.valida(dataSalva)
            val corrigida = perfilAtual.aniversarioCorrigido

            Text(text = "Data de nascimento", style = MaterialTheme.typography.labelMedium)
            Text(
                text = if (temData) DataNascimento.paraExibicao(dataSalva).orEmpty() else "Não informada",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Só você vê esta data. Ela é usada para filtrar a classificação indicativa das obras.",
                style = MaterialTheme.typography.bodySmall
            )
            // Aniversário antigo em texto livre (ex: "15/03"): não serve para calcular a idade
            if (!temData && !dataSalva.isNullOrBlank()) {
                Text(
                    text = "Seu aniversário antigo (\"$dataSalva\") não vale mais: informe a data completa.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                // Ainda não há data válida: o primeiro preenchimento é livre
                !temData -> Button(onClick = { mostrarSeletorData = true }) {
                    Text("Informar data de nascimento")
                }
                // Há data e a correção ainda não foi usada: uma única correção (RN17)
                !corrigida -> {
                    OutlinedButton(onClick = { mostrarSeletorData = true }) {
                        Text("Corrigir data")
                    }
                    Text(
                        text = "Você pode corrigir a data uma única vez.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // A correção já foi usada: a data está travada
                else -> Text(
                    text = "A data está travada: a única correção já foi usada.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pede confirmação se houver alterações não sincronizadas (RN27)
            BotaoSair(texto = "Sair da conta", aoSairConfirmado = aoSair)

            // Exclusão da conta (RF29): em vermelho, separada dos outros botões
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = { mostrarDialogoExcluir = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Excluir minha conta")
            }
        }
    }

    if (mostrarDialogoApelido && uid != null && perfilAtual != null) {
        DialogoEditarApelido(
            valorInicial = perfilAtual.publico.apelido,
            aoFechar = { mostrarDialogoApelido = false },
            aoConfirmar = { novoApelido ->
                salvarPublico(uid, perfilAtual.publico.copy(apelido = novoApelido))
                mostrarDialogoApelido = false
            }
        )
    }

    // Seletor de data: ao confirmar, guarda a escolha e passa para a confirmação abaixo
    if (mostrarSeletorData && uid != null) {
        SeletorDataNascimento(
            dataInicial = dataSalva,
            aoConfirmar = { data ->
                mostrarSeletorData = false
                dataEscolhida = data
            },
            aoFechar = { mostrarSeletorData = false }
        )
    }

    // Confirmação antes de gravar: a data trava, então o usuário precisa saber disso (RN17)
    val escolhida = dataEscolhida
    if (escolhida != null && uid != null) {
        val ehCorrecao = DataNascimento.valida(dataSalva)
        AlertDialog(
            onDismissRequest = { dataEscolhida = null },
            title = { Text(if (ehCorrecao) "Corrigir a data?" else "Salvar a data?") },
            text = {
                Text(
                    "Data: ${DataNascimento.paraExibicao(escolhida)}\n\n" +
                            (if (ehCorrecao) "Esta é a sua única correção: depois dela, a data fica travada."
                            else "Depois de salva, a data só poderá ser corrigida uma vez.")
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dataEscolhida = null
                    salvarDataNascimento(uid, escolhida)
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { dataEscolhida = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirmação da exclusão: explica o que será apagado e o que é mantido
    if (mostrarDialogoExcluir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoExcluir = false },
            title = { Text("Excluir sua conta?") },
            text = {
                Text(
                    "Isto apaga de forma permanente seu perfil, sua lista, seu progresso, " +
                            "suas avaliações, suas opiniões e seus votos. Não tem como desfazer.\n\n" +
                            "As denúncias que você enviou são mantidas para a moderação.\n\n" +
                            "Para confirmar, você vai entrar com sua conta Google de novo."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoExcluir = false
                        excluirConta()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoExcluir = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Progresso da exclusão: não fecha (nem com o botão voltar) até terminar,
    // para o usuário não sair da tela no meio e deixar a exclusão pela metade
    if (excluindo) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Excluindo sua conta...") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Aguarde, isso pode levar alguns instantes.")
                }
            },
            confirmButton = { }
        )
    }
}