package com.seriando.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import coil3.compose.AsyncImage
import com.seriando.app.BuildConfig
import com.seriando.app.R
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.model.PerfilPublico
import com.seriando.app.model.ResultadoBusca
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.width

@Composable
fun HomeScreen(
    aoClicarEmPesquisar: () -> Unit,
    aoClicarEmItem: (Int, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    val perfilRepository = remember { PerfilRepository() }
    val webClientId = stringResource(R.string.default_web_client_id)

    var usuario by remember { mutableStateOf(authRepository.usuarioAtual()) }
    var destaques by remember { mutableStateOf<List<ResultadoBusca>>(emptyList()) }
    var perfilPublico by remember { mutableStateOf<PerfilPublico?>(null) }

    LaunchedEffect(Unit) {
        try {
            val resposta = RetrofitInstance.api.buscarEmAlta(BuildConfig.TMDB_API_KEY)
            destaques = resposta.results
                .filter { it.caminhoCapa != null }
                .shuffled()
        } catch (e: Exception) {
            destaques = emptyList()
        }
    }

    // Busca o perfil público (apelido + avatar) sempre que o usuário muda.
    // Mostrado aqui só para leitura; editar é só na aba Usuário.
    LaunchedEffect(usuario?.uid) {
        val uid = usuario?.uid
        perfilPublico = if (uid != null) perfilRepository.buscarPerfilPublico(uid) else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SERIANDO",
                style = MaterialTheme.typography.headlineLarge
            )

            if (usuario != null) {
                TextButton(onClick = {
                    authRepository.sair()
                    usuario = null
                    perfilPublico = null
                }) {
                    Text("Sair")
                }
            } else {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            usuario = authRepository.entrarComGoogle(context, webClientId)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Não foi possível entrar.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }) {
                    Text("Entrar")
                }
            }
        }

        // Nome e foto do usuário logado, só leitura (editar fica na aba Usuário).
        val publico = perfilPublico
        if (usuario != null && publico != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (publico.avatarId == "google") {
                    AsyncImage(
                        model = usuario?.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = publico.avatarId, style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Olá, ${publico.apelido}!",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Organize suas séries. Nunca perca onde parou.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = aoClicarEmPesquisar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pesquisar série ou filme")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Em destaque",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(destaques) { item ->
                DestaqueCard(
                    resultado = item,
                    aoClicar = {
                        val tipoMidia = if (item.ehFilme) "movie" else "tv"
                        aoClicarEmItem(item.id, tipoMidia)
                    }
                )
            }
        }
    }
}