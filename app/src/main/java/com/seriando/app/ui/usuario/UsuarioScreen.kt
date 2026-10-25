package com.seriando.app.ui.usuario

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.seriando.app.BuildConfig
import com.seriando.app.R
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.local.PreferenciasApp
import com.seriando.app.data.repository.ContaRepository
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.data.repository.ResultadoDataNascimento
import com.seriando.app.model.ControleDeIdade
import com.seriando.app.model.DataNascimento
import com.seriando.app.model.ModoDoTema
import com.seriando.app.model.PerfilPublico
import com.seriando.app.ui.comum.BotaoSair
import com.seriando.app.ui.home.DialogoEditarApelido
import com.seriando.app.ui.login.AvisoDeLoginNecessario
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Texto da faixa de classificação indicativa liberada para uma idade efetiva (RF50).
 * As classificações do Brasil são L, 10, 12, 14, 16 e 18; vale a maior que não passa da idade.
 */
private fun textoDaFaixaLiberada(idadeEfetiva: Int): String = when {
    idadeEfetiva >= ControleDeIdade.MAIORIDADE -> "Todas as classificações"
    idadeEfetiva >= 16 -> "Até 16 anos"
    idadeEfetiva >= 14 -> "Até 14 anos"
    idadeEfetiva >= 12 -> "Até 12 anos"
    idadeEfetiva >= 10 -> "Até 10 anos"
    else -> "Apenas livre para todos os públicos (L)"
}

/**
 * Aba Conta (RF12 a RF15, RF29, RF33, RF59): cabeçalho com avatar, apelido e e-mail;
 * seções Perfil (apelido, e-mail, data de nascimento e faixa liberada), Sobre (Termos de Uso,
 * Política de Privacidade e versão) e Conta (sair e excluir).
 * O perfil vem do banco local (Room), que a tela observa: funciona sem internet e se atualiza
 * sozinho quando a sincronização traz mudanças da nuvem (RN21).
 * Toda gravação tem try/catch com Log e Toast, porque a falha não pode derrubar o app.
 *
 * @param aoSair             chamado depois do logout ou da exclusão da conta; quem navega é o SeriandoNavHost (RF53).
 * @param aoAbrirTermos      abre a tela dos Termos de Uso.
 * @param aoAbrirPrivacidade abre a tela da Política de Privacidade.
 */
@Composable
fun UsuarioScreen(
    aoSair: () -> Unit = {},
    aoAbrirTermos: () -> Unit = {},
    aoAbrirPrivacidade: () -> Unit = {}
) {

    val authRepository = remember { AuthRepository() }
    val contaRepository = remember { ContaRepository() }
    val context = LocalContext.current
    val perfilRepository = remember { PerfilRepository() }
    val scope = rememberCoroutineScope()

    // Modo do tema (RF80): preferência só do aparelho, lida e gravada no DataStore. A MainActivity
    // observa a mesma preferência, então o tema do app muda assim que o usuário escolhe.
    val preferencias = remember { PreferenciasApp(context) }
    val modoDoTema by preferencias.modoDoTema.collectAsState(initial = ModoDoTema.SISTEMA)
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

    // Escolha do avatar (RF15): folha inferior com a foto do Google e as 15 fotos do app
    var mostrarSeletorAvatar by remember { mutableStateOf(false) }

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
            .padding(horizontal = 20.dp, vertical = 24.dp),
        // Espaço fixo entre o título, o cabeçalho e cada seção
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Conta", style = MaterialTheme.typography.headlineLarge)

        if (usuario == null || uid == null) {
            // Visitante (RF54): aviso com botão "Entrar", que abre a folha de login
            AvisoDeLoginNecessario(
                mensagem = "Entre com sua conta para ver e editar seu perfil.",
                aoLogarComSucesso = { loginFeito++ }
            )
        } else if (perfilAtual == null) {
            // O perfil ainda não chegou ao banco local; na primeira abertura depois de entrar
            // ele vem da nuvem, o que precisa de internet
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Carregando perfil...", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Na primeira vez, o perfil é baixado da nuvem e precisa de conexão com a internet.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            val publico = perfilAtual.publico

            // Cabeçalho: avatar grande (toque abre a escolha), apelido e e-mail
            CabecalhoDaConta(
                avatarId = publico.avatarId,
                apelido = publico.apelido,
                email = usuario.email.orEmpty(),
                fotoGoogle = usuario.photoUrl,
                aoEditarAvatar = { mostrarSeletorAvatar = true }
            )

            // ---------- Seção Perfil: apelido, e-mail, data de nascimento (RF59) e faixa liberada ----------
            val temData = DataNascimento.valida(dataSalva)
            val corrigida = perfilAtual.aniversarioCorrigido
            // Primeira data é livre; depois dela, uma única correção (RN17); depois, travada
            val podeEditarData = !temData || !corrigida
            val faixa = textoDaFaixaLiberada(ControleDeIdade.idadeEfetiva(dataSalva))

            CartaoDeSecao(titulo = "Perfil") {
                LinhaDaConta(
                    icone = Icons.Outlined.Person,
                    titulo = "Apelido",
                    valor = publico.apelido,
                    textoDaAcao = "Editar",
                    aoClicar = { mostrarDialogoApelido = true }
                )
                SeparadorDeLinhas()
                LinhaDaConta(
                    icone = Icons.Outlined.Email,
                    titulo = "E-mail",
                    valor = usuario.email.orEmpty()
                )
                SeparadorDeLinhas()
                LinhaDaConta(
                    icone = Icons.Outlined.Cake,
                    titulo = "Data de nascimento",
                    valor = if (temData) DataNascimento.paraExibicao(dataSalva).orEmpty() else "Não informada",
                    textoDaAcao = when {
                        !temData -> "Informar"
                        !corrigida -> "Corrigir"
                        else -> null // travada: sem ação
                    },
                    aoClicar = if (podeEditarData) ({ mostrarSeletorData = true }) else null
                )
                NotaDoCartao("Só você vê esta data. Ela é usada para filtrar a classificação indicativa das obras.")
                // Aniversário antigo em texto livre (ex: "15/03"): não serve para calcular a idade
                if (!temData && !dataSalva.isNullOrBlank()) {
                    NotaDoCartao("Seu aniversário antigo (\"$dataSalva\") não vale mais: informe a data completa.")
                }
                when {
                    temData && !corrigida -> NotaDoCartao("Você pode corrigir a data uma única vez.")
                    temData && corrigida -> NotaDoCartao("A data está travada: a única correção já foi usada.")
                }
                SeparadorDeLinhas()
                LinhaDaConta(
                    icone = Icons.Outlined.Lock,
                    titulo = "Classificação liberada",
                    valor = if (temData) faixa else "$faixa (sem data informada)"
                )
            }
        }

        // ---------- Seção Aparência (RF80): tema claro, escuro ou do aparelho; vale também para o visitante ----------
        CartaoDeAparencia(
            modoAtual = modoDoTema,
            aoEscolher = { modo -> scope.launch { preferencias.definirModoDoTema(modo) } }
        )

        // ---------- Seção Sobre: aparece também para o visitante ----------
        CartaoDeSecao(titulo = "Sobre") {
            LinhaDaConta(
                icone = Icons.Outlined.Description,
                titulo = "Termos de Uso",
                aoClicar = aoAbrirTermos
            )
            SeparadorDeLinhas()
            LinhaDaConta(
                icone = Icons.Outlined.PrivacyTip,
                titulo = "Política de Privacidade",
                aoClicar = aoAbrirPrivacidade
            )
            SeparadorDeLinhas()
            LinhaDaConta(
                icone = Icons.Outlined.Info,
                titulo = "Versão",
                valor = "Seriando ${BuildConfig.VERSION_NAME}"
            )
        }

        // ---------- Seção Conta: sair e excluir (só para quem tem conta e perfil carregado) ----------
        if (usuario != null && uid != null && perfilAtual != null) {
            CartaoDeSecao(titulo = "Conta") {
                // Pede confirmação se houver alterações não sincronizadas (RN27)
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    BotaoSair(texto = "Sair da conta", aoSairConfirmado = aoSair)
                }
                SeparadorDeLinhas()
                // Exclusão da conta (RF29): em vermelho, separada do botão de sair
                LinhaDaConta(
                    icone = Icons.Outlined.Delete,
                    titulo = "Excluir minha conta",
                    valor = "Apaga todos os seus dados. Não tem como desfazer.",
                    corDeDestaque = MaterialTheme.colorScheme.error,
                    aoClicar = { mostrarDialogoExcluir = true }
                )
            }
        }
    }

    // Escolha do avatar: grava assim que o usuário toca em uma opção (a folha fecha em seguida)
    if (mostrarSeletorAvatar && uid != null && perfilAtual != null) {
        SeletorAvatar(
            avatarSelecionado = perfilAtual.publico.avatarId,
            apelido = perfilAtual.publico.apelido,
            fotoGoogle = usuario?.photoUrl,
            aoSelecionar = { novoAvatar ->
                salvarPublico(uid, perfilAtual.publico.copy(avatarId = novoAvatar))
            },
            aoFechar = { mostrarSeletorAvatar = false }
        )
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
