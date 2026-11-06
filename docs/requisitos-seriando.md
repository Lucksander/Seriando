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
| RF08 | Definir status de acompanhamento (séries e filmes têm conjuntos diferentes). Séries: Quero assistir, Assistindo, Maratonei e Abandonei. Filmes: Quero assistir, Concluído e Abandonei. Os nomes gravados no Firestore (`COMPLETO`, `ASSISTIDO`, `ABANDONEI`) não mudam; só os rótulos exibidos | Concluído |
| RF09 | Marcar episódios e temporadas como assistidos | Concluído |
| RF10 | Acompanhar o progresso da série | Concluído |
| RF11 | Ver destaques na Home e navegar por abas | Concluído |
| RF12 | Aba Conta (antes chamada Usuário): exibir apelido, foto e e-mail | Concluído (aba renomeada no app: RF58) |
| RF13 | Editar apelido | Concluído |
| RF14 | Informar data de aniversário (opcional, visível apenas ao próprio usuário) | Concluído (substituído pelo RF59) |
| RF15 | Foto de perfil: usar a foto da conta Google ou escolher um avatar do app (a foto própria da galeria é o RF30) | Concluído |
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
| RF27 | Sair da conta (logout), limpando o cache local. O botão "Sair da conta" é largo e em destaque na aba Conta, e sempre pede confirmação (RN27) | Concluído |
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
| RF45 | Incluir as categorias "Séries em alta" e "Filmes em alta", cada uma só com o seu tipo, com os títulos mais populares da semana | Concluído |
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
| RF53 | Tela de entrada, mostrada depois da introdução e em toda abertura em que não houver usuário logado, e também depois de sair da conta ou excluí-la: logotipo, botão "Entrar com o Google", botão "Entrar com e-mail" (RF32), opção "Continuar como visitante" e links para a Política de Privacidade e os Termos de Uso | Concluído |
| RF54 | Modo visitante: permite navegar na Home, usar a Descobrir e o sorteio, pesquisar, ver detalhes, onde assistir, elenco e página da pessoa; ao tentar adicionar à lista, marcar episódios, avaliar ou ver a comunidade, abre uma folha inferior (BottomSheet) com os benefícios de ter conta (lista sincronizada, opinião e comunidade, modo offline) e os botões "Entrar com o Google" e "Entrar com e-mail"; as abas Minha Lista e Conta mostram um aviso com o botão "Entrar" | Concluído |
| RF77 | Política de Privacidade (LGPD) e Termos de Uso dentro do app, em texto guardado no código (funcionam sem internet e para o visitante), com o e-mail de contato; abrem a partir das telas de introdução, da tela de entrada e da seção Sobre da aba Conta | Concluído |
| RF78 | Avatares do app em imagem: 15 fotos escolhidas em uma folha inferior, junto da foto da conta Google quando houver; substituem os avatares em emoji (quem tinha um emoji vê a inicial do apelido até escolher uma foto) | Concluído |
| RF79 | Aba Conta reorganizada em cartões: cabeçalho com avatar, seção Perfil (apelido, e-mail, data de nascimento e classificação liberada), seção Sobre (Termos de Uso, Política de Privacidade, Rever tour e versão) e seção Conta (sair e excluir a conta) | Concluído |
| RF49 | Exibir a classificação indicativa do Brasil (L, 10, 12, 14, 16, 18) nos detalhes da obra | Concluído |
| RF50 | Ocultar as obras acima da idade do usuário na Home, na busca, nas categorias, na filmografia da pessoa, na Descobrir e no histórico de obras vistas | Concluído |
| RF51 | Bloquear a tela de Detalhes de uma obra acima da idade do usuário, mesmo quando acessada por outro caminho, com um aviso e o botão Voltar | Concluído |
| RF59 | Informar a data de nascimento completa (dia, mês e ano) com seletor de data na aba Conta, necessária para calcular a idade; substitui o aniversário em texto livre do RF14 | Concluído |
| RF73 | Botão de compartilhar na tela de Detalhes (ícone Outlined ao lado do nome da obra): abre o menu de compartilhar do Android com uma mensagem e o link da página da obra na TMDB; funciona também para o visitante | Concluído |
| RF74 | Exibir, nos detalhes, um selo com a situação da obra segundo a TMDB (séries: em exibição, finalizada, cancelada, em produção, planejada ou piloto; filmes: só quando ainda não lançado ou cancelado) | Concluído |
| RF75 | Exibir o ano de lançamento nos detalhes: filmes mostram o ano; séries mostram o intervalo ("2019 – 2024" ou "2019 – atual") | Concluído |
| RF76 | Exibir as temporadas como cartões selecionáveis, com nome, quantidade de episódios, ano e barra de progresso dos episódios assistidos | Concluído |
| RF30 | Foto de perfil própria: escolher uma foto da galeria (Photo Picker), que o app recorta em quadrado (384 px) e guarda **somente no aparelho**, sem Firebase Storage e sem sincronização; os outros usuários continuam vendo o avatar do perfil público (RN37). Substitui a proposta original de enviar a foto ao Firebase Storage | Concluído |
| RF32 | Login com e-mail e senha (Firebase Authentication) como alternativa ao Google: cadastro com apelido, confirmação obrigatória do e-mail (RN33), entrada, recuperação de senha e reenvio do e-mail de confirmação, em uma folha inferior de quatro etapas aberta pelo botão "Entrar com e-mail" da tela de entrada e do aviso de login. Quem já usa Google continua entrando normalmente com o mesmo endereço | Concluído |
| RF66 | Buscar por nome (ignorando maiúsculas e acentos) e ordenar a Minha Lista por data de adição, nome ou status, com filtros fixos por status em chips | Concluído |
| RF67 | Exibir estatísticas na aba Conta (cartão "Minha atividade"): filmes concluídos, séries maratonadas e episódios assistidos, contados na hora a partir do banco local | Concluído |
| RF80 | Escolher o tema do app na aba Conta (seção Aparência): do aparelho, claro ou escuro; a escolha fica só no aparelho (DataStore) e vale também para o visitante. O tema claro usa uma paleta lilás mais viva | Concluído |
| RF81 | Fileira "Recomendados para você" no topo da Home, para quem está logado: obras parecidas com as últimas que o usuário terminou (séries com status Maratonei e filmes Concluído), vindas do endpoint de recomendações da TMDB, sem repetir o que já está na lista e respeitando o controle de idade (RF50). Não aparece para o visitante nem para quem ainda não terminou nenhuma obra (versão simples; ver sugestão de evolução em 1.4) | Concluído |
| RF82 | Puxar para atualizar na Home: arrastar a tela para baixo descarta o cache do catálogo (em memória e em disco, RN39) e busca de novo as categorias e as recomendações, com o indicador de progresso do Material 3 | Concluído |
| RF83 | Fileira "Continuar assistindo" no topo da Home, para quem está logado: séries com status Assistindo, da mais recentemente assistida para a mais antiga (até 10), cada uma com o próximo episódio (por exemplo "Próximo: T2 · E5"), barra de progresso e a contagem de episódios assistidos; "Em dia" quando não há próximo episódio. Usa só dados do aparelho (lista, progresso e detalhes guardados), sem requisições, e funciona offline | Concluído |
| RF84 | Carrossel de destaques no topo da Home: cartões grandes com a imagem horizontal (backdrop), o tipo, o nome e a sinopse resumida de até 3 séries e 3 filmes em alta, alternados; passa sozinho a cada 5 segundos (pausa enquanto o usuário arrasta), mostra pontinhos de posição e abre os detalhes ao toque. Usa as listas de "em alta" já carregadas, sem requisição extra, e respeita o controle de idade | Concluído |
| RF85 | Ranking nas fileiras "Séries em alta" e "Filmes em alta": só o Top 10, com a posição (1 a 10) em número grande sobre a capa; a lista completa continua no "Ver mais" | Concluído |
| RF68 | Ocultar, para quem denunciou, a opinião denunciada: ao confirmar a denúncia (RF28) ela some da tela na hora, com o aviso "Denúncia enviada. A opinião foi ocultada para você.", e continua oculta nas próximas vezes e em outros aparelhos, porque a marca fica na conta do usuário (RN26). Os demais usuários continuam vendo a opinião | Concluído |
| RF86 | Tour guiado da Home, mostrado na primeira vez que a Home aparece (para o visitante e para quem está logado): a tela escurece, um recorte destaca o elemento do passo e um balão com título, texto e os botões "Próximo" e "Pular" explica a funcionalidade. Seis passos, nesta ordem: barra de pesquisa, carrossel de destaques, primeira fileira de gênero e as abas Descobrir, Minha Lista e Conta. A Home rola sozinha até cada alvo, passos cujo alvo não está na tela (por exemplo, carrossel sem destaques carregados) são pulados e a tecla Voltar do Android pula o tour. A indicação de "tour visto" fica só no aparelho (DataStore, `tour_home_visto`, RN42 e RN43) | Concluído |
| RF87 | Tour guiado da tela de Detalhes, mostrado na primeira vez que uma obra é aberta: três passos (Onde assistir, Sua avaliação e Opiniões da comunidade), com rolagem automática até cada seção. Para o visitante, o último passo troca o texto por um convite a entrar na conta, porque a comunidade exige login (RN08). A indicação fica só no aparelho (DataStore, `tour_detalhes_visto`, RN42 e RN43) | Concluído |
| RF88 | Opção "Rever tour" na seção Sobre da aba Conta (também para o visitante): apaga as duas indicações de tour visto, volta para a Home e roda o tour da Home de novo; o tour dos Detalhes reaparece na próxima obra aberta | Concluído |

### 1.2 A implementar (escopo atual)

Todos os blocos do escopo atual foram concluídos.

### 1.3 Opcionais (se sobrar tempo, por ordem de prioridade)

| ID | Requisito | Situação |
|----|-----------|----------|
| RF31 | Avaliar e opinar por episódio | Opcional |
| RF60 | Versão iOS do aplicativo em Compose Multiplatform, reaproveitando o código compartilhado (ui, ViewModels, Repository e model) | Opcional |
| RF61 | Publicação na Apple App Store: distribuição de testes pelo TestFlight e envio para revisão | Opcional |

RF60 e RF61 dependem dos pré-requisitos descritos na seção 1.5. O RF32 (login com e-mail e senha), que seria o login equivalente exigido pela Apple quando não houver Sign in with Apple, já está concluído.

Os RF63 a RF68 vieram do documento propostas_e_melhorias_seriando.md. Os RF63, RF64 e RF65 foram implementados com um desenho ajustado (seção 1.1): sem filtro por década nem nota mínima, e com o streaming escolhido no menu Catálogo. Os RF66, RF67 e RF68 também foram implementados, e não resta nenhum deles como opcional.

### 1.4 Sugestões para versões futuras (fora do escopo atual)

| Sugestão | Origem |
|----------|--------|
| Contadores de curtidas e descurtidas gravados no documento da opinião, mantidos por Cloud Function (ou transação com regras estritas) | Etapa de economia de requisições (RNF19); reduz a leitura de votos a 1 por opinião exibida. Ver estrategia-de-requisicoes.md, seções 6 e 8 |
| Firebase App Check (Play Integrity) e restrição da chave do Firebase ao pacote e à impressão digital do app | Adiado: o provedor gratuito depende de o app ser reconhecido pela Play Store; fazer junto com a publicação |
| Servidor intermediário para a TMDB (cache central e chave fora do APK) e licença comercial da TMDB | Necessário se o app tiver anúncios ou venda, pelos termos da TMDB. Ver estrategia-de-requisicoes.md, seção 8 |
| Exclusão de conta sem varrer todas as opiniões (gravar o uid do votante nos votos e criar índice, ou usar Cloud Function) | Hoje a rotina percorre todas as opiniões do app (RF29); aceitável na escala atual |
| Mostrar a Home com dados guardados, mesmo vencidos, quando estiver sem internet | Mudaria o comportamento aprovado do modo offline (RN24) |
| Evoluir as recomendações (RF81) usando também as notas dadas pelo usuário e os gêneros que ele mais assiste, com uma tela "Ver mais" | A versão simples (RF81) foi implementada; esta evolução vinha da pergunta aberta da pesquisa de viabilidade (ver estudo-viabilidade.md) |
| Descobrir obras pelas emoções registradas pela comunidade | Proposta do documento de propostas; exige contadores calculados no servidor (Cloud Function, plano pago do Firebase) e só teria resultados para obras avaliadas por usuários |
| Notificação de novos episódios e temporadas das séries marcadas como "Assistindo" | Proposta do documento de propostas; exige tarefa em segundo plano (WorkManager) e permissão de notificação |
| Média de estrelas dadas pelo usuário nas estatísticas da aba Conta | Proposta do documento de propostas; exige ler as avaliações do usuário em todas as obras |
| Login com a conta Microsoft | Avaliado e adiado porque o esforço não compensa neste momento. Exigiria registrar um app no Microsoft Entra, ativar o provedor no Firebase, ajustar as regras do Firestore (a conta Microsoft pode vir com `email_verified` falso) e tratar "conta já existe com outra credencial" |
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
| RNF13 | Estratégia de cache das chamadas à TMDB definida em três camadas: cache em memória na sessão (Home, busca e Descobrir), cópia das obras no Room (obrasCache) com tempo de validade (RN39) e cache HTTP em disco para as listas de catálogo, para reduzir chamadas redundantes e consumo de dados (ver RNF19) | Desempenho |
| RNF14 | Conteúdo das telas começa abaixo da barra de status e não rola por baixo dos ícones dela, mantendo a barra transparente (tela de ponta a ponta) | Usabilidade |
| RNF15 | Ícones da interface no estilo Outlined do Material Icons, de forma consistente (preenchidos só onde o preenchimento tem significado, como a estrela da avaliação) | Usabilidade (identidade visual) |
| RNF16 | Backup automático do Android desativado (`allowBackup="false"`), para que o banco local com dados pessoais não seja copiado para a nuvem do Google (STRIDE I4) | Segurança / Privacidade |
| RNF17 | Testes automatizados das regras de negócio puras (data de nascimento e idade, controle de idade, leitura da classificação indicativa, status, busca e ordenação da Minha Lista, escolha e combinação das recomendações, validação de e-mail, senha e apelido, e mensagens de erro do login), executados na JVM sem emulador, e do banco local (Room, incluindo as estatísticas), executados no Android com banco em memória | Qualidade / Manutenibilidade |
| RNF18 | Contas de e-mail e senha: a senha nunca é guardada pelo app (fica só no Firebase Authentication), tem de 8 a 64 caracteres, e as mensagens de erro de entrada e cadastro são neutras para não revelar quais e-mails têm conta (proteção contra enumeração de e-mails, RN35) | Segurança / Privacidade |
| RNF19 | Uso eficiente das cotas e dos limites dos serviços externos (TMDB e Firestore), para que o custo por usuário não cresça com o volume de dados e o app possa crescer sem estourar o plano gratuito: chamadas reunidas (append_to_response), cache com validade, consultas de agregação, paginação, sincronização por diferença e nova tentativa em caso de erro 429 (cabeçalho Retry-After, no máximo 2 tentativas). Justificativa, números de antes e depois, cenários de crescimento e plano de evolução em estrategia-de-requisicoes.md | Desempenho / Escalabilidade |
| RNF20 | Todas as imagens (capas, backdrops e fotos) aparecem com um fade suave de 350 ms ao terminar de carregar, configurado uma única vez no carregador de imagens (Coil) | Usabilidade |

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
| RN26 | Uma opinião denunciada pelo usuário fica oculta só para ele, nas telas de qualquer aparelho em que entrar: a marca é gravada na conta dele, em `usuarios/{uid}/opinioesOcultas`, no mesmo lote da denúncia (ou as duas são gravadas, ou nenhuma), e é apagada na exclusão de conta (RF29). A marca vale para aquele autor naquela obra, e os demais usuários continuam vendo a opinião. A denúncia continua sendo gravada em denuncias, de acesso restrito (RF28) |
| RN27 | Ao sair da conta com alterações ainda não sincronizadas, o app tenta enviá-las e, se não conseguir, avisa quantas serão perdidas e pede confirmação, porque o banco local é apagado no logout (RN25); sem pendências, pede uma confirmação simples |
| RN28 | Sem conexão, as alterações ficam pendentes e o app não tenta enviá-las; a sincronização completa roda ao abrir o app, no login e quando a internet volta |
| RN29 | O histórico de busca (pesquisas e obras vistas recentemente) fica só no aparelho (SharedPreferences), nunca é enviado ao Firestore e vale também para o visitante; não é apagado no logout nem na exclusão de conta, e o usuário o apaga pelos botões "Limpar" da tela de busca |
| RN30 | A escolha de "Continuar como visitante" vale só para aquela abertura do app e não é guardada: sem usuário logado, a tela de entrada aparece em toda abertura. Só a indicação de "introdução já vista" é guardada, no aparelho (DataStore) |
| RN31 | Se a classificação indicativa de uma obra não puder ser consultada (por exemplo, sem conexão), ela é tratada como sem classificação e fica oculta para menores de 18 anos (RN14) |
| RN32 | As listas guardadas em memória (Home, busca e Descobrir) são descartadas e recarregadas quando a idade do usuário muda: login, logout, data de nascimento salva ou recebida da nuvem |
| RN33 | Uma conta de e-mail e senha só vale depois de o e-mail ser confirmado: até lá o app a trata como visitante (nada é gravado na nuvem ou na lista) e as regras do Firestore exigem `email_verified == true` para ler e gravar. Contas Google já chegam confirmadas |
| RN34 | O cadastro exige e-mail em formato válido (até 254 caracteres), senha de 8 a 64 caracteres, confirmação da senha igual e apelido de até 40 caracteres |
| RN35 | Os erros de entrada e de cadastro são neutros: "E-mail ou senha incorretos." vale para senha errada e para e-mail inexistente, e o aviso de e-mail já cadastrado orienta a usar "Esqueci minha senha" sem dizer se a conta é do Google ou de e-mail e senha |
| RN36 | Ao excluir a conta (RF29), quem entrou com e-mail e senha digita a senha de novo (reautenticação); quem entrou com Google continua reautenticando pelo Google |
| RN38 | As recomendações da Home (RF81) partem das até 5 obras terminadas mais recentes da lista (séries Maratonei, filmes Concluído); obras que já estão na lista nunca são recomendadas, a obra recomendada por mais de uma base sobe na fileira, a fileira tem no máximo 20 obras, passa pelo filtro por idade (RN15) e nada é gravado no Firestore |
| RN37 | A foto de perfil própria (RF30) existe em um único arquivo no armazenamento interno do app, nunca é enviada à nuvem e é apagada no logout, na exclusão da conta, na troca de usuário no aparelho e ao escolher outro avatar |
| RN39 | As respostas da TMDB têm validade antes de serem pedidas de novo. No Room: detalhes e temporadas 6 horas e "onde assistir" 24 horas; a cópia vencida só é usada quando a rede falha (modo offline). Em disco (cache HTTP de 10 MB): Em alta e Descobrir (inclui as categorias da Home) 3 horas, recomendações 6 horas, e gêneros, streamings, pessoas e classificações 24 horas; busca, detalhes e temporadas não entram nesse cache. O "puxar para atualizar" da Home (RF82) esvazia o cache em disco |
| RN40 | A comunidade de uma obra é lida de forma econômica: a média e o total das notas são calculados pelo servidor (consulta de agregação), as opiniões vêm em páginas (a do próprio usuário sempre no topo, depois lotes de 20 documentos por data, com o botão "Carregar mais opiniões") e os votos só são lidos das opiniões que estão na tela |
| RN41 | Os episódios assistidos são recebidos da nuvem por diferença: lê-se só o que mudou desde a última conferência (com margem de 1 hora para diferenças de relógio) e compara-se o total da nuvem com o local; todos os episódios da série só são lidos se os totais divergirem, se nunca houve leitura completa ou se a última tem mais de 7 dias. As datas de conferência ficam só no aparelho e são apagadas no logout e na exclusão de conta (RN25) |
| RN42 | As indicações de "tour visto" (`tour_home_visto` e `tour_detalhes_visto`) ficam só no aparelho, em DataStore (`PreferenciasApp`), nunca vão ao Firestore, valem também para o visitante e não são apagadas no logout nem na exclusão de conta. Cada parte do tour (Home e Detalhes) tem a sua indicação e aparece uma vez por instalação, até o usuário tocar em "Rever tour" (RF88), que apaga as duas |
| RN43 | O tour só é considerado visto se pelo menos um passo foi realmente exibido: se nenhum alvo estiver na tela (por exemplo, sem internet e sem destaques carregados), a indicação não é gravada e o tour tenta de novo na próxima abertura. Concluir e pular gravam a indicação do mesmo jeito. Dentro de uma sessão, o tour não recomeça sozinho ao trocar de aba nem ao girar a tela |

---

## 4. Modelo de Dados (Firestore)

```
usuarios/{uid}                                  -> dados privados (data de nascimento AAAA-MM-DD, aniversarioAlterado, e-mail)
perfisPublicos/{uid}                            -> apelido, avatar
usuarios/{uid}/series/{serieId}                 -> lista pessoal e status
usuarios/{uid}/series/{serieId}/episodiosAssistidos
avaliacoes/{tipo_tmdbId}/opinioes/{autorUid}    -> nota e opinião pública: autorUid, apelidoAutor, avatarAutor, nota, texto, spoiler, emocoes[], publica, data
usuarios/{uid}/opinioesPrivadas/{tipo_tmdbId}   -> opinião privada (só o dono lê): mesmos campos de texto
usuarios/{uid}/opinioesOcultas/{tipo_tmdbId_autorUid} -> opinião ocultada por quem denunciou (só o dono lê): obraKey, autorUid, ocultadaEm
avaliacoes/{tipo_tmdbId}/opinioes/{autorUid}/votos/{votanteUid}  -> valor (1 ou -1)
denuncias/{id}                                  -> leitura restrita ao administrador
```

Observações sobre os blocos K e L, sobre o RF29 e sobre o RF68:

- Nenhuma coleção nova no Firestore. A classificação indicativa vem da TMDB a cada consulta (com cache em memória) e a idade é calculada a partir da data de nascimento guardada em `usuarios/{uid}` (campo `aniversario`, que passa de texto livre, como "15/03", para data completa; RF59).
- A indicação de "introdução já vista" (RF52) é guardada apenas no aparelho (DataStore), não no Firestore.
- As indicações de tour visto (RF86 a RF88, RN42) também ficam só no aparelho (DataStore, chaves `tour_home_visto` e `tour_detalhes_visto`): o tour não cria coleção no Firestore.
- Para RN17, o campo de aniversário em `usuarios/{uid}` passa a ter regra de atualização restrita. O campo `aniversarioAlterado` (booleano) marca que a única correção já foi usada: as regras do Firestore só permitem mudar a data uma vez, quando essa marca passa de falsa para verdadeira.
- A exclusão de conta (RF29) não cria coleções: apaga os documentos descritos acima, inclusive as marcas de `opinioesOcultas`. Depende de uma regra de leitura em `/{path=**}/opinioes/{opiniaoId}` (consulta em todas as obras, só para usuário logado), usada para achar as avaliações e os votos do usuário.
- O RF68 cria a subcoleção `usuarios/{uid}/opinioesOcultas`: um documento por opinião ocultada, com id `tipoMidia_tmdbId_autorUid` (o que impede duplicar a marca), e os campos `obraKey`, `autorUid` e `ocultadaEm`. Só o dono lê e escreve (regra no `firestore.rules`), e a marca é gravada no mesmo lote da denúncia (RN26).
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
- A sincronização tem duas fases: o envio das pendências (conferindo a nuvem para fazer valer a alteração mais recente) e o recebimento dos dados da nuvem. O recebimento lê documentos da nuvem, então roda ao abrir o app, no login e quando a internet volta; as alterações feitas na tela só disparam o envio (RN28). Os episódios são recebidos por diferença (RN41), para não ler de novo todos os documentos a cada abertura; as datas de conferência ficam em SharedPreferences (`estado_sincronizacao`), e não no Room, para não exigir migração.
- O e-mail não é guardado no banco local, porque o login do Google já o mantém no aparelho (RNF08).
- O cache das obras guarda as respostas da TMDB como texto JSON e só mantém as obras da lista: as demais são apagadas a cada sincronização completa. Cada cópia tem validade (RN39): enquanto vale, a TMDB não é consultada de novo.
- Este bloco foi promovido de opcional para obrigatório (antes RF33 opcional), por orientação do professor: a persistência local é uma das competências centrais da disciplina.

---

## 5. Documentação a produzir

- Diagrama de casos de uso (atualizar com os Blocos H, I, K, L, M e N, com o RF29, com a Descobrir e o sorteio, com o histórico de busca, com o tour guiado e incluindo o ator Visitante)
- Diagrama de arquitetura (camadas ui, data, model, navigation + Firebase + TMDB; incluir o banco local Room e a sincronização com o Firestore)
- Modelagem do banco: coleções e documentos, campos e exemplos de leitura e escrita; incluir as tabelas locais do Room (seção 4.1)
- Diagramas de sequência: login (Google e e-mail e senha), adicionar à lista, publicar opinião, votar, sincronizar alterações feitas offline
- Modelo de ameaças (STRIDE) e checklist de segurança (OWASP MASVS), produzidos em `stride-seriando.md` (itens verificados no código em 24/10/2026 e atualizado em 26/10/2026 com o login por e-mail e senha e a foto local), incluindo o risco de data de nascimento falsa e de app adulterado no controle de idade (RF50 e RF51), tratado como risco parcialmente aceito, os dados guardados no aparelho pelo modo offline (RF33), além das limitações conhecidas das regras do Firestore (campo `data` vindo do relógio do celular, lista de 24 emoções não validada no servidor, votos órfãos após a exclusão de conta)
- Nota LGPD: registrar o tratamento de dados de crianças e adolescentes decorrente do controle de idade, a exclusão de conta (RF29), com o que é mantido: denúncias feitas pelo usuário (registro de moderação) e votos de terceiros nas opiniões dele, e os dados pessoais guardados no aparelho, apagados no logout e na exclusão de conta (RN25)
- Política de Privacidade e Termos de Uso (RF77): produzidos em `politica-de-privacidade.md` e `termos-de-uso.md`, com o mesmo texto exibido no app
- Justificativa de por que a versão iOS não está no escopo atual (seção 1.5)
- Estratégia de requisições e uso de cotas (RNF13 e RNF19): produzida em `estrategia-de-requisicoes.md`, com os limites da TMDB e do Firestore, números de antes e depois, cenários de crescimento, decisões adiadas e plano de evolução
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
- Pré-requisitos de revisão da Apple: login equivalente (Sign in with Apple ou e-mail e senha, RF32, já atendido), exclusão de conta (RF29, já atendida), denúncia (RF28, já atendida) e bloqueio de usuários (a criar).
- Observação: o Expo EAS é a esteira de apps Expo/React Native. Como o Seriando é Kotlin com Compose Multiplatform, a esteira iOS equivalente passa pelo Xcode e pelo TestFlight, e o EAS não se aplica a esta stack.
