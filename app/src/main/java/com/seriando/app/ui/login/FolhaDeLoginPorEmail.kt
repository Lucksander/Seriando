package com.seriando.app.ui.login

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.auth.OperacaoDeAuth
import com.seriando.app.data.auth.mensagemDeErroDeAuth
import com.seriando.app.model.ValidacaoDeConta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Telas da folha, na ordem em que o usuário costuma passar por elas. */
private enum class Etapa { ENTRAR, CRIAR_CONTA, ESQUECI_SENHA, CONFIRMAR_EMAIL }

/** Segundos de espera entre dois reenvios do e-mail de confirmação (evita abuso e bloqueio do Firebase). */
private const val ESPERA_PARA_REENVIAR = 30

/**
 * Folha inferior de acesso com e-mail e senha (RF32). Reúne, na mesma folha, as quatro telas do
 * fluxo: entrar, criar conta, "esqueci minha senha" e "confirme seu e-mail".
 *
 * Quem abre a folha com uma conta ainda não confirmada cai direto na confirmação. O login só
 * é considerado concluído, e [aoLogarComSucesso] só é chamado, quando o e-mail está confirmado.
 *
 * @param aoFechar          chamada ao deslizar a folha para baixo ou tocar fora dela.
 * @param aoLogarComSucesso chamada quando o usuário entrou com o e-mail confirmado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolhaDeLoginPorEmail(
    aoFechar: () -> Unit,
    aoLogarComSucesso: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    // Começa pela confirmação se já existe uma conta criada e ainda não confirmada
    var etapa by remember {
        mutableStateOf(if (authRepository.precisaConfirmarEmail()) Etapa.CONFIRMAR_EMAIL else Etapa.ENTRAR)
    }
    // O e-mail digitado é compartilhado entre as telas: quem erra a senha e toca em
    // "Esqueci minha senha" não precisa digitá-lo de novo
    var email by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = aoFechar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Mantém o conteúdo acima do teclado e da barra de navegação, e rolável
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (etapa) {
                Etapa.ENTRAR -> EtapaEntrar(
                    email = email,
                    aoMudarEmail = { email = it },
                    authRepository = authRepository,
                    aoEsqueciSenha = { etapa = Etapa.ESQUECI_SENHA },
                    aoCriarConta = { etapa = Etapa.CRIAR_CONTA },
                    aoPrecisarConfirmar = { etapa = Etapa.CONFIRMAR_EMAIL },
                    aoLogarComSucesso = aoLogarComSucesso
                )

                Etapa.CRIAR_CONTA -> EtapaCriarConta(
                    email = email,
                    aoMudarEmail = { email = it },
                    authRepository = authRepository,
                    aoJaTenhoConta = { etapa = Etapa.ENTRAR },
                    aoContaCriada = { etapa = Etapa.CONFIRMAR_EMAIL }
                )

                Etapa.ESQUECI_SENHA -> EtapaEsqueciSenha(
                    email = email,
                    aoMudarEmail = { email = it },
                    authRepository = authRepository,
                    aoVoltar = { etapa = Etapa.ENTRAR }
                )

                Etapa.CONFIRMAR_EMAIL -> EtapaConfirmarEmail(
                    authRepository = authRepository,
                    aoConfirmado = aoLogarComSucesso,
                    aoUsarOutraConta = { etapa = Etapa.ENTRAR }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------
// Etapa 1: entrar
// ---------------------------------------------------------------------------------------

/** Tela de login: e-mail e senha, com atalhos para criar conta e recuperar a senha. */
@Composable
private fun EtapaEntrar(
    email: String,
    aoMudarEmail: (String) -> Unit,
    authRepository: AuthRepository,
    aoEsqueciSenha: () -> Unit,
    aoCriarConta: () -> Unit,
    aoPrecisarConfirmar: () -> Unit,
    aoLogarComSucesso: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var senha by remember { mutableStateOf("") }
    var erroDoEmail by remember { mutableStateOf<String?>(null) }
    var erroDaSenha by remember { mutableStateOf<String?>(null) }
    var erroGeral by remember { mutableStateOf<String?>(null) } // erro vindo do Firebase
    var carregando by remember { mutableStateOf(false) }

    /** Valida os campos e, se estiverem certos, tenta entrar. */
    fun entrar() {
        if (carregando) return
        erroDoEmail = ValidacaoDeConta.erroDoEmail(email)
        // No login só se exige que a senha exista; a regra de tamanho vale apenas no cadastro
        erroDaSenha = if (senha.isEmpty()) "Informe sua senha." else null
        erroGeral = null
        if (erroDoEmail != null || erroDaSenha != null) return

        carregando = true
        scope.launch {
            try {
                authRepository.entrarComEmail(email, senha)
                // E-mail ainda não confirmado: segue para a tela de confirmação
                if (authRepository.precisaConfirmarEmail()) aoPrecisarConfirmar() else aoLogarComSucesso()
            } catch (e: Exception) {
                Log.e("FolhaDeLoginPorEmail", "Falha ao entrar com e-mail", e)
                erroGeral = mensagemDeErroDeAuth(e, OperacaoDeAuth.ENTRAR)
            } finally {
                carregando = false
            }
        }
    }

    TituloDaEtapa("Entrar com e-mail")

    CampoDeConta(
        valor = email, aoMudar = aoMudarEmail, rotulo = "E-mail", erro = erroDoEmail,
        teclado = KeyboardType.Email, acaoDoTeclado = ImeAction.Next, habilitado = !carregando
    )
    CampoDeConta(
        valor = senha, aoMudar = { senha = it }, rotulo = "Senha", erro = erroDaSenha,
        acaoDoTeclado = ImeAction.Done, aoAcao = { entrar() }, ehSenha = true, habilitado = !carregando
    )

    MensagemDeErro(erroGeral)

    BotaoPrincipal(texto = "Entrar", textoCarregando = "Entrando...", carregando = carregando, aoClicar = { entrar() })

    TextButton(onClick = aoEsqueciSenha, enabled = !carregando, modifier = Modifier.fillMaxWidth()) {
        Text("Esqueci minha senha")
    }
    OutlinedButton(
        onClick = aoCriarConta, enabled = !carregando,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Text("Criar uma conta")
    }
}

// ---------------------------------------------------------------------------------------
// Etapa 2: criar conta
// ---------------------------------------------------------------------------------------

/** Tela de cadastro: apelido, e-mail, senha e repetição da senha. */
@Composable
private fun EtapaCriarConta(
    email: String,
    aoMudarEmail: (String) -> Unit,
    authRepository: AuthRepository,
    aoJaTenhoConta: () -> Unit,
    aoContaCriada: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var apelido by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var erroDoApelido by remember { mutableStateOf<String?>(null) }
    var erroDoEmail by remember { mutableStateOf<String?>(null) }
    var erroDaSenha by remember { mutableStateOf<String?>(null) }
    var erroDaConfirmacao by remember { mutableStateOf<String?>(null) }
    var erroGeral by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    /** Valida todos os campos de uma vez (cada um mostra o seu erro) e cria a conta se estiver tudo certo. */
    fun criar() {
        if (carregando) return
        erroDoApelido = ValidacaoDeConta.erroDoApelido(apelido)
        erroDoEmail = ValidacaoDeConta.erroDoEmail(email)
        erroDaSenha = ValidacaoDeConta.erroDaSenha(senha)
        erroDaConfirmacao = ValidacaoDeConta.erroDaConfirmacao(senha, confirmacao)
        erroGeral = null
        if (listOf(erroDoApelido, erroDoEmail, erroDaSenha, erroDaConfirmacao).any { it != null }) return

        carregando = true
        scope.launch {
            try {
                authRepository.criarContaComEmail(email, senha, apelido)
                aoContaCriada() // segue para "Confirme seu e-mail"
            } catch (e: Exception) {
                Log.e("FolhaDeLoginPorEmail", "Falha ao criar a conta", e)
                erroGeral = mensagemDeErroDeAuth(e, OperacaoDeAuth.CRIAR_CONTA)
            } finally {
                carregando = false
            }
        }
    }

    TituloDaEtapa("Criar conta")

    CampoDeConta(
        valor = apelido, aoMudar = { apelido = it }, rotulo = "Apelido", erro = erroDoApelido,
        acaoDoTeclado = ImeAction.Next, capitalizacao = KeyboardCapitalization.Words, habilitado = !carregando
    )
    CampoDeConta(
        valor = email, aoMudar = aoMudarEmail, rotulo = "E-mail", erro = erroDoEmail,
        teclado = KeyboardType.Email, acaoDoTeclado = ImeAction.Next, habilitado = !carregando
    )
    CampoDeConta(
        valor = senha, aoMudar = { senha = it }, rotulo = "Senha", erro = erroDaSenha,
        acaoDoTeclado = ImeAction.Next, ehSenha = true, habilitado = !carregando,
        dica = "Mínimo de ${ValidacaoDeConta.SENHA_MINIMA} caracteres, com letras e números."
    )
    CampoDeConta(
        valor = confirmacao, aoMudar = { confirmacao = it }, rotulo = "Repita a senha", erro = erroDaConfirmacao,
        acaoDoTeclado = ImeAction.Done, aoAcao = { criar() }, ehSenha = true, habilitado = !carregando
    )

    MensagemDeErro(erroGeral)

    BotaoPrincipal(texto = "Criar conta", textoCarregando = "Criando...", carregando = carregando, aoClicar = { criar() })

    Text(
        text = "Ao criar a conta, você concorda com os Termos de Uso e a Política de Privacidade do Seriando.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    TextButton(onClick = aoJaTenhoConta, enabled = !carregando, modifier = Modifier.fillMaxWidth()) {
        Text("Já tenho uma conta")
    }
}

// ---------------------------------------------------------------------------------------
// Etapa 3: esqueci minha senha
// ---------------------------------------------------------------------------------------

/**
 * Tela de recuperação de senha. Com a proteção contra enumeração de e-mails, o Firebase responde
 * com sucesso mesmo se o e-mail não tiver conta, então a mensagem de sucesso é sempre a mesma
 * e não revela se o e-mail existe. Quem tem conta Google com esse e-mail também pode usar o link
 * para criar uma senha e passar a entrar pelos dois métodos, na mesma conta.
 */
@Composable
private fun EtapaEsqueciSenha(
    email: String,
    aoMudarEmail: (String) -> Unit,
    authRepository: AuthRepository,
    aoVoltar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var erroDoEmail by remember { mutableStateOf<String?>(null) }
    var erroGeral by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }
    var enviado by remember { mutableStateOf(false) }

    fun enviar() {
        if (carregando) return
        erroDoEmail = ValidacaoDeConta.erroDoEmail(email)
        erroGeral = null
        if (erroDoEmail != null) return

        carregando = true
        scope.launch {
            try {
                authRepository.enviarRedefinicaoDeSenha(email)
                enviado = true
            } catch (e: Exception) {
                Log.e("FolhaDeLoginPorEmail", "Falha ao enviar a redefinição de senha", e)
                erroGeral = mensagemDeErroDeAuth(e, OperacaoDeAuth.REDEFINIR_SENHA)
            } finally {
                carregando = false
            }
        }
    }

    TituloDaEtapa("Esqueci minha senha")

    if (enviado) {
        // Mesma mensagem exista ou não conta com esse e-mail
        Text(
            text = "Se existir uma conta com esse e-mail, enviamos um link para criar uma nova senha. " +
                    "Confira também a caixa de spam.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        BotaoPrincipal(texto = "Voltar para entrar", textoCarregando = "", carregando = false, aoClicar = aoVoltar)
    } else {
        Text(
            text = "Informe o e-mail da sua conta e enviaremos um link para você criar uma nova senha.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        CampoDeConta(
            valor = email, aoMudar = aoMudarEmail, rotulo = "E-mail", erro = erroDoEmail,
            teclado = KeyboardType.Email, acaoDoTeclado = ImeAction.Done, aoAcao = { enviar() },
            habilitado = !carregando
        )
        MensagemDeErro(erroGeral)
        BotaoPrincipal(texto = "Enviar link", textoCarregando = "Enviando...", carregando = carregando, aoClicar = { enviar() })
        TextButton(onClick = aoVoltar, enabled = !carregando, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }
}

// ---------------------------------------------------------------------------------------
// Etapa 4: confirme seu e-mail
// ---------------------------------------------------------------------------------------

/**
 * Tela "Confirme seu e-mail". O usuário toca no link recebido, volta ao app e toca em
 * "Já confirmei". O reenvio tem uma espera de 30 segundos entre dois pedidos.
 */
@Composable
private fun EtapaConfirmarEmail(
    authRepository: AuthRepository,
    aoConfirmado: () -> Unit,
    aoUsarOutraConta: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val emailDaConta = remember { authRepository.emailDaContaAtual().orEmpty() }

    var carregando by remember { mutableStateOf(false) }
    // Aviso mostrado abaixo do texto; o par diz se é um erro (vermelho) ou só uma informação
    var aviso by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var segundosParaReenviar by remember { mutableIntStateOf(0) }

    // Contagem regressiva do reenvio: a cada mudança do valor, espera 1 segundo e desconta 1
    LaunchedEffect(segundosParaReenviar) {
        if (segundosParaReenviar > 0) {
            delay(1000)
            segundosParaReenviar -= 1
        }
    }

    // Box para centralizar o ícone (esta função não está dentro do escopo da Column)
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null, // decorativo: o título já descreve a tela
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    TituloDaEtapa("Confirme seu e-mail")
    Text(
        text = "Enviamos um link de confirmação para $emailDaConta. Toque nele e depois volte aqui " +
                "para continuar. Se não encontrar, olhe a caixa de spam.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    // Aviso do último resultado: erro em vermelho, informação na cor normal
    aviso?.let { (texto, ehErro) ->
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = if (ehErro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    BotaoPrincipal(
        texto = "Já confirmei", textoCarregando = "Verificando...", carregando = carregando,
        aoClicar = {
            if (!carregando) {
                carregando = true
                aviso = null
                scope.launch {
                    try {
                        if (authRepository.verificarConfirmacaoDoEmail()) {
                            aoConfirmado()
                        } else {
                            aviso = "Ainda não vimos a confirmação. Toque no link do e-mail e tente de novo." to true
                        }
                    } catch (e: Exception) {
                        Log.e("FolhaDeLoginPorEmail", "Falha ao verificar a confirmação", e)
                        aviso = mensagemDeErroDeAuth(e, OperacaoDeAuth.OUTRA) to true
                    } finally {
                        carregando = false
                    }
                }
            }
        }
    )

    OutlinedButton(
        onClick = {
            scope.launch {
                try {
                    authRepository.reenviarEmailDeConfirmacao()
                    aviso = "E-mail reenviado. Confira também a caixa de spam." to false
                    segundosParaReenviar = ESPERA_PARA_REENVIAR
                } catch (e: Exception) {
                    Log.e("FolhaDeLoginPorEmail", "Falha ao reenviar a confirmação", e)
                    aviso = mensagemDeErroDeAuth(e, OperacaoDeAuth.OUTRA) to true
                }
            }
        },
        enabled = !carregando && segundosParaReenviar == 0,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Text(if (segundosParaReenviar > 0) "Reenviar e-mail (${segundosParaReenviar}s)" else "Reenviar e-mail")
    }

    // Sair desta conta pendente e voltar ao login (apaga a sessão não confirmada)
    TextButton(
        onClick = {
            authRepository.sair()
            aoUsarOutraConta()
        },
        enabled = !carregando,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Usar outra conta")
    }
}

// ---------------------------------------------------------------------------------------
// Peças comuns
// ---------------------------------------------------------------------------------------

/** Título centralizado de cada etapa. */
@Composable
private fun TituloDaEtapa(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Mensagem de erro geral (vinda do Firebase), em vermelho; não desenha nada se for null. */
@Composable
private fun MensagemDeErro(texto: String?) {
    if (texto != null) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Botão cheio principal da etapa; fica desabilitado e muda o texto enquanto [carregando]. */
@Composable
private fun BotaoPrincipal(
    texto: String,
    textoCarregando: String,
    carregando: Boolean,
    aoClicar: () -> Unit
) {
    Button(
        onClick = aoClicar,
        enabled = !carregando,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(if (carregando) textoCarregando else texto)
    }
}

/**
 * Campo de texto dos formulários, com cantos arredondados, erro embaixo e, nas senhas, um olho
 * para mostrar ou ocultar o texto.
 *
 * @param erro          mensagem de erro do campo (borda e texto vermelhos); null = sem erro.
 * @param dica          texto de ajuda embaixo do campo quando não há erro.
 * @param acaoDoTeclado tecla de ação do teclado: Next vai ao campo de baixo, Done executa [aoAcao].
 * @param ehSenha       esconde o texto e mostra o botão do olho.
 */
@Composable
private fun CampoDeConta(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    erro: String?,
    teclado: KeyboardType = KeyboardType.Text,
    acaoDoTeclado: ImeAction = ImeAction.Next,
    aoAcao: () -> Unit = {},
    ehSenha: Boolean = false,
    capitalizacao: KeyboardCapitalization = KeyboardCapitalization.None,
    habilitado: Boolean = true,
    dica: String? = null
) {
    val foco = LocalFocusManager.current
    var senhaVisivel by remember { mutableStateOf(false) }
    // Erro tem prioridade sobre a dica
    val textoDeApoio = erro ?: dica

    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        singleLine = true,
        enabled = habilitado,
        isError = erro != null,
        supportingText = if (textoDeApoio != null) {
            { Text(textoDeApoio) }
        } else null,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (ehSenha) KeyboardType.Password else teclado,
            imeAction = acaoDoTeclado,
            capitalization = capitalizacao
        ),
        keyboardActions = KeyboardActions(
            onNext = { foco.moveFocus(FocusDirection.Down) },
            onDone = {
                foco.clearFocus()
                aoAcao()
            }
        ),
        visualTransformation = if (ehSenha && !senhaVisivel) PasswordVisualTransformation()
        else VisualTransformation.None,
        trailingIcon = if (ehSenha) {
            {
                IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                    Icon(
                        imageVector = if (senhaVisivel) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                    )
                }
            }
        } else null
    )
}
