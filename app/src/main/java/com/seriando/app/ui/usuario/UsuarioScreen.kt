package com.seriando.app.ui.usuario

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.seriando.app.model.PerfilPrivado
import com.seriando.app.model.PerfilPublico
import com.seriando.app.ui.home.DialogoEditarApelido
import kotlinx.coroutines.launch

/**
 * Aba Usuário (RF12 a RF15, RF29): apelido, foto/avatar, e-mail, aniversário
 * e exclusão da conta.
 * Toda gravação no Firestore tem try/catch com Log e Toast, porque as regras
 * de segurança podem recusá-la (RNF01) e a falha não pode derrubar o app.
 */
@Composable
fun UsuarioScreen() {

    val authRepository = remember { AuthRepository() }
    val contaRepository = remember { ContaRepository() }
    val context = LocalContext.current
    val perfilRepository = remember { PerfilRepository() }
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.default_web_client_id)
    val usuario = authRepository.usuarioAtual()

    // Começa com um valor padrão (não nulo), em vez de null — assim a tela
    // sempre tem algo para mostrar, mesmo antes da busca no Firestore terminar.
    var perfilPublico by remember { mutableStateOf(PerfilPublico()) }
    var perfilPrivado by remember { mutableStateOf<PerfilPrivado?>(null) }
    var mostrarDialogoApelido by remember { mutableStateOf(false) }
    var aniversario by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }

    // Exclusão de conta (RF29)
    var mostrarDialogoExcluir by remember { mutableStateOf(false) } // diálogo de confirmação aberto
    var excluindo by remember { mutableStateOf(false) }             // exclusão em andamento

    /**
     * Salva o perfil público (apelido/avatar) mostrando a mudança na tela na hora.
     * Se a gravação falhar (rede ou regra de segurança), volta ao valor anterior
     * e avisa o usuário, para a tela nunca mostrar algo que não foi salvo.
     */
    fun salvarPublico(uid: String, novo: PerfilPublico) {
        val anterior = perfilPublico
        perfilPublico = novo
        scope.launch {
            try {
                perfilRepository.salvarPerfilPublico(uid, novo)
            } catch (e: Exception) {
                Log.e("Seriando", "Erro ao salvar perfil público", e)
                perfilPublico = anterior
                Toast.makeText(context, "Não foi possível salvar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Exclui a conta e todos os dados (RF29).
     * O ContaRepository pede o login do Google de novo ANTES de apagar qualquer coisa:
     * se o usuário cancelar ali, nada foi apagado.
     * No sucesso, limpa os caches locais (RF27); ao terminar, "excluindo" volta a false,
     * a tela recompõe e, sem usuário, mostra o aviso de login.
     */
    fun excluirConta() {
        excluindo = true
        scope.launch {
            try {
                contaRepository.excluirConta(context, webClientId)
                authRepository.sair() // limpa os caches locais
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

    // Carrega os dois perfis ao abrir; cria o público se ainda não existir.
    LaunchedEffect(usuario?.uid) {
        val uid = usuario?.uid
        if (uid != null) {
            try {
                val publicoSalvo = perfilRepository.buscarPerfilPublico(uid)

                if (publicoSalvo != null) {
                    perfilPublico = publicoSalvo
                } else {
                    // Perfil público nunca foi criado (ex: login antigo, antes
                    // dessa tela existir). Cria agora, com o primeiro nome da
                    // conta Google como sugestão, em vez de deixar a tela vazia.
                    // ifBlank: nome vazio viraria apelido vazio, que a regra recusa.
                    val primeiroNome = (usuario.displayName ?: "").trim().split(" ").firstOrNull()
                        .orEmpty().ifBlank { "Usuário" }
                    val novoPerfil = PerfilPublico(apelido = primeiroNome, avatarId = "google")
                    perfilRepository.salvarPerfilPublico(uid, novoPerfil)
                    perfilPublico = novoPerfil
                }

                val privadoSalvo = perfilRepository.buscarPerfilPrivado(uid)
                perfilPrivado = privadoSalvo
                aniversario = privadoSalvo?.aniversario ?: ""
            } catch (e: Exception) {
                Log.e("Seriando", "Erro ao carregar perfil", e)
                Toast.makeText(context, "Não foi possível carregar o perfil.", Toast.LENGTH_SHORT).show()
            } finally {
                // Sai do "Carregando..." mesmo com falha, para a tela não ficar travada
                carregando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = "Conta", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (usuario == null) {
            Text(
                text = "Entre com sua conta na Home para ver e editar seu perfil.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (carregando) {
            Text(text = "Carregando perfil...", style = MaterialTheme.typography.bodyMedium)
        } else {
            // Avatar: foto do Google, ou o emoji escolhido pelo usuário.
            if (perfilPublico.avatarId == "google") {
                AsyncImage(
                    model = usuario.photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = perfilPublico.avatarId, style = MaterialTheme.typography.displayMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Escolher avatar do app:", style = MaterialTheme.typography.labelMedium)
            SeletorAvatar(
                avatarSelecionado = perfilPublico.avatarId,
                aoSelecionar = { novoAvatar ->
                    salvarPublico(usuario.uid, perfilPublico.copy(avatarId = novoAvatar))
                }
            )
            TextButton(onClick = {
                salvarPublico(usuario.uid, perfilPublico.copy(avatarId = "google"))
            }) {
                Text("Usar foto do Google")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = perfilPublico.apelido, style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { mostrarDialogoApelido = true }) {
                    Text("Editar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "E-mail", style = MaterialTheme.typography.labelMedium)
            Text(text = perfilPrivado?.email ?: usuario.email ?: "", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = aniversario,
                // Limite de 20 caracteres: é o máximo aceito pelas regras do Firestore
                onValueChange = { if (it.length <= 20) aniversario = it },
                label = { Text("Aniversário (ex: 15/03)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    try {
                        val privadoAtual = perfilPrivado ?: PerfilPrivado(email = usuario.email ?: "")
                        val atualizado = privadoAtual.copy(aniversario = aniversario.ifBlank { null })
                        perfilRepository.salvarPerfilPrivado(usuario.uid, atualizado)
                        perfilPrivado = atualizado
                        Toast.makeText(context, "Aniversário salvo.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("Seriando", "Erro ao salvar aniversário", e)
                        Toast.makeText(context, "Não foi possível salvar.", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Salvar aniversário")
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { authRepository.sair() }) {
                Text("Sair da conta")
            }

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

    if (mostrarDialogoApelido && usuario != null) {
        DialogoEditarApelido(
            valorInicial = perfilPublico.apelido,
            aoFechar = { mostrarDialogoApelido = false },
            aoConfirmar = { novoApelido ->
                salvarPublico(usuario.uid, perfilPublico.copy(apelido = novoApelido))
                mostrarDialogoApelido = false
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