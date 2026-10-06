package com.seriando.app.ui.usuario

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.model.PerfilPrivado
import com.seriando.app.model.PerfilPublico
import com.seriando.app.ui.home.DialogoEditarApelido
import kotlinx.coroutines.launch


@Composable
fun UsuarioScreen() {

    val authRepository = remember { AuthRepository() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val perfilRepository = remember { PerfilRepository() }
    val scope = rememberCoroutineScope()
    val usuario = authRepository.usuarioAtual()

    // Começa com um valor padrão (não nulo), em vez de null — assim a tela
    // sempre tem algo para mostrar, mesmo antes da busca no Firestore terminar.
    var perfilPublico by remember { mutableStateOf(PerfilPublico()) }
    var perfilPrivado by remember { mutableStateOf<PerfilPrivado?>(null) }
    var mostrarDialogoApelido by remember { mutableStateOf(false) }
    var aniversario by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }

    LaunchedEffect(usuario?.uid) {
        val uid = usuario?.uid
        if (uid != null) {
            val publicoSalvo = perfilRepository.buscarPerfilPublico(uid)

            if (publicoSalvo != null) {
                perfilPublico = publicoSalvo
            } else {
                // Perfil público nunca foi criado (ex: login antigo, antes
                // dessa tela existir). Cria agora, com o primeiro nome da
                // conta Google como sugestão, em vez de deixar a tela vazia.
                val primeiroNome = (usuario.displayName ?: "Usuário").trim().split(" ").firstOrNull() ?: "Usuário"
                val novoPerfil = PerfilPublico(apelido = primeiroNome, avatarId = "google")
                perfilRepository.salvarPerfilPublico(uid, novoPerfil)
                perfilPublico = novoPerfil
            }

            val privadoSalvo = perfilRepository.buscarPerfilPrivado(uid)
            perfilPrivado = privadoSalvo
            aniversario = privadoSalvo?.aniversario ?: ""

            carregando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = "Usuário", style = MaterialTheme.typography.headlineLarge)
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
                    val atualizado = perfilPublico.copy(avatarId = novoAvatar)
                    perfilPublico = atualizado // atualiza a tela na hora
                    scope.launch {
                        perfilRepository.salvarPerfilPublico(usuario.uid, atualizado)
                    }
                }
            )
            TextButton(onClick = {
                val atualizado = perfilPublico.copy(avatarId = "google")
                perfilPublico = atualizado
                scope.launch {
                    perfilRepository.salvarPerfilPublico(usuario.uid, atualizado)
                }
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
                onValueChange = { aniversario = it },
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
                        android.widget.Toast.makeText(context, "Aniversário salvo.", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("Seriando", "Erro ao salvar aniversário", e)
                        android.widget.Toast.makeText(context, "Não foi possível salvar.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Salvar aniversário")
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { authRepository.sair() }) {
                Text("Sair da conta")
            }
        }
    }

    if (mostrarDialogoApelido && usuario != null) {
        DialogoEditarApelido(
            valorInicial = perfilPublico.apelido,
            aoFechar = { mostrarDialogoApelido = false },
            aoConfirmar = { novoApelido ->
                val atualizado = perfilPublico.copy(apelido = novoApelido)
                perfilPublico = atualizado
                scope.launch {
                    perfilRepository.salvarPerfilPublico(usuario.uid, atualizado)
                }
                mostrarDialogoApelido = false
            }
        )
    }
}