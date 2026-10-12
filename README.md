# 📺 Seriando

Aplicativo Android moderno e intuitivo para organizar as séries e filmes que você assiste, avaliar, comentar e acompanhar junto à comunidade. Pesquise títulos, acompanhe temporadas e episódios, veja a disponibilidade nos serviços de streaming, explore a biografia e filmografia de atores/atrizes e nunca mais perca onde parou.

Projeto desenvolvido em **Kotlin** com **Jetpack Compose**, com foco em boas práticas de arquitetura Android, consumo de APIs REST, autenticação com Google Credential Manager, persistência em nuvem com Cloud Firestore e gerenciamento de estado.

---

## 📌 Versão Atual

**Versão: V0.12** *(em desenvolvimento — rumo à V1.0)*

### 🆕 Novidades da V0.12
- **Home por Categorias**: a Home passou a ter uma fileira horizontal para cada gênero (Em alta, Ação, Comédia, Suspense, Drama, Ficção científica, Terror, Romance, Animação, Crime, Documentário e Família), misturando filmes e séries. No fim de cada fileira, o cartão "Ver mais" abre a lista completa da categoria, em grade de 3 colunas com o botão "Carregar mais".
- **Regras de Segurança do Firestore**: o banco saiu do modo de teste. As regras (arquivo `firestore.rules`) limitam cada usuário aos próprios dados, validam os campos gravados, garantem um único voto por usuário por opinião e impedem votar na própria opinião.
- **Opinião privada separada**: a opinião privada passou a ser gravada em um local que só o próprio autor consegue ler, em vez de apenas ficar escondida pelo app.
- **Tratamento de erros e carregamento**: todas as telas mostram indicador de carregamento e mensagem de erro, com botão para tentar de novo quando faz sentido, e uma falha de rede ou de permissão não derruba mais o app.
- **Exclusão de conta e LGPD (RF29)**: funcionalidade para apagar permanentemente a conta e todos os dados vinculados no Firestore em lotes idempotentes, exigindo confirmação de identidade prévia via Credential Manager.

---

## 📖 Sobre o Projeto

O **Seriando** é um aplicativo Android desenvolvido para ajudar cinéfilos e maratonadores a organizarem seus conteúdos de entretenimento. Utilizando a API pública do **TMDB (The Movie Database)**, o aplicativo oferece informações detalhadas sobre séries, temporadas, episódios, filmes, elenco e provedores de streaming.

Com integração ao **Firebase Authentication (Google Sign-In)** e **Cloud Firestore**, cada usuário mantém sua lista pessoal sincronizada na nuvem, pode marcar episódios e temporadas assistidas, classificar obras com notas de 1 a 5 estrelas, compartilhar opiniões públicas ou privadas com tags de emoções, curtir/descurtir comentários da comunidade e reportar conteúdos inadequados.

---

## 🚀 Funcionalidades

### 🔍 Navegação & Busca
- **Aba Home**: Fileiras horizontais por gênero (incluindo "Em alta" na semana), com o cartão "Ver mais" que abre a lista completa da categoria, e botão de acesso rápido à pesquisa.
- **Busca Unificada**: Pesquisa integrada para séries e filmes utilizando a API `/search/multi` do TMDB.
- **Cache de Navegação & Pesquisa**: Preservação do termo buscado, dos resultados obtidos e da aba ativa ao navegar entre telas, evitando requisições redundantes.

### 🎬 Detalhes da Obra & Mídia
- **Informações Completas**: Capa/poster, nota média do TMDB, sinopse, ano de lançamento, elenco interativo e trailer oficial do YouTube.
- **Gerenciamento de Temporadas e Episódios**:
  - Lista de temporadas e episódios com thumbnails individuais.
  - Marcação de episódios individuais ou de temporadas inteiras como assistidos.
  - Barra de progresso visual de episódios assistidos por série.
- **Onde Assistir (JustWatch Integration)**: Exibição dos serviços de streaming disponíveis no Brasil para assinatura, aluguel ou compra.

### 🎭 Página do Ator/Atriz *(Novo na V0.11)*
- **Perfil do Artista**: Foto, nome completo, data e local de nascimento.
- **Biografia Inteligente**: Leitura expansível e busca de biografia em inglês caso a tradução em português esteja ausente na API.
- **Filmografia Interativa**: Grade com até 60 trabalhos mais populares do ator/atriz, exibindo poster, título e nome do personagem, com navegação direta para a obra escolhida.

### 👤 Autenticação & Perfil de Usuário
- **Google Sign-In**: Autenticação via Firebase Auth combinada com o **Android Credential Manager API**.
- **Perfil Personalizável**:
  - Apelido do usuário.
  - Escolha de avatar entre a foto da conta Google ou uma seleção de emojis divertidos.
  - Informações de e-mail e data de aniversário (privada).
- **Diálogo de Login Obrigatório**: Solicitado suavemente quando o usuário tenta realizar ações restritas (adicionar à lista, avaliar, comentar ou curtir).
- **Exclusão Permanente de Conta (RF29 / LGPD)**: Permite ao usuário excluir permanentemente sua conta e todos os dados vinculados no Firestore (lista pessoal, progresso de episódios, avaliações, opiniões e votos) em lotes seguros, exigindo reautenticação via Credential Manager para confirmação de identidade.

### 📋 Minha Lista Pessoal
- **Sincronização Cloud Firestore**: Lista pessoal salva na nuvem e acessível de qualquer dispositivo.
- **Status Personalizado por Tipo de Mídia**:
  - *Séries*: Assistindo, Quero Assistir, Pausado, Finalizado, Abandonado.
  - *Filmes*: Quero Assistir, Já Assisti, Abandonado.
- **Filtro por Status**: Organização rápida dos itens salvos por categoria de status.

### ⭐ Avaliações, Opiniões & Comunidade
- **Avaliação por Estrelas**: Sistema de classificação de 1 a 5 estrelas com cálculo e exibição da média das avaliações da comunidade.
- **Opiniões Públicas & Privadas**: Publicação de críticas de obras com opção de alerta de *spoiler*.
- **Indicadores de Emoção**: Marcação de sentimento predominante ao assistir (Amei, Empolgante, Triste, Chocante, Engraçado, Decepcionante) representado por ícones e cores temáticas.
- **Interação Social (Curtidas & Descurtidas)**:
  - Sistema de votos em opiniões com contagem pública.
  - Possibilidade de alterar ou desfazer o voto a qualquer momento.
- **Sistema de Denúncia**: Canal para reportar opiniões inadequada com seleção de motivo (ex: spoiler não sinalizado, discurso de ódio, spam/ofensa).

### 🔒 Segurança e Privacidade *(Novo na V0.12)*
- **Regras do Firestore por usuário**: lista pessoal, e-mail e aniversário só podem ser lidos e gravados pelo próprio dono.
- **Comunidade apenas para quem entrou**: avaliações e opiniões só são lidas por usuários autenticados; sem login, o app mostra um aviso no lugar.
- **Validação no servidor**: nota de 1 a 5, opinião de até 500 caracteres, no máximo 6 emoções, voto de +1 ou -1 e campos permitidos em cada coleção.
- **Denúncias protegidas**: o app só cria denúncias; elas não podem ser lidas, alteradas nem apagadas pelo aplicativo.
- **Exclusão de conta em lotes**: deleção idempotente no Firestore em etapas (filhos -> pais -> conta) em lotes de até 400 documentos, mediante reautenticação com o Google.
- **Chaves fora do repositório**: chave da TMDB em `local.properties` e `google-services.json` no `.gitignore`.

---

## 🛠️ Tecnologias & Bibliotecas

- **Linguagem**: Kotlin
- **Interface Gráfica**: Jetpack Compose com Material Design 3
- **Navegação**: Navigation Compose
- **Rede & Comunicação API**: Retrofit 2 + Gson Converter + OkHttp
- **Carregamento de Imagens**: Coil 3 (AsyncImage)
- **Autenticação**: Firebase Authentication + Android Credential Manager API + Google Identity
- **Banco de Dados Cloud**: Cloud Firestore
- **APIs Externas**:
  - **TMDB (The Movie Database)**: Dados de filmes, séries, atores e imagens.
  - **JustWatch**: Dados de disponibilidade em streaming no Brasil.
- **Gerenciamento de Build**: Gradle com Version Catalogs (`libs.versions.toml`)

---

## 📂 Estrutura do Projeto

```
com.seriando.app
│
├── data/
│   ├── auth/          # AuthRepository (Firebase Auth + Credential Manager + Logout)
│   ├── cache/         # AbaCache, BuscaCache e CatalogoCache (Preservação de estado em memória)
│   ├── remote/        # RetrofitInstance e TmdbApiService (End-points REST do TMDB)
│   └── repository/    # Repositórios (Avaliacao, Catalogo, Conta, Denuncia, MinhaLista, Perfil, Progresso)
│
├── model/              # Data classes (Movie, Serie, Pessoa, Avaliacao, Categoria, Emocao, Status, WatchProviders, etc.)
│
├── navigation/
│   ├── MainScaffold.kt # Scaffold principal com BottomNavigation (Home, Minha Lista, Usuário)
│   ├── Rotas.kt        # Definição centralizada de rotas e argumentos
│   └── SeriandoNavHost.kt # Grafo de navegação do aplicativo
│
├── ui/
│   ├── details/       # DetailsScreen, ElencoCard, OpiniaoCard, SecaoOndeAssistir, SeletorEmocoes, SeletorEstrelas, DialogoDenuncia
│   ├── home/          # HomeScreen, CategoriaRow, CategoriaScreen, DestaqueCard, ResultadoCard, SerieCardSalva, DialogoEditarApelido, DialogoRemoverDaLista
│   ├── login/         # DialogoLoginObrigatorio
│   ├── mylist/        # MinhaListaScreen (Lista com filtro de status)
│   ├── pessoa/        # PessoaScreen (Página do ator/atriz, biografia e filmografia)
│   ├── search/        # SearchScreen (Campo de busca e resultados)
│   ├── theme/         # Color, Theme e Type (Material 3)
│   └── usuario/       # UsuarioScreen e SeletorAvatar
│
└── MainActivity.kt    # Ponto de entrada do aplicativo
```

---

## ⚙️ Instalação e Configuração

### Requisitos Prévios
- Android Studio Ladybug ou superior
- JDK 11 ou superior
- Dispositivo físico com Android 7.0 (API 24)+ ou Emulador Android

### 1. Clonar o Repositório

```bash
git clone https://github.com/Lucksander/seriando.git
```

### 2. Configurar Chave da API TMDB

1. Crie uma conta em [The Movie Database (TMDB)](https://www.themoviedb.org/).
2. Vá em **Configurações da Conta > API** e solicite uma chave de API (*Developer*).
3. Crie (or edite) o arquivo `local.properties` na raiz do projeto e insira a seguinte chave:

```properties
TMDB_API_KEY=sua_chave_aqui
```

### 3. Configurar o Firebase

1. Acesse o [Console do Firebase](https://console.firebase.google.com/) e crie um novo projeto.
2. Adicione um app Android com o pacote `com.seriando.app` e insira a impressão digital SHA-1 da sua chave de depuração (*debug keystore*).
3. Faça o download do arquivo `google-services.json` e coloque-o na pasta `app/` do projeto.
4. No console do Firebase:
  - Em **Authentication**, ative o provedor **Google**.
  - Em **Firestore Database**, crie o banco de dados.
  - Em **Firestore Database > Regras**, cole o conteúdo do arquivo `firestore.rules` (na raiz do repositório) e publique.
5. Copie o **Web Client ID** (encontrado no `google-services.json` no nó `client_type: 3`) e adicione no arquivo `app/src/main/res/values/strings.xml`:

```xml
<string name="default_web_client_id">SEU_WEB_CLIENT_ID</string>
```

> ⚠️ *Nota: O arquivo `local.properties` e o `google-services.json` estão inclusos no `.gitignore` por motivos de segurança e nunca devem ser commitados no repositório público.*

### 4. Executar o Aplicativo

1. Abra o projeto no Android Studio.
2. Sincronize os arquivos do Gradle (*Sync Project with Gradle Files*).
3. Selecione o dispositivo/emulador e clique em **Run** (`Shift + F10`).

---

## 📈 Histórico de Versões

- **V0.1**: Configuração inicial do projeto Android com Jetpack Compose, estrutura básica de pastas e integração com Git/GitHub.
- **V0.2**: Integração inicial com a API TMDB via Retrofit e pesquisa simples de séries. Proteção de API Key em `local.properties`.
- **V0.3**: Exibição de capas, sinopses e dados das séries obtidos do TMDB usando Coil.
- **V0.4**: Implementação do Navigation Compose, separação de Home, Pesquisa e tela de Detalhes da Série (com temporadas e episódios).
- **V0.5**: Implementação do Login com Google utilizando Firebase Authentication e Credential Manager API.
- **V0.6**: Integração com Cloud Firestore e funcionalidade de salvar séries/filmes na lista pessoal do usuário.
- **V0.7**: Navegação por abas com `MainScaffold`, grade de destaques aleatórios em alta na Home, busca unificada de filmes e séries, e controle de episódios assistidos.
- **V0.8**: Status de acompanhamento diferenciados para séries e filmes, nota do TMDB, elenco, trailers do YouTube, marcação de temporada inteira, remoção de itens da lista, preservação de estado da busca e aba Usuário com perfil público/privado.
- **V0.9**: Sistema de avaliação por estrelas (1 a 5), cálculo da média da comunidade, opiniões públicas e privadas com aviso de spoiler, e seletor de emoções.
- **V0.10**: Sistema de curtidas e descurtidas em opiniões, funcionalidade de denunciar comentários com motivos, thumbnails de episódios e exibição de provedores de streaming ("Onde Assistir") via JustWatch.
- **V0.11**: Página do ator/atriz com foto, nome, data/local de nascimento, biografia expansível (com fallback em inglês) e filmografia completa interativa em grade ordenada por popularidade.
- **V0.12**: **(Atual)** Home organizada por categorias de gênero com "Ver mais" e lista completa paginada, regras de segurança do Firestore com validação de campos, opinião privada em local de leitura restrita, tratamento de erros e carregamento em todas as telas, e exclusão permanente de conta com remoção de dados em lotes (RF29 / LGPD).

---

## 📌 Planejamento Futuro (Rumo à V1.0)

- [x] **Home Organizada por Categorias**: Seções divididas por gêneros cinematográficos com opção de "Ver Mais".
- [x] **Regras de Segurança do Firestore**: Configuração de regras rígidas no Firestore para ambiente de produção.
- [x] **Tratamento Global de Erros**: Melhoria nos estados de carregamento, *shimmers* e tratamento de exceções de rede.
- [ ] **Identidade Visual & Ícone**: Design System proprietário com paleta de cores customizada e ícone exclusivo do app.
- [ ] **Testes Automatizados**: Implementação de testes unitários de repositórios e testes de UI com Compose.
- [ ] **Lançamento na Google Play Store**.

---

## 🎯 Objetivos de Aprendizado

Este projeto serve como portfólio prático e estudo contínuo cobrindo:
- Desenvolvendo UIs declarativas com **Jetpack Compose** e **Material 3**.
- Arquitetura Android limpa e orientada a componentes.
- Consumo e tratamento de APIs REST com **Retrofit 2** e **Coroutines**.
- Integração com serviços de nuvem da suíte **Firebase** (Auth e Firestore).
- Implementação da nova API **Android Credential Manager**.
- Gerenciamento de estado e cache em memória.
- Documentação técnica e versionamento de software.

---

## ⚠️ Avisos e Isenções de Responsabilidade

- Este projeto tem fins **estritamente educacionais e não comerciais**.
- Este produto utiliza a API do TMDB, mas não é endossado nem certificado pelo **TMDB (The Movie Database)**. Todos os dados e imagens pertencem ao TMDB e aos seus respectivos detentores de direitos.
- Os dados de disponibilidade em serviços de streaming são fornecidos por **JustWatch**.

---

## 📄 Licença

Distribuído sob a licença **MIT**. Veja o arquivo `LICENSE` para mais detalhes.

---

## 👨‍💻 Autores

- **Lucas Santos**
- **Erika Passos**
