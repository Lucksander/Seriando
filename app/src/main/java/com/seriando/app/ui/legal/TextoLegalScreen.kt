package com.seriando.app.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tela que mostra um documento legal (Termos de Uso ou Política de Privacidade).
 * É a mesma tela para os dois: muda só o [documento] recebido.
 * Funciona sem internet e para visitante, porque o texto está no código ([TextosLegais.kt]).
 *
 * @param documento documento a exibir.
 * @param aoVoltar  chamada ao tocar na seta da barra superior; quem navega é o SeriandoNavHost.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextoLegalScreen(
    documento: DocumentoLegal,
    aoVoltar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(documento.titulo) },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingInterno ->
        // Documento longo: LazyColumn desenha só o que está na tela
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Última atualização: ${documento.atualizadoEm}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(documento.blocos) { bloco ->
                when (bloco) {
                    is BlocoLegal.Secao -> Text(
                        text = bloco.titulo,
                        // Espaço extra acima separa uma seção da anterior
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    is BlocoLegal.Paragrafo -> Text(
                        text = bloco.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    is BlocoLegal.Lista -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        bloco.itens.forEach { item ->
                            Row {
                                Text(text = "•", modifier = Modifier.padding(end = 8.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    is BlocoLegal.Tabela -> TabelaEmCartoes(bloco)
                }
            }
        }
    }
}

/**
 * Desenha uma tabela do documento como uma pilha de cartões, porque tabela larga não cabe
 * na tela do celular. Cada linha vira um cartão: a primeira coluna é o título e as outras
 * aparecem como "Cabeçalho: valor".
 */
@Composable
private fun TabelaEmCartoes(tabela: BlocoLegal.Tabela) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tabela.linhas.forEach { celulas ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Primeira coluna: o assunto da linha
                    Text(
                        text = celulas.firstOrNull().orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Demais colunas: "Cabeçalho: valor", com o cabeçalho em destaque
                    for (indice in 1 until celulas.size) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    append(tabela.cabecalhos.getOrElse(indice) { "" })
                                    append(": ")
                                }
                                append(celulas[indice])
                            },
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
