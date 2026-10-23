package com.seriando.app.ui.usuario

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import com.seriando.app.model.DataNascimento
import java.util.Calendar

/**
 * Janela com o seletor de data do Material 3, para a data de nascimento (RF59).
 * Não deixa escolher datas futuras nem anteriores a 1900.
 * Esta janela só escolhe: quem grava (e aplica a trava do RN17) é o PerfilRepository.
 *
 * @param dataInicial data já salva ("AAAA-MM-DD") para abrir no dia certo; null se não houver.
 * @param aoConfirmar chamado com a data escolhida no formato "AAAA-MM-DD".
 * @param aoFechar    chamado ao cancelar ou tocar fora da janela.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorDataNascimento(
    dataInicial: String?,
    aoConfirmar: (String) -> Unit,
    aoFechar: () -> Unit
) {
    val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
    val hojeUtc = DataNascimento.hojeEmMillisUtc()

    val estado = rememberDatePickerState(
        // Dados antigos em texto livre devolvem null: o seletor abre sem data marcada
        initialSelectedDateMillis = DataNascimento.paraMillisUtc(dataInicial),
        yearRange = DataNascimento.ANO_MINIMO..anoAtual,
        // Os dias futuros ficam desabilitados
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= hojeUtc
            override fun isSelectableYear(year: Int): Boolean = year in DataNascimento.ANO_MINIMO..anoAtual
        }
    )

    DatePickerDialog(
        onDismissRequest = aoFechar,
        confirmButton = {
            TextButton(
                // Só habilita depois que o usuário escolhe um dia
                enabled = estado.selectedDateMillis != null,
                onClick = { estado.selectedDateMillis?.let { aoConfirmar(DataNascimento.deMillisUtc(it)) } }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = estado)
    }
}