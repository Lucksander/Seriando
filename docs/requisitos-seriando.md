# Seriando — Levantamento de Requisitos (revisão)

Aplicativo Android para acompanhar séries e filmes, com lista pessoal, progresso de episódios e uma comunidade de opiniões.

**Legenda de situação:** Concluído | A implementar | Opcional (se sobrar tempo)

---

## 1. Requisitos Funcionais

### 1.1 Já implementados

| ID | Requisito | Situação |
|----|-----------|----------|
| RF01 | Autenticar com conta Google | Concluído |
| RF02 | Identificar o usuário autenticado e associar lista e progresso à conta | Concluído |
| RF03 | Pesquisar séries e filmes | Concluído |
| RF04 | Consultar dados na API da TMDB | Concluído |
| RF05 | Visualizar detalhes (sinopse, capa, elenco, trailer, nota da TMDB) | Concluído |
| RF06 | Visualizar temporadas e episódios | Concluído |
| RF07 | Adicionar e remover itens da minha lista | Concluído |
| RF08 | Definir status de acompanhamento (séries e filmes têm conjuntos diferentes) | Concluído |
| RF09 | Marcar episódios e temporadas como assistidos | Concluído |
| RF10 | Acompanhar o progresso da série | Concluído |
| RF11 | Ver destaques na Home e navegar por abas | Concluído |
| RF12 | Aba Conta (antes chamada Usuário): exibir apelido, foto e e-mail | Concluído (aba renomeada no app: RF58) |
| RF13 | Editar apelido | Concluído |
| RF14 | Informar data de aniversário (opcional, visível apenas ao próprio usuário) | Concluído |
| RF15 | Foto de perfil: usar a foto da conta Google ou escolher um avatar do app | Concluído |
| RF16 | Avaliar filme ou série com 1 a 5 estrelas | Concluído |
| RF17 | Editar e remover a própria avaliação | Concluído |
| RF18 | Ver a média das avaliações da comunidade | Concluído |
| RF19 | Escrever uma opinião sobre filme ou série (com limite de caracteres) | Concluído |
| RF20 | Marcar a opinião como contendo spoiler (oculta até o leitor tocar para revelar) | Concluído |
| RF21 | Ver as opiniões de outros usuários sobre a obra | Concluído |
| RF22 | Editar e remover a própria opinião | Concluído |
| RF23 | Curtir uma opinião | Concluído |
| RF24 | Descurtir uma opinião | Concluído |
| RF25 | Remover o próprio voto (tocar novamente desfaz) | Concluído |
| RF26 | Ver a quantidade de curtidas e descurtidas de cada opinião | Concluído |
| RF27 | Sair da conta (logout), limpando o cache local | Concluído |
| RF28 | Denunciar uma opinião (grava a denúncia em coleção de acesso restrito) | Concluído |
| RF34 | Exibir, nos detalhes de séries e filmes, em quais serviços de streaming a obra está disponível no Brasil (assinatura, aluguel e compra), com link para a página da TMDB | Concluído |
| RF35 | Ao escrever a opinião, marcar até 6 emoções de como se sentiu assistindo (conjunto fixo do app) | Concluído |
| RF36 | Cada emoção exibida com um indicador colorido (bolinha) ao lado da opinião, agrupadas por família de cor, quebrando em várias linhas quando necessário | Concluído |
| RF40 | Tocar em um nome do elenco, na tela de Detalhes, abre a página da pessoa | Concluído |
| RF41 | Exibir foto, nome e biografia da pessoa (dados da TMDB) | Concluído |
| RF42 | Exibir a filmografia: séries e filmes em que a pessoa trabalhou | Concluído |
| RF43 | Tocar em um título da filmografia abre a tela de Detalhes daquela obra | Concluído |
| RF47 | Exibir a imagem (thumbnail) de cada episódio na lista de episódios da tela de Detalhes | Concluído |
| RF48 | Marcar a própria opinião como pública (visível a todos) ou privada (visível só ao autor) | Concluído |
| RF44 | Organizar a Home em categorias por gênero (ação, suspense, comédia etc.), cada uma como uma fileira com rolagem horizontal | Concluído |
| RF45 | Incluir uma categoria "Em alta" com os títulos mais populares do momento | Concluído |
| RF46 | Ao rolar uma categoria até o fim, mostrar um botão "Ver mais" que leva a uma lista completa só daquela categoria | Concluído |
| RF29 | Excluir a conta e todos os dados do usuário (LGPD e Play Store) | Concluído |
| RF55 | Tocar no nome de uma categoria de gênero na Home abre a lista completa daquela categoria (mesmo destino do "Ver mais"), com uma seta ao lado do título indicando que ele é tocável | Concluído |
| RF56 | Exibir o "Ver mais" do fim da fileira como um card do tamanho das capas, com seta dentro de um círculo e o texto "Ver mais" | Concluído |
| RF57 | Exibir os thumbnails dos episódios maiores e em rolagem horizontal na tela de Detalhes | Concluído |
| RF58 | Renomear a aba Usuário para "Conta" na navegação | Concluído |

### 1.2 A implementar (escopo atual)

Os blocos A a J e o bloco M foram concluídos. Faltam os blocos K, L e N.

**Bloco K — Controle de idade (classificação indicativa)**

| ID | Requisito | Situação |
|----|-----------|----------|
| RF49 | Exibir a classificação indicativa do Brasil (L, 10, 12, 14, 16, 18) nos detalhes da obra | A implementar |
| RF50 | Ocultar as obras acima da idade do usuário na Home, na busca, nas categorias e na filmografia da pessoa | A implementar |
| RF51 | Bloquear a tela de Detalhes de uma obra acima da idade do usuário, mesmo quando acessada por outro caminho | A implementar |
| RF59 | Informar a data de nascimento completa (dia, mês e ano) com seletor de data na aba Conta, necessária para calcular a idade; substitui o aniversário em texto livre do RF14 | A implementar |

**Bloco L — Introdução e modo visitante**

| ID | Requisito | Situação |
|----|-----------|----------|
| RF52 | Exibir telas de introdução na primeira abertura do app (ícone do Seriando, "seu cantinho para registrar e organizar suas séries no seu ritmo" e "descubra em que streaming sua série ou filme está"), mostradas apenas uma vez | A implementar |
| RF53 | Exibir, depois da introdução, uma tela de entrada com o botão de login com Google e a opção de entrar sem fazer login | A implementar |
| RF54 | Modo visitante: permitir pesquisar, ver detalhes, onde assistir, elenco e página da pessoa; ao tentar adicionar à lista, avaliar, opinar, votar ou ver a comunidade, pedir o login | A implementar |

**Bloco N — Modo offline (offline-first)**

Promovido de opcional para obrigatório (antes RF33), por orientação do professor: a persistência local é uma das competências centrais da disciplina.

| ID | Requisito | Situação |
|----|-----------|----------|
| RF33 | Modo offline com persistência local (Room): a lista pessoal, os status, o progresso de episódios, o perfil do próprio usuário e os dados das obras da lista ficam salvos no aparelho; o app abre, permite consultar a lista e marcar episódios sem internet e sincroniza com o Firestore quando a conexão voltar | A implementar |
| RF62 | Avisar o usuário quando o app estiver sem conexão e indicar que há alterações ainda não sincronizadas | A implementar |

### 1.3 Opcionais (se sobrar tempo, por ordem de prioridade)

| ID | Requisito | Situação |
|----|-----------|----------|
| RF30 | Enviar foto de perfil própria (Photo Picker + Firebase Storage) | Opcional |
| RF31 | Avaliar e opinar por episódio | Opcional |
| RF32 | Login próprio com e-mail e senha (Firebase Authentication) | Opcional |
| RF60 | Versão iOS do aplicativo em Compose Multiplatform, reaproveitando o código compartilhado (ui, ViewModels, Repository e model) | Opcional |
| RF61 | Publicação na Apple App Store: distribuição de testes pelo TestFlight e envio para revisão | Opcional |

RF60 e RF61 dependem dos pré-requisitos descritos na seção 1.5. O RF32 é um deles quando não houver Sign in with Apple.

### 1.4 Sugestões para versões futuras (fora do escopo atual)

| Sugestão | Origem |
|----------|--------|
| Recomendação de séries e filmes parecidos com os já assistidos | Apontada com destaque na pergunta aberta da pesquisa de viabilidade (ver estudo-viabilidade.md) |

### 1.5 Por que o aplicativo para iOS não está no escopo atual

O Seriando precisa ser multiplataforma, e a versão iOS (RF60 e RF61) está registrada como requisito opcional. Ela não foi implementada nesta entrega pelos motivos abaixo, e o projeto fica com a base preparada para ela (RNF11).

1. **Prazo.** A entrega é em novembro. A prioridade é fechar os requisitos obrigatórios (Blocos K, L e N), a documentação final e os testes.
2. **Ponto de partida Android.** O app nasceu como projeto Android nativo, com Jetpack Compose, Retrofit, Credential Manager e DataStore. Esse código de cliente não roda no iOS sem migrar para uma stack multiplataforma (por exemplo, Ktor no lugar do Retrofit, biblioteca multiplataforma para o Firebase e um login com Google específico para cada plataforma). Fazer isso agora consumiria o tempo dos requisitos obrigatórios sem entregar funcionalidade nova.
3. **Exigências da plataforma Apple.** Publicar no iOS exige um Mac com Xcode, uma conta Apple Developer (paga, anual) e testes em simulador ou aparelho iOS.
4. **Exigências da App Store que o app ainda não cumpre.** Por oferecer login de terceiros, é preciso oferecer um login equivalente (Sign in with Apple ou e-mail e senha, RF32). Por conter conteúdo gerado por usuários, a Apple também pede um mecanismo de bloqueio de usuários, que não existe nos requisitos atuais. O app já cumpre a exclusão de conta dentro do app (RF29) e a denúncia de conteúdo (RF28).
5. **Decisão.** Em vez de migrar agora, o projeto adota a preparação mínima do RNF11, para que a versão iOS seja uma evolução futura sem grande retrabalho.

**Pré-requisitos para a versão iOS (evolução futura):**
- Migrar o projeto para Compose Multiplatform (módulo compartilhado com commonMain, androidMain e iosMain).
- Trocar Retrofit por Ktor e adotar kotlinx.serialization.
- Acessar o Firebase por biblioteca multiplataforma ou por interfaces com implementação em cada plataforma.
- Implementar o login com Google no iOS e um login equivalente (Sign in with Apple ou RF32).
- Criar o bloqueio de usuários.
- Registrar o app no Firebase para iOS (arquivo de configuração próprio).

---

## 2. Requisitos Não Funcionais

| ID | Requisito | Categoria |
|----|-----------|-----------|
| RNF01 | Regras de segurança do Firestore por UID, com validação dos campos gravados | Segurança |
| RNF02 | Chave da API da TMDB fora do código-fonte e do repositório | Segurança |
| RNF03 | Dados públicos (apelido, foto, opinião pública) separados dos privados (aniversário, e-mail, opinião privada) | Privacidade |
| RNF04 | Um único voto por usuário por opinião, garantido pela estrutura e pelas regras do banco | Integridade |
| RNF05 | Comunicação exclusivamente via HTTPS | Segurança |
| RNF06 | Tratamento de erros e estados de carregamento em todas as telas | Usabilidade |
| RNF07 | Código limpo e comentado | Manutenibilidade |
| RNF08 | Coleta mínima de dados pessoais, com finalidade documentada | Privacidade (LGPD) |
| RNF09 | Atribuir a JustWatch como fonte dos dados de streaming, de forma visível na tela (exigência dos termos da TMDB) | Conformidade legal |
| RNF10 | Capas das obras com bordas arredondadas, de forma consistente em todas as telas | Usabilidade (identidade visual) |
| RNF11 | Código preparado para multiplataforma: sem Context do Android em ViewModels e Repositories; acesso ao Firebase, à TMDB e ao armazenamento local sempre por trás de interfaces na camada de dados (Repository); código específico de Android (Credential Manager, DataStore, Room) isolado em poucos arquivos | Portabilidade / Manutenibilidade |
| RNF12 | Esteira de publicação documentada para as duas lojas: Google Play (obrigatória) e Apple App Store (preparação documentada, execução opcional, RF61) | Distribuição |

---

## 3. Regras de Negócio

| ID | Regra |
|----|-------|
| RN01 | Cada usuário possui no máximo uma avaliação e uma opinião por obra |
| RN02 | A nota é um número inteiro de 1 a 5 |
| RN03 | O texto da opinião tem tamanho máximo de 500 caracteres |
| RN04 | O usuário não pode votar na própria opinião |
| RN05 | O voto vale +1 (curtir) ou -1 (descurtir) |
| RN06 | Somente o autor pode editar ou remover sua opinião, avaliação e voto |
| RN07 | Opiniões marcadas como spoiler são exibidas ocultas até o usuário tocar para revelar |
| RN08 | Somente usuários autenticados leem e escrevem na comunidade |
| RN09 | A região consultada para disponibilidade de streaming é fixa: Brasil (BR). Quando não houver dados, o app exibe "Sem informação de streaming no Brasil" |
| RN10 | O usuário escolhe no máximo 6 emoções ao escrever a própria opinião, de uma lista fixa de 24 emoções definida pelo app |
| RN11 | As emoções não são uma interação de terceiros — só o autor da opinião as escolhe, no momento em que escreve ou edita o texto |
| RN12 | Uma opinião privada aparece apenas para o próprio autor e é gravada em usuarios/{uid}/opinioesPrivadas, fora da coleção pública; removê-la apaga texto, spoiler, emoções e a visibilidade, preservando a nota em estrelas (que tem sua própria remoção, RF17) |
| RN13 | A idade do usuário é calculada a partir da data de nascimento (RF14 e RF59) no momento do uso; a idade em si não é guardada |
| RN14 | A classificação indicativa considerada é a do Brasil (BR); obra sem classificação para o Brasil fica oculta para usuários menores de 18 anos |
| RN15 | O filtro por idade é aplicado em um único ponto da camada de dados (Repository), valendo para todas as telas |
| RN16 | Usuário sem data de nascimento informada e visitante são tratados como menores, com uma faixa de classificação restrita: até 12 anos (classificações L, 10 e 12) |
| RN17 | Depois de preenchida, a data de nascimento não pode ser alterada livremente (travada nas regras do Firestore, com no máximo uma correção) |
| RN18 | O visitante não grava dados no Firestore e não lê a comunidade (RN08 continua valendo); ações que exigem conta pedem o login |
| RN19 | Ao excluir a conta (RF29), são apagados o perfil, a lista, o progresso, as avaliações, as opiniões (públicas e privadas) e os votos do usuário; as denúncias feitas por ele são mantidas como registro de moderação, e os votos de outras pessoas nas opiniões dele ficam órfãos e invisíveis, porque só cada autor pode apagar o próprio voto |
| RN20 | Remover um item da lista apaga também o progresso de episódios dele (o usuário é avisado antes); marcar um episódio de uma série que não está na lista a adiciona automaticamente com o status "Assistindo" |
| RN21 | A interface lê sempre do banco local (Room); a sincronização com o Firestore atualiza o banco local em segundo plano |
| RN22 | Alterações feitas sem conexão (lista, status, progresso de episódios) ficam registradas no aparelho como pendentes e são enviadas ao Firestore quando a conexão voltar |
| RN23 | Em conflito entre o dado local e o da nuvem, vale a gravação mais recente; limitação conhecida: a data da gravação vem do relógio do celular |
| RN24 | A comunidade (opiniões, avaliações, votos e médias) e o login exigem conexão, e RN08 continua valendo; sem conexão, o app avisa e mantém disponível o que está salvo no aparelho |
| RN25 | O banco local guarda dados apenas do usuário logado e é apagado por completo no logout (RF27) e na exclusão de conta (RF29) |

---

## 4. Modelo de Dados (Firestore)

```
usuarios/{uid}                                  -> dados privados (aniversário, e-mail)
perfisPublicos/{uid}                            -> apelido, avatar
usuarios/{uid}/series/{serieId}                 -> lista pessoal e status
usuarios/{uid}/series/{serieId}/episodiosAssistidos
avaliacoes/{tipo_tmdbId}/opinioes/{autorUid}    -> nota e opinião pública: autorUid, apelidoAutor, avatarAutor, nota, texto, spoiler, emocoes[], publica, data
usuarios/{uid}/opinioesPrivadas/{tipo_tmdbId}   -> opinião privada (só o dono lê): mesmos campos de texto
avaliacoes/{tipo_tmdbId}/opinioes/{autorUid}/votos/{votanteUid}  -> valor (1 ou -1)
denuncias/{id}                                  -> leitura restrita ao administrador
```

Observações sobre os blocos K e L e sobre o RF29:

- Nenhuma coleção nova no Firestore. A classificação indicativa vem da TMDB a cada consulta (com cache em memória) e a idade é calculada a partir da data de nascimento guardada em `usuarios/{uid}` (campo `aniversario`, que passa de texto livre, como "15/03", para data completa; RF59).
- A indicação de "introdução já vista" (RF52) é guardada apenas no aparelho (DataStore), não no Firestore.
- Para RN17, o campo de aniversário em `usuarios/{uid}` passa a ter regra de atualização restrita.
- A exclusão de conta (RF29) não cria coleções: apaga os documentos descritos acima. Depende de uma regra de leitura em `/{path=**}/opinioes/{opiniaoId}` (consulta em todas as obras, só para usuário logado), usada para achar as avaliações e os votos do usuário.

### 4.1 Banco local (Room) — Bloco N

O Firestore não ganha coleções com o modo offline. O Bloco N cria apenas um banco local no aparelho, que espelha os dados pessoais do usuário logado:

```
lista                 -> obra (tipo e id da TMDB), título, capa, status, pendenteSincronizacao, atualizadoEm
episodiosAssistidos   -> obra, temporada, episódio, pendenteSincronizacao, atualizadoEm
perfil                -> apelido, avatar, data de nascimento
obrasCache            -> dados das obras da lista (sinopse, temporadas, episódios) para consulta sem internet
```

Observações sobre o Bloco N:

- O escopo local cobre a lista pessoal, o progresso, o perfil do próprio usuário e os dados das obras da lista. A comunidade não é guardada localmente (RN24).
- O acesso ao Room fica atrás de uma interface na camada de dados, para ser trocado por outra implementação em uma futura versão multiplataforma (RNF11).
- Os campos `pendenteSincronizacao` e `atualizadoEm` sustentam a fila de envio (RN22), a resolução de conflitos (RN23) e o aviso de alterações não sincronizadas (RF62).
- O banco local é apagado no logout e na exclusão de conta (RN25).

---

## 5. Documentação a produzir

- Diagrama de casos de uso (atualizar com os Blocos H, I, K, L, M e N e com o RF29, incluindo o ator Visitante)
- Diagrama de arquitetura (camadas ui, data, model, navigation + Firebase + TMDB; incluir o banco local Room e a sincronização com o Firestore)
- Modelagem do banco: coleções e documentos, campos e exemplos de leitura e escrita; incluir as tabelas locais do Room (seção 4.1)
- Diagramas de sequência: login, adicionar à lista, publicar opinião, votar, sincronizar alterações feitas offline
- Modelo de ameaças (STRIDE) e checklist de segurança (OWASP MASVS), incluindo o risco de data de nascimento falsa e de app adulterado no controle de idade (RF50 e RF51), tratado como risco parcialmente aceito, os dados guardados no aparelho pelo modo offline (RF33), além das limitações conhecidas das regras do Firestore (campo `data` vindo do relógio do celular, lista de 24 emoções não validada no servidor, votos órfãos após a exclusão de conta)
- Nota LGPD: registrar o tratamento de dados de crianças e adolescentes decorrente do controle de idade, a exclusão de conta (RF29), com o que é mantido: denúncias feitas pelo usuário (registro de moderação) e votos de terceiros nas opiniões dele, e os dados pessoais guardados no aparelho, apagados no logout e na exclusão de conta (RN25)
- Justificativa de por que a versão iOS não está no escopo atual (seção 1.5)
- Esteira de publicação (RNF12), descrita abaixo

### 5.1 Esteira de publicação

**Google Play (obrigatória)**
- Gerar o App Bundle (.aab) assinado em modo release.
- Criar o app no Google Play Console, com ficha da loja, ícone e capturas de tela.
- Publicar a política de privacidade e preencher a seção de segurança dos dados (Data safety).
- Distribuir primeiro em teste interno e depois em produção.
- Atender à exigência de exclusão de conta dentro do app (RF29).

**Apple App Store (preparação documentada; execução opcional, RF61)**
- Conta Apple Developer, app criado no App Store Connect e bundle ID registrado.
- Certificados e perfis de provisionamento.
- Build e envio pelo Xcode (ou Xcode Cloud / Fastlane), com distribuição de testes pelo TestFlight.
- Ficha da loja, política de privacidade e informações de privacidade do app.
- Pré-requisitos de revisão da Apple: login equivalente (Sign in with Apple ou RF32), exclusão de conta (RF29, já atendida), denúncia (RF28, já atendida) e bloqueio de usuários (a criar).
- Observação: o Expo EAS é a esteira de apps Expo/React Native. Como o Seriando é Kotlin com Compose Multiplatform, a esteira iOS equivalente passa pelo Xcode e pelo TestFlight, e o EAS não se aplica a esta stack.
