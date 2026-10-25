package com.seriando.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

/**
 * Foto de perfil própria, guardada SÓ neste aparelho (RF30).
 *
 * A foto escolhida na galeria é recortada em quadrado, reduzida a 384x384 e salva como JPEG na
 * pasta interna do app (invisível para outros apps). Nada vai ao Firestore nem ao Storage:
 * os outros usuários continuam vendo o avatar do perfil público. Isto evita moderar imagens
 * de usuários e mantém a foto fora da nuvem (RNF08, LGPD).
 *
 * Há uma única foto, porque o aparelho guarda só os dados de um usuário por vez (RN25). Ela é
 * apagada junto com o banco local, no logout e na exclusão da conta (ver Dependencias).
 */
object FotoDePerfilLocal {

    private const val NOME_DO_ARQUIVO = "foto_perfil.jpg"
    private const val NOME_TEMPORARIO = "foto_perfil.tmp"

    /** Lado da imagem salva, em pixels (o mesmo dos avatares do app). */
    private const val LADO_EM_PIXELS = 384

    private const val QUALIDADE_JPEG = 90

    // Muda sempre que a foto é salva ou removida. Os avatares na tela observam este valor
    // para se redesenharem sozinhos, sem precisar de reiniciar nada.
    private val _versao = MutableStateFlow(0L)
    val versao: StateFlow<Long> = _versao.asStateFlow()

    /** Onde a foto fica: pasta interna (filesDir), que só o próprio app enxerga. */
    private fun arquivo(contexto: Context): File = File(contexto.filesDir, NOME_DO_ARQUIVO)

    /** O arquivo da foto, ou null se o usuário não escolheu nenhuma. */
    fun existente(contexto: Context): File? = arquivo(contexto).takeIf { it.exists() }

    /**
     * Lê a imagem escolhida no Photo Picker, recorta no centro em quadrado, ajusta a rotação
     * (EXIF) e salva reduzida. Roda fora da thread principal.
     *
     * A imagem é lida em duas etapas (primeiro só o tamanho, depois já reduzida) para uma
     * foto grande de câmera não estourar a memória.
     *
     * @param origem endereço (content://) devolvido pelo Photo Picker.
     * @return true se a foto foi salva; false se o arquivo não pôde ser lido ou gravado.
     */
    suspend fun salvarDe(contexto: Context, origem: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = contexto.contentResolver

            // 1) Só as dimensões, sem carregar os pixels
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(origem)?.use { BitmapFactory.decodeStream(it, null, limites) }
            if (limites.outWidth <= 0 || limites.outHeight <= 0) return@withContext false

            // 2) Fator de redução (potência de 2) que ainda deixa o lado menor com pelo menos 384px
            val ladoMenor = min(limites.outWidth, limites.outHeight)
            var amostra = 1
            while (ladoMenor / (amostra * 2) >= LADO_EM_PIXELS) amostra *= 2

            // 3) Carrega a imagem já reduzida
            val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
            val original = resolver.openInputStream(origem)?.use {
                BitmapFactory.decodeStream(it, null, opcoes)
            } ?: return@withContext false

            // 4) Orientação gravada pela câmera (fotos "deitadas" por engano ficam em pé)
            val rotacao = resolver.openInputStream(origem)?.use { lerRotacao(it) } ?: 0

            // 5) Recorte quadrado no centro, escala para 384 e rotação, tudo numa operação só
            val lado = min(original.width, original.height)
            val x = (original.width - lado) / 2
            val y = (original.height - lado) / 2
            val matriz = Matrix().apply {
                setScale(LADO_EM_PIXELS.toFloat() / lado, LADO_EM_PIXELS.toFloat() / lado)
                postRotate(rotacao.toFloat())
            }
            val final = Bitmap.createBitmap(original, x, y, lado, lado, matriz, true)

            // 6) Grava num arquivo temporário e só então renomeia: se algo falhar no meio,
            // a foto anterior continua intacta
            val temporario = File(contexto.filesDir, NOME_TEMPORARIO)
            temporario.outputStream().use { saida ->
                final.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, saida)
            }
            if (final !== original) final.recycle()
            original.recycle()

            val destino = arquivo(contexto)
            if (!temporario.renameTo(destino)) {
                temporario.delete()
                return@withContext false
            }

            _versao.value = System.currentTimeMillis() // avisa as telas
            true
        } catch (e: Exception) {
            // Arquivo inválido, sem permissão, disco cheio...: o chamador avisa o usuário
            Log.e("FotoDePerfilLocal", "Falha ao salvar a foto de perfil", e)
            false
        }
    }

    /** Apaga a foto (se existir) e avisa as telas. Seguro de chamar sem foto salva. */
    fun remover(contexto: Context) {
        arquivo(contexto).delete()
        File(contexto.filesDir, NOME_TEMPORARIO).delete()
        _versao.value = System.currentTimeMillis()
    }

    /** Lê a rotação (0, 90, 180 ou 270 graus) gravada nos metadados EXIF da imagem. */
    private fun lerRotacao(entrada: java.io.InputStream): Int =
        when (ExifInterface(entrada).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
}
