# Seriando — Modelagem do Banco de Dados (Cloud Firestore)

## 1. Por que não há MER/DER tradicional

O Firestore é um banco NoSQL orientado a documentos: não existem tabelas, chaves estrangeiras nem junções (JOIN). Por isso, este documento substitui o MER/DER e os scripts DDL/DML por equivalentes adequados ao modelo:

| Modelo relacional | Equivalente no Seriando (Firestore) |
|---|---|
| DER (diagrama entidade-relacionamento) | Diagrama de coleções e documentos (seção 2) |
| DDL (estrutura das tabelas) | Tabela de campos e tipos de cada documento (seção 3) |
| DML (exemplos de INSERT/SELECT) | Exemplos de leitura e escrita via repositórios Kotlin (seção 4) |

## 2. Diagrama de coleções e documentos

```mermaid
erDiagram
  USUARIOS ||--o{ SERIES_DA_LISTA : contem
  USUARIOS ||--o{ OPINIOES_PRIVADAS : contem
  USUARIOS ||--o{ OPINIOES_OCULTAS : contem
  SERIES_DA_LISTA ||--o{ EPISODIOS_ASSISTIDOS : contem
  AVALIACOES ||--o{ OPINIOES : contem
  OPINIOES ||--o{ VOTOS : recebe
  OPINIOES ||--o{ DENUNCIAS : "pode gerar"
  PERFIS_PUBLICOS ||--|| USUARIOS : "corresponde a"

  USUARIOS {
    string uid PK
    string email
    string aniversario "AAAA-MM-DD"
    boolean aniversarioAlterado
  }
  PERFIS_PUBLICOS {
    string uid PK
    string apelido
    string avatarId
    long atualizadoEm
  }
  SERIES_DA_LISTA {
    string serieId PK
    string nome
    string sinopse
    string caminhoCapa
    string tipoMidia
    string status
    long dataAdicao
    long atualizadoEm
  }
  EPISODIOS_ASSISTIDOS {
    string chave PK "temporada_numero"
    int temporada
    int numero
    long atualizadoEm
  }
  AVALIACOES {
    string idObra PK "tipo_tmdbId"
  }
  OPINIOES {
    string autorUid PK
    string apelidoAutor
    string avatarAutor
    int nota
    string texto
    boolean spoiler
    array emocoes
    boolean publica
    long data
  }
  OPINIOES_PRIVADAS {
    string idObra PK "tipo_tmdbId"
    string texto
    boolean spoiler
    array emocoes
    long data
  }
  OPINIOES_OCULTAS {
    string id PK "tipo_tmdbId_autorUid"
    string obraKey "tipo_tmdbId"
    string autorUid
    long ocultadaEm
  }
  VOTOS {
    string votanteUid PK
    int valor "1 ou -1"
  }
  DENUNCIAS {
    string id PK
    string autorOpiniaoUid
    string tipoMidia
    int tmdbId
    string denuncianteUid
    string motivo
    long data
  }
```

## 3. Estrutura de cada coleção (equivalente ao DDL)

### 3.1 `usuarios/{uid}` — dados privados

| Campo | Tipo | Observações |
|---|---|---|
| `email` | string | Vindo da conta Google ou do cadastro com e-mail e senha (RF32), no primeiro login confirmado. A senha nunca é gravada aqui: fica só no Firebase Authentication |
| `aniversario` | string ou null | Data de nascimento completa no formato `AAAA-MM-DD` (RF59). Usada só para calcular a idade (RN13) e para o controle de idade (Bloco K); a idade em si não é guardada. Valores antigos em texto livre (como "15/03") são tratados como data inválida e o usuário vira menor até informar a data (RN16) |
| `aniversarioAlterado` | boolean | `true` depois que a data foi corrigida pela única vez permitida (RN17). As regras do Firestore recusam uma nova alteração da data quando este campo já é `true` |

A data pode ser definida uma vez e corrigida uma única vez. Depois disso, as regras de segurança impedem qualquer mudança (ver seção 5).

Subcoleções: `usuarios/{uid}/series/{serieId}` (3.3) `usuarios/{uid}/opinioesPrivadas/{tipoMidia_tmdbId}` (3.6) e `usuarios/{uid}/opinioesOcultas/{tipoMidia_tmdbId_autorUid}` (3.7).

### 3.2 `perfisPublicos/{uid}` — dados públicos

| Campo | Tipo | Observações |
|---|---|---|
| `apelido` | string | Sugerido do primeiro nome da conta Google, ou digitado no cadastro por e-mail e senha (até 40 caracteres); editável |
| `avatarId` | string | `"google"` (usa a foto da conta) ou `"foto_01"` a `"foto_15"` (uma das 15 imagens de avatar do app, RF78). A foto própria do aparelho (RF30) não muda esse campo: ela existe só no aparelho do dono e os outros veem o avatar gravado aqui. Valores antigos em emoji são aceitos pelas regras, mas o app mostra a inicial do apelido no lugar deles |
| `atualizadoEm` | long | Instante da última alteração do perfil, em milissegundos; vale para o perfil inteiro (inclui a data de nascimento de `usuarios/{uid}`) e resolve conflitos pela gravação mais recente (RN23) |

### 3.3 `usuarios/{uid}/series/{serieId}` — lista pessoal

| Campo | Tipo | Observações |
|---|---|---|
| `id` | int | ID da obra na TMDB |
| `nome` | string | |
| `sinopse` | string | |
| `caminhoCapa` | string ou null | Caminho relativo da capa na TMDB |
| `tipoMidia` | string | `"tv"` ou `"movie"` |
| `status` | string | Nome do enum `StatusSerie` ou `StatusFilme`, conforme `tipoMidia` |
| `dataAdicao` | long | Timestamp em milissegundos |
| `atualizadoEm` | long | Instante da última alteração, em milissegundos (RN23). Documentos antigos não têm o campo e valem 0 |

Subcoleção: `usuarios/{uid}/series/{serieId}/episodiosAssistidos/{temporada_numero}`

| Campo | Tipo | Observações |
|---|---|---|
| `temporada` | int | |
| `numero` | int | Número do episódio dentro da temporada |
| `dataAssistido` | long | Timestamp em milissegundos |
| `atualizadoEm` | long | Instante da marcação, em milissegundos (RN23). O app grava `temporada`, `numero` e `atualizadoEm`; documentos antigos podem ter `dataAssistido` |

### 3.4 `avaliacoes/{tipoMidia_tmdbId}/opinioes/{autorUid}` — avaliação e opinião

Um único documento por usuário por obra (RN01) reúne nota e opinião pública.

| Campo | Tipo | Observações |
|---|---|---|
| `autorUid` | string | |
| `apelidoAutor` | string | Denormalizado, evita buscar o perfil do autor a cada listagem |
| `avatarAutor` | string | Idem |
| `nota` | int | 1 a 5 (RN02); 0 = sem nota |
| `texto` | string | Até 500 caracteres (RN03) |
| `spoiler` | boolean | |
| `emocoes` | array\<string\> | Até 6 ids de `EMOCOES_DISPONIVEIS` (RN10) |
| `publica` | boolean | `true` neste documento; a opinião privada fica em `usuarios/{uid}/opinioesPrivadas` (3.6) |
| `data` | long | Timestamp em milissegundos |

Subcoleção: `avaliacoes/{tipoMidia_tmdbId}/opinioes/{autorUid}/votos/{votanteUid}`

| Campo | Tipo | Observações |
|---|---|---|
| `valor` | int | `1` (curtir) ou `-1` (descurtir), RN05 |

### 3.5 `denuncias/{id}` — moderação

| Campo | Tipo | Observações |
|---|---|---|
| `autorOpiniaoUid` | string | Quem escreveu a opinião denunciada |
| `tipoMidia` | string | |
| `tmdbId` | int | |
| `denuncianteUid` | string | Quem denunciou |
| `motivo` | string | Um dos valores fixos de `MOTIVOS_DENUNCIA` |
| `data` | long | Timestamp em milissegundos |

Leitura restrita a um futuro painel de moderação — o app cliente nunca lê esta coleção, só grava (RNF03). Ao denunciar, a denúncia é gravada no mesmo lote da marca de ocultação descrita em 3.7 (RF68).

### 3.6 `usuarios/{uid}/opinioesPrivadas/{tipoMidia_tmdbId}` — opinião privada

Guarda a opinião marcada como privada (RF48, RN12), fora de `avaliacoes/`, para que as regras do Firestore a mostrem só ao dono. A nota em estrelas fica sempre no documento público.

| Campo | Tipo | Observações |
|---|---|---|
| `autorUid` | string | Sempre o dono do documento |
| `apelidoAutor`, `avatarAutor` | string | Cópia do perfil no momento da gravação |
| `texto` | string | Até 500 caracteres (RN03) |
| `spoiler` | boolean | |
| `emocoes` | array\<string\> | Até 6 (RN10) |
| `publica` | boolean | Sempre `false` neste documento |
| `data` | long | Timestamp em milissegundos |

### 3.7 `usuarios/{uid}/opinioesOcultas/{tipoMidia_tmdbId_autorUid}` — opiniões ocultadas por quem denunciou

Guarda, na conta de quem denunciou, a marca de que a opinião de um autor, numa obra, deve ficar oculta para ele (RF68, RN26). O id do documento combina obra e autor, o que impede duplicar a marca: denunciar de novo apenas regrava o mesmo documento. Os demais usuários continuam vendo a opinião.

| Campo | Tipo | Observações |
|---|---|---|
| `obraKey` | string | Obra onde a opinião está, no formato `tipo_tmdbId` (ex.: `tv_1399`); usada para buscar só as marcas da obra aberta |
| `autorUid` | string | Autor da opinião ocultada; nunca o próprio dono do documento |
| `ocultadaEm` | long | Timestamp em milissegundos |

Só o dono lê e escreve (RNF03). É gravada no mesmo lote da denúncia (seção 3.5), é lida ao montar a lista de opiniões de uma obra e é apagada na exclusão de conta (RF29). Não existe cópia no Room: a comunidade só funciona com internet (RN24).

## 4. Exemplos de leitura e escrita (equivalente ao DML)

> Desde o Bloco N (modo offline), as telas da lista, do progresso e do perfil não chamam mais o Firestore diretamente: leem e gravam no banco local (seção 6) e o `Sincronizador` envia ao Firestore. Os exemplos abaixo mostram a forma original de acesso ao Firestore, que continua valendo para a comunidade (avaliações, votos e denúncias) e é o que o `Sincronizador` executa por baixo.

Escrita (equivalente a um INSERT/UPDATE):

```kotlin
// MinhaListaRepository.kt
suspend fun adicionarSerie(uid: String, serie: SerieSalva) {
    db.collection("usuarios").document(uid)
        .collection("series").document(serie.id.toString())
        .set(serie)
        .await()
}
```

Leitura paginada (equivalente a um SELECT ... ORDER BY data DESC LIMIT 20 a partir de um ponto). Desde a etapa de economia de requisições (RNF19, RN40), o app não lê mais todas as opiniões da obra de uma vez; descarta, no aparelho, o que não deve aparecer (só nota, opiniões ocultadas, a do próprio usuário):

```kotlin
// AvaliacaoRepository.kt (trecho simplificado de buscarMaisOpinioes)
var consulta = colecaoOpinioes(tipoMidia, tmdbId)
    .orderBy("data", Query.Direction.DESCENDING)
    .limit(20)                                  // 20 documentos por consulta
if (cursor != null) consulta = consulta.startAfter(cursor)   // continua da data da última opinião lida
val documentos = consulta.get().await().documents
```

Agregação calculada pelo servidor (equivalente a SELECT COUNT(*), AVG(nota) ... WHERE nota > 0), sem baixar os documentos (RN40):

```kotlin
// AvaliacaoRepository.kt (trecho de buscarMediaDaComunidade)
val resultado = colecaoOpinioes(tipoMidia, tmdbId)
    .whereGreaterThan("nota", 0)
    .aggregate(AggregateField.count(), AggregateField.average("nota"))
    .get(AggregateSource.SERVER)
    .await()
```

Escrita em lote, tudo ou nada (equivalente a uma transação com dois INSERT), usada ao denunciar uma opinião (RF28 e RF68):

```kotlin
// DenunciaRepository.kt (trecho)
val lote = db.batch()
lote.set(db.collection("denuncias").document(), denuncia)   // registro para moderação
lote.set(
    db.collection("usuarios").document(denuncianteUid)
        .collection("opinioesOcultas").document("${chaveObra}_$autorOpiniaoUid"),
    mapOf("obraKey" to chaveObra, "autorUid" to autorOpiniaoUid, "ocultadaEm" to System.currentTimeMillis())
)
lote.commit().await()   // ou as duas gravações acontecem, ou nenhuma
```

Atualização de um campo específico (equivalente a um UPDATE parcial):

```kotlin
// MinhaListaRepository.kt
suspend fun atualizarStatusTexto(uid: String, serieId: Int, nomeStatus: String) {
    db.collection("usuarios").document(uid)
        .collection("series").document(serieId.toString())
        .update("status", nomeStatus)
        .await()
}
```

Exemplo ilustrativo da gravação da data de nascimento com a marca da correção única (RN17); a implementação real está no envio do perfil do `Sincronizador`:

```kotlin
// Exemplo ilustrativo (Sincronizador, envio do perfil)
db.collection("usuarios").document(uid)
    .set(
        mapOf(
            "aniversario" to dataAAAAMMDD,          // texto no formato AAAA-MM-DD
            "aniversarioAlterado" to jaFoiCorrigida  // true depois da única correção
        ),
        SetOptions.merge()
    )
    .await()
```

Remoção (equivalente a um DELETE):

```kotlin
// MinhaListaRepository.kt
suspend fun removerSerie(uid: String, serieId: Int) {
    db.collection("usuarios").document(uid)
        .collection("series").document(serieId.toString())
        .delete()
        .await()
}
```

## 5. Observação sobre regras de segurança

As regras de segurança definitivas estão publicadas no Firebase e versionadas no arquivo `firestore.rules` do repositório: cada usuário só lê e escreve a própria subárvore, as notas e opiniões públicas são legíveis por qualquer usuário autenticado, as opiniões privadas só pelo dono, cada voto só pelo próprio votante (e nunca na própria opinião) e a coleção `denuncias` é somente-escrita para o app. Os campos de cada documento são validados (tipos, tamanhos e valores permitidos).

Em `usuarios/{uid}`, as regras também validam a data de nascimento e a trava de correção (RN17): a data só pode ser corrigida uma vez, controlada pelo campo `aniversarioAlterado`; depois da correção, a data não muda mais. Confira o texto exato no arquivo `firestore.rules`. Essa trava protege o controle de idade contra troca repetida da data, mas não contra uma data falsa informada de início (a data não é verificada). O modelo de ameaças (STRIDE) detalha as limitações conhecidas.

A subcoleção `opinioesOcultas` (RF68) só é legível e gravável pelo dono; as regras validam os três campos e exigem que o id do documento seja exatamente `obraKey_autorUid` e que o autor ocultado não seja o próprio dono.

## 6. Banco local (Room) — modo offline

O Room (SQLite no aparelho) espelha os dados pessoais do usuário logado e é de onde as telas leem (RN21). O Firestore não ganha coleções por causa dele: o `Sincronizador` converte as linhas locais nos documentos descritos na seção 3 e vice-versa. O arquivo do banco é `seriando.db`, na versão 1 e sem migração: mudanças de schema exigem desinstalar o app no desenvolvimento, e uma migração será obrigatória antes da publicação.

```mermaid
erDiagram
  LISTA ||--o{ EPISODIOS_ASSISTIDOS : "serieId = id"
  LISTA ||--o{ OBRAS_CACHE : "tipoMidia + tmdbId"
  PERFIL

  LISTA {
    int id PK "id da TMDB"
    string nome
    string sinopse
    string caminhoCapa
    string tipoMidia "tv ou movie"
    string status
    long dataAdicao
    long atualizadoEm
    boolean pendenteSincronizacao
    boolean removido
  }
  EPISODIOS_ASSISTIDOS {
    int serieId PK
    int temporada PK
    int numero PK
    long atualizadoEm
    boolean pendenteSincronizacao
    boolean removido
  }
  PERFIL {
    string uid PK
    string apelido
    string avatarId
    string aniversario "AAAA-MM-DD"
    boolean aniversarioCorrigido
    long atualizadoEm
    boolean pendenteSincronizacao
  }
  OBRAS_CACHE {
    string tipoMidia PK
    int tmdbId PK
    int parte PK "-1 detalhes, -2 onde assistir, 0 ou mais temporada"
    string json
    long atualizadoEm
  }
```

### 6.1 Tabelas e correspondência com o Firestore

| Tabela local | Documento(s) no Firestore | Observações |
|---|---|---|
| `lista` | `usuarios/{uid}/series/{id}` | A chave é o id da TMDB, igual ao id do documento (limitação conhecida: um filme e uma série com o mesmo id da TMDB se sobrescreveriam) |
| `episodiosAssistidos` | `usuarios/{uid}/series/{id}/episodiosAssistidos/{temporada_numero}` | |
| `perfil` | `perfisPublicos/{uid}` (apelido, avatar, `atualizadoEm`) e `usuarios/{uid}` (data de nascimento, `aniversarioAlterado` e e-mail) | Uma linha por usuário. `aniversarioCorrigido` é o equivalente local de `aniversarioAlterado`. O e-mail não é guardado localmente (coleta mínima, RNF08): vem do login |
| `obrasCache` | — (só existe no aparelho) | Cópia das respostas da TMDB das obras da lista, em JSON, para consultar sem internet (RNF13) e como cache com validade: detalhes e temporadas 6 horas, "onde assistir" 24 horas (RN39). Nunca vai ao Firestore |

Dados que ficam fora do Room e do Firestore, só no aparelho: a indicação de "introdução já vista" (DataStore, RN30) e o histórico de busca (SharedPreferences, RN29), as datas de conferência dos episódios (SharedPreferences `estado_sincronizacao`, RN41) e o cache HTTP das listas da TMDB (pasta de cache do app, 10 MB, RN39). A classificação indicativa das obras é consultada na TMDB e guardada apenas em memória durante o uso (controle de idade); não vai para nenhum dos dois bancos.

### 6.2 Campos de controle da sincronização

- `pendenteSincronizacao`: `true` enquanto a alteração não foi confirmada na nuvem (RN22).
- `atualizadoEm`: instante da última alteração, em milissegundos; a gravação mais recente vence (RN23).
- `removido`: lápide. Remover sem conexão marca a linha como removida e pendente; ela some da tela, e só é apagada depois de a exclusão chegar à nuvem.
- `aniversarioCorrigido`: `true` depois da correção única da data de nascimento (RN17).
- Confirmar o envio só limpa a marca de pendente se o `atualizadoEm` ainda for o mesmo que foi enviado; assim, uma alteração feita durante o envio não se perde.
- O recebimento dos episódios usa o `atualizadoEm` dos documentos da nuvem para ler só o que mudou desde a última conferência (com margem de 1 hora para diferenças de relógio) e confere o total da nuvem com o total local por uma consulta de agregação; se divergirem, lê todos os episódios da série. A data da última conferência fica em SharedPreferences, não no Room (RN41).

### 6.3 Exemplos de consulta (DAOs do Room)

```kotlin
// ListaDao: a lista que a tela mostra, sem os itens marcados como removidos (Flow: a tela atualiza sozinha)
@Query("SELECT * FROM lista WHERE removido = 0 ORDER BY dataAdicao DESC")
fun observarItens(): Flow<List<ListaEntity>>

// ListaDao: remove da tela na hora e deixa a lápide para o envio à nuvem
@Query("UPDATE lista SET removido = 1, pendenteSincronizacao = 1, atualizadoEm = :agora WHERE id = :id")
suspend fun marcarRemovido(id: Int, agora: Long)

// ListaDao: confirma o envio só se o item não mudou desde então
@Query("UPDATE lista SET pendenteSincronizacao = 0 WHERE id = :id AND atualizadoEm = :atualizadoEm")
suspend fun marcarSincronizado(id: Int, atualizadoEm: Long)

// ObraCacheDao: apaga a cópia das obras que não estão mais na lista
@Query("DELETE FROM obrasCache WHERE NOT EXISTS (SELECT 1 FROM lista WHERE lista.id = obrasCache.tmdbId AND lista.tipoMidia = obrasCache.tipoMidia AND lista.removido = 0)")
suspend fun apagarForaDaLista()
```

### 6.4 Ciclo de vida do banco local (RN25)

O banco guarda dados apenas do usuário logado: é apagado por completo no logout e na exclusão de conta, junto com as datas de conferência dos episódios (`estado_sincronizacao`). A foto de perfil própria (RF30) não está no Room nem no Firestore: é o arquivo `foto_perfil.jpg` na pasta interna do app, apagado junto com o banco (RN37). O app também registra de quem é o banco (preferência `dono_do_banco_local`) e o apaga no login se encontrar dados de outro usuário. Ao entrar, sair ou receber da nuvem uma data de nascimento diferente, a `VersaoDaIdade` é atualizada e as listas em memória são recarregadas (RN32).
