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
| RF14 | Informar data de aniversário (opcional, visível apenas ao próprio usuário) | Concluído (substituído pelo RF59) |
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
| RF33 | Modo offline com persistência local (Room): a lista pessoal, os status, o progresso de episódios, o perfil do próprio usuário e os dados das obras da lista ficam salvos no aparelho; o app abre, permite consultar a lista e marcar episódios sem internet e sincroniza com o Firestore quando a conexão voltar | Concluído |
| RF62 | Avisar o usuário quando o app estiver sem conexão e indicar que há alterações ainda não sincronizadas; as telas que dependem de internet (Home, busca e comunidade) mostram uma mensagem com ícone explicando a falta de conexão e a ação possível (tentar de novo) | Concluído |
| RF63 | Aba "Descobrir" na navegação inferior (que passa a ter 4 itens: Home, Descobrir, Minha Lista e Conta), com três menus suspensos (Tipo: filmes ou séries; Catálogo: Popular, Novos ou um serviço de streaming; Gênero) e uma grade de capas com 4 colunas que carrega a próxima página ao rolar; filtros, resultados e posição da rolagem são mantidos ao voltar da tela de Detalhes | Concluído |
| RF64 | Filtrar a Descobrir por serviço de streaming do Brasil (somente assinatura), escolhido no menu Catálogo, com a lista de serviços vinda da TMDB (sem lojas e agregadores) e atribuição à JustWatch (RNF09) | Concluído |
| RF65 | Botão "Sortear" ("O que assistir hoje?"): sorteia, sem repetir a anterior, uma obra entre as já carregadas na grade da Descobrir, respeitando os filtros ativos, e mostra capa, nome, ano e sinopse com as ações "Ver detalhes" e "Sortear de novo" | Concluído |
| RF69 | Logotipo do Seriando no cabeçalho da Home: o ícone do S como primeira letra, seguido de "eriando" na fonte Poppins SemiBold | Concluído |
| RF70 | Avatar do usuário logado (foto da conta Google ou avatar do app) no canto do cabeçalho da Home; tocar nele abre a aba Conta. O botão "Sair" fica apenas na aba Conta | Concluído |
| RF71 | Barra de pesquisa com lupa na Home (atalho que abre a tela de busca) e na tela de busca (campo de uma linha em que a tecla de pesquisa do teclado dispara a busca, com botão para limpar o texto) | Concluído |
| RF72 | Histórico na tela de busca, mostrado com o campo vazio: obras vistas recentemente (capas, até 12, as abertas a partir da busca) e pesquisas recentes (até 10, só as que devolveram resultados), com opção de apagar uma pesquisa e de limpar cada lista | Concluído |
| RF52 | Telas de introdução na primeira abertura do app: três páginas deslizáveis com indicador de pontos (boas-vindas, onde assistir e comunidade), botão "Pular" no topo de todas e botão "Avançar" que vira "Começar" na última; mostradas apenas uma vez, com a indicação guardada só no aparelho (DataStore, `introducao_vista`) | Concluído |
| RF53 | Tela de entrada, mostrada depois da introdução e em toda abertura em que não houver usuário logado, e também depois de sair da conta ou excluí-la: logotipo, botão "Entrar com o Google", opção "Continuar como visitante" e links para a Política de Privacidade e os Termos de Uso | Concluído |
| RF54 | Modo visitante: permite navegar na Home, usar a Descobrir e o sorteio, pesquisar, ver detalhes, onde assistir, elenco e página da pessoa; ao tentar adicionar à lista, marcar episódios, avaliar ou ver a comunidade, abre uma folha inferior (BottomSheet) com os benefícios de ter conta (lista sincronizada, opinião e comunidade, modo offline) e o botão "Entrar com o Google"; as abas Minha Lista e Conta mostram um aviso com o botão "Entrar" | Concluído |
| RF77 | Política de Privacidade (LGPD) e Termos de Uso dentro do app, em texto guardado no código (funcionam sem internet e para o visitante), com o e-mail de contato; abrem a partir das telas de introdução, da tela de entrada e da seção Sobre da aba Conta | Concluído |
| RF78 | Avatares do app em imagem: 11 fotos escolhidas em uma folha inferior, junto da foto da conta Google quando houver; substituem os avatares em emoji (quem tinha um emoji vê a inicial do apelido até escolher uma foto) | Concluído |
| RF79 | Aba Conta reorganizada em cartões: cabeçalho com avatar, seção Perfil (apelido, e-mail, data de nascimento e classificação liberada), seção Sobre (Termos de Uso, Política de Privacidade e versão) e seção Conta (sair e excluir a conta) | Concluído |
| RF49 | Exibir a classificação indicativa do Brasil (L, 10, 12, 14, 16, 18) nos detalhes da obra | Concluído |
| RF50 | Ocultar as obras acima da idade do usuário na Home, na busca, nas categorias, na filmografia da pessoa, na Descobrir e no histórico de obras vistas | Concluído |
| RF51 | Bloquear a tela de Detalhes de uma obra acima da idade do usuário, mesmo quando acessada por outro caminho, com um aviso e o botão Voltar | Concluído |
| RF59 | Informar a data de nascimento completa (dia, mês e ano) com seletor de data na aba Conta, necessária para calcular a idade; substitui o aniversário em texto livre do RF14 | Concluído |
| RF73 | Botão de compartilhar na tela de Detalhes (ícone Outlined ao lado do nome da obra): abre o menu de compartilhar do Android com uma mensagem e o link da página da obra na TMDB; funciona também para o visitante | Concluído |
| RF74 | Exibir, nos detalhes, um selo com a situação da obra segundo a TMDB (séries: em exibição, finalizada, cancelada, em produção, planejada ou piloto; filmes: só quando ainda não lançado ou cancelado) | Concluído |
| RF75 | Exibir o ano de lançamento nos detalhes: filmes mostram o ano; séries mostram o intervalo ("2019 – 2024" ou "2019 – atual") | Concluído |
| RF76 | Exibir as temporadas como cartões selecionáveis, com nome, quantidade de episódios, ano e barra de progresso dos episódios assistidos | Concluído |

### 1.2 A implementar (escopo atual)

Todos os blocos do escopo atual foram concluídos.

### 1.3 Opcionais (se sobrar tempo, por ordem de prioridade)

| ID | Requisito | Situação |
|----|-----------|----------|
| RF30 | Enviar foto de perfil própria (Photo Picker + Firebase Storage) | Opcional |
| RF31 | Avaliar e opinar por episódio | Opcional |
| RF32 | Login próprio com e-mail e senha (Firebase Authentication) | Opcional |
| RF60 | Versão iOS do aplicativo em Compose Multiplatform, reaproveitando o código compartilhado (ui, ViewModels, Repository e model) | Opcional |
| RF61 | Publicação na Apple App Store: distribuição de testes pelo TestFlight e envio para revisão | Opcional |
| RF68 | Ocultar imediatamente, para quem denunciou, a opinião denunciada, com o aviso "Opinião denunciada e oculta" (RN26) | Opcional |
| RF66 | Buscar por nome e ordenar (data de adição, nome ou status) dentro da Minha Lista | Opcional |
| RF67 | Exibir estatísticas na aba Conta: total de filmes assistidos, de séries completas e de episódios assistidos | Opcional |

RF60 e RF61 dependem dos pré-requisitos descritos na seção 1.5. O RF32 é um deles quando não houver Sign in with Apple.

Os RF63 a RF68 vieram do documento propostas_e_melhorias_seriando.md. Os RF63, RF64 e RF65 foram implementados com um desenho ajustado (seção 1.1): sem filtro por década nem nota mínima, e com o streaming escolhido no menu Catálogo. Seguem opcionais, na ordem sugerida de implementação, os RF66, RF67 e RF68.

### 1.4 Sugestões para versões futuras (fora do escopo atual)

| Sugestão | Origem |
|----------|--------|
| Recomendação de séries e filmes parecidos com os já assistidos | Apontada com destaque na pergunta aberta da pesquisa de viabilidade (ver estudo-viabilidade.md) |
| Descobrir obras pelas emoções registradas pela comunidade | Proposta do documento de propostas; exige contadores calculados no servidor (Cloud Function, plano pago do Firebase) e só teria resultados para obras avaliadas por usuários |
| Notificação de novos episódios e temporadas das séries marcadas como "Assistindo" | Proposta do documento de propostas; exige tarefa em segundo plano (WorkManager) e permissão de notificação |
| Média de estrelas dadas pelo usuário nas estatísticas da aba Conta | Proposta do documento de propostas; exige ler as avaliações do usuário em todas as obras |
| Compartilhamento que abre direto no Seriando | Evolução do RF73: o link da TMDB abre o site, e não o app. Exige deep links (App Links) e uma página própria para cada obra; depois da publicação, a mensagem de compartilhar pode incluir o link do app na Play Store |

### 1.5 Por que o aplicativo para iOS não está no escopo atual

O Seriando precisa ser multiplataforma, e a versão iOS (RF60 e RF61) está registrada como requisito opcional. Ela não foi implementada nesta entrega pelos motivos abaixo, e o projeto fica com a base preparada para ela (RNF11).

1. **Prazo.** A entrega é em novembro. Os requisitos obrigatórios (Blocos K, L e N) foram concluídos; a prioridade agora é a documentação final e os testes.
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

**Justificativa da decisão de priorizar uma única plataforma:**

O objetivo principal do projeto Seriando sempre foi entregar uma solução completa, estável e com alto nível de acabamento funcional e visual. Diante do prazo delimitado para o desenvolvimento, a equipe compreendeu que tentar abraçar o lançamento simultâneo para Android e iOS exigiria dividir esforços entre duas plataformas distintas. Isso poderia comprometer a profundidade das funcionalidades exigidas — como o suporte offline completo, o controle de classificação indicativa e os recursos de comunidade.

Em alinhamento com a orientação do professor, tomou-se a decisão estratégica de priorizar uma única plataforma. A escolha pelo ecossistema Android nativo (com Jetpack Compose) permitiu concentrar 100% da capacidade técnica na construção de uma arquitetura sólida, robusta e pronta para produção.

Apesar do foco inicial, o projeto foi estruturado com os princípios de desacoplamento da camada de dados (RNF11). Dessa forma, a base de código fica devidamente preparada para que a versão iOS possa ser implementada como uma evolução futura do sistema, mantendo o mesmo padrão de qualidade.

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
| RNF13 | Estratégia de cache das chamadas à TMDB definida: cache em memória com tempo de validade na camada Repository e dados das obras da lista guardados no Room (obrasCache), para reduzir chamadas redundantes e consumo de dados | Desempenho |
| RNF14 | Conteúdo das telas começa abaixo da barra de status e não rola por baixo dos ícones dela, mantendo a barra transparente (tela de ponta a ponta) | Usabilidade |
| RNF15 | Ícones da interface no estilo Outlined do Material Icons, de forma consistente (preenchidos só onde o preenchimento tem significado, como a estrela da avaliação) | Usabilidade (identidade visual) |
| RNF16 | Backup automático do Android desativado (`allowBackup="false"`), para que o banco local com dados pessoais não seja copiado para a nuvem do Google (STRIDE I4) | Segurança / Privacidade |

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
| RN16 | Usuário sem data de nascimento informada (ou com data inválida, como o aniversário antigo em texto livre) e visitante são tratados como menores, com uma faixa de classificação restrita: até 12 anos (classificações L, 10 e 12) |
| RN17 | Depois de preenchida, a data de nascimento não pode ser alterada livremente (travada nas regras do Firestore, com no máximo uma correção) |
| RN18 | O visitante não grava dados no Firestore e não lê a comunidade (RN08 continua valendo); ações que exigem conta pedem o login |
| RN19 | Ao excluir a conta (RF29), são apagados o perfil, a lista, o progresso, as avaliações, as opiniões (públicas e privadas) e os votos do usuário; as denúncias feitas por ele são mantidas como registro de moderação, e os votos de outras pessoas nas opiniões dele ficam órfãos e invisíveis, porque só cada autor pode apagar o próprio voto |
| RN20 | Remover um item da lista apaga também o progresso de episódios dele (o usuário é avisado antes); marcar um episódio de uma série que não está na lista a adiciona automaticamente com o status "Assistindo" |
| RN21 | A interface lê sempre do banco local (Room); a sincronização com o Firestore atualiza o banco local em segundo plano |
| RN22 | Alterações feitas sem conexão (lista, status, progresso de episódios) ficam registradas no aparelho como pendentes e são enviadas ao Firestore quando a conexão voltar |
| RN23 | Em conflito entre o dado local e o da nuvem, vale a gravação mais recente; limitação conhecida: a data da gravação vem do relógio do celular |
| RN24 | A comunidade (opiniões, avaliações, votos e médias) e o login exigem conexão, e RN08 continua valendo; sem conexão, o app avisa e mantém disponível o que está salvo no aparelho |
| RN25 | O banco local guarda dados apenas do usuário logado e é apagado por completo no logout (RF27) e na exclusão de conta (RF29) |
| RN26 | Uma opinião denunciada pelo usuário fica oculta para ele, guardada apenas no aparelho; a denúncia continua sendo gravada em denuncias, de acesso restrito (RF28) |
| RN27 | Ao sair da conta com alterações ainda não sincronizadas, o app tenta enviá-las e, se não conseguir, avisa quantas serão perdidas e pede confirmação, porque o banco local é apagado no logout (RN25) |
| RN28 | Sem conexão, as alterações ficam pendentes e o app não tenta enviá-las; a sincronização completa roda ao abrir o app, no login e quando a internet volta |
| RN29 | O histórico de busca (pesquisas e obras vistas recentemente) fica só no aparelho (SharedPreferences), nunca é enviado ao Firestore e vale também para o visitante; não é apagado no logout nem na exclusão de conta, e o usuário o apaga pelos botões "Limpar" da tela de busca |
| RN30 | A escolha de "Continuar como visitante" vale só para aquela abertura do app e não é guardada: sem usuário logado, a tela de entrada aparece em toda abertura. Só a indicação de "introdução já vista" é guardada, no aparelho (DataStore) |
| RN31 | Se a classificação indicativa de uma obra não puder ser consultada (por exemplo, sem conexão), ela é tratada como sem classificação e fica oculta para menores de 18 anos (RN14) |
| RN32 | As listas guardadas em memória (Home, busca e Descobrir) são descartadas e recarregadas quando a idade do usuário muda: login, logout, data de nascimento salva ou recebida da nuvem |

---

## 4. Modelo de Dados (Firestore)

```
usuarios/{uid}                                  -> dados privados (data de nascimento AAAA-MM-DD, aniversarioAlterado, e-mail)
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
- Para RN17, o campo de aniversário em `usuarios/{uid}` passa a ter regra de atualização restrita. O campo `aniversarioAlterado` (booleano) marca que a única correção já foi usada: as regras do Firestore só permitem mudar a data uma vez, quando essa marca passa de falsa para verdadeira.
- A exclusão de conta (RF29) não cria coleções: apaga os documentos descritos acima. Depende de uma regra de leitura em `/{path=**}/opinioes/{opiniaoId}` (consulta em todas as obras, só para usuário logado), usada para achar as avaliações e os votos do usuário.
- A aba Descobrir (RF63 a RF65) e o histórico de busca (RF72) não criam coleções no Firestore: a Descobrir só consulta a TMDB, e o histórico fica no aparelho, em SharedPreferences (RN29).

### 4.1 Banco local (Room) — Bloco N

O Firestore não ganha coleções com o modo offline. O Bloco N cria apenas um banco local no aparelho, que espelha os dados pessoais do usuário logado:

```
lista                 -> id da TMDB (chave), tipo, título, sinopse, capa, status, dataAdicao, pendenteSincronizacao, atualizadoEm, removido
episodiosAssistidos   -> série, temporada, episódio (chave composta), pendenteSincronizacao, atualizadoEm, removido
perfil                -> uid, apelido, avatar, aniversário (data de nascimento a partir do Bloco K), aniversarioCorrigido, pendenteSincronizacao, atualizadoEm
obrasCache            -> tipo, id da TMDB e parte (detalhes, onde assistir ou temporada), json da resposta da TMDB, atualizadoEm
```

Observações sobre o Bloco N:

- O escopo local cobre a lista pessoal, o progresso, o perfil do próprio usuário e os dados das obras da lista. A comunidade não é guardada localmente (RN24).
- O acesso ao Room fica atrás de uma interface na camada de dados, para ser trocado por outra implementação em uma futura versão multiplataforma (RNF11).
- Os campos `pendenteSincronizacao` e `atualizadoEm` sustentam a fila de envio (RN22), a resolução de conflitos (RN23) e o aviso de alterações não sincronizadas (RF62).
- O banco local é apagado no logout e na exclusão de conta (RN25).
- O campo `removido` é uma "lápide": um item removido sem conexão não some do banco na hora, fica marcado e pendente até a exclusão ser enviada à nuvem, e a tela não o mostra.
- A sincronização tem duas fases: o envio das pendências (conferindo a nuvem para fazer valer a alteração mais recente) e o recebimento dos dados da nuvem. O recebimento lê muitos documentos, então roda ao abrir o app, no login e quando a internet volta; as alterações feitas na tela só disparam o envio (RN28).
- O e-mail não é guardado no banco local, porque o login do Google já o mantém no aparelho (RNF08).
- O cache das obras guarda as respostas da TMDB como texto JSON e só mantém as obras da lista: as demais são apagadas a cada sincronização completa.
- Este bloco foi promovido de opcional para obrigatório (antes RF33 opcional), por orientação do professor: a persistência local é uma das competências centrais da disciplina.

---

## 5. Documentação a produzir

- Diagrama de casos de uso (atualizar com os Blocos H, I, K, L, M e N, com o RF29, com a Descobrir e o sorteio, com o histórico de busca e incluindo o ator Visitante)
- Diagrama de arquitetura (camadas ui, data, model, navigation + Firebase + TMDB; incluir o banco local Room e a sincronização com o Firestore)
- Modelagem do banco: coleções e documentos, campos e exemplos de leitura e escrita; incluir as tabelas locais do Room (seção 4.1)
- Diagramas de sequência: login, adicionar à lista, publicar opinião, votar, sincronizar alterações feitas offline
- Modelo de ameaças (STRIDE) e checklist de segurança (OWASP MASVS), produzidos em `stride-seriando.md` (itens verificados no código em 24/10/2026), incluindo o risco de data de nascimento falsa e de app adulterado no controle de idade (RF50 e RF51), tratado como risco parcialmente aceito, os dados guardados no aparelho pelo modo offline (RF33), além das limitações conhecidas das regras do Firestore (campo `data` vindo do relógio do celular, lista de 24 emoções não validada no servidor, votos órfãos após a exclusão de conta)
- Nota LGPD: registrar o tratamento de dados de crianças e adolescentes decorrente do controle de idade, a exclusão de conta (RF29), com o que é mantido: denúncias feitas pelo usuário (registro de moderação) e votos de terceiros nas opiniões dele, e os dados pessoais guardados no aparelho, apagados no logout e na exclusão de conta (RN25)
- Política de Privacidade e Termos de Uso (RF77): produzidos em `politica-de-privacidade.md` e `termos-de-uso.md`, com o mesmo texto exibido no app
- Justificativa de por que a versão iOS não está no escopo atual (seção 1.5)
- Esteira de publicação (RNF12), descrita abaixo

### 5.1 Esteira de publicação

**Google Play (obrigatória)**
- Gerar o App Bundle (.aab) assinado em modo release.
- Criar o app no Google Play Console, com ficha da loja, ícone e capturas de tela.
- Publicar a política de privacidade e preencher a seção de segurança dos dados (Data safety).
- Distribuir primeiro em teste interno e depois em produção.
- Atender à exigência de exclusão de conta dentro do app (RF29).
- Fixar o número de versão do banco local (Room) e escrever a migração antes de qualquer mudança de tabela depois da publicação.

**Apple App Store (preparação documentada; execução opcional, RF61)**
- Conta Apple Developer, app criado no App Store Connect e bundle ID registrado.
- Certificados e perfis de provisionamento.
- Build e envio pelo Xcode (ou Xcode Cloud / Fastlane), com distribuição de testes pelo TestFlight.
- Ficha da loja, política de privacidade e informações de privacidade do app.
- Pré-requisitos de revisão da Apple: login equivalente (Sign in with Apple ou RF32), exclusão de conta (RF29, já atendida), denúncia (RF28, já atendida) e bloqueio de usuários (a criar).
- Observação: o Expo EAS é a esteira de apps Expo/React Native. Como o Seriando é Kotlin com Compose Multiplatform, a esteira iOS equivalente passa pelo Xcode e pelo TestFlight, e o EAS não se aplica a esta stack.
