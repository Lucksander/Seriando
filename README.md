# 📺 Seriando

Aplicativo Android para organizar as séries e filmes que você assiste, avaliar, comentar e acompanhar com uma comunidade. Pesquise, acompanhe temporadas e episódios, marque o que já viu e nunca perca onde parou.

Projeto desenvolvido em Kotlin com foco em aprendizado, consumo de APIs, autenticação, persistência de dados e boas práticas de desenvolvimento Android.

## 📌 Versão Atual

Versão: V0.9 (em desenvolvimento — rumo à V1.0)

Novidades da V0.9

- Avaliação por estrelas (1 a 5), com média da comunidade
- Opiniões em texto sobre séries e filmes, públicas ou privadas
- Aviso de spoiler, com texto oculto até o leitor tocar para revelar
- Marcação de até 6 emoções por opinião (ex: Raiva, Curiosidade, Euforia), exibidas como bolinha colorida + nome
- Editar e remover a própria opinião

## 📖 Sobre o Projeto

O Seriando é um aplicativo Android que utiliza a API da TMDB para buscar informações de séries e filmes. O usuário faz login com a conta Google, monta sua lista pessoal, acompanha o progresso de cada série episódio por episódio, organiza tudo por status, avalia e comenta publicamente, com os dados salvos na nuvem.

O projeto está sendo desenvolvido de forma incremental, com o objetivo de praticar Kotlin, Jetpack Compose, integração com APIs REST, Firebase, persistência de dados e versionamento com Git/GitHub.

## 🚀 Funcionalidades

### Já implementado

- Navegação por abas: Home, Minha Lista e Usuário
- Home com busca e grade de destaques aleatórios (séries e filmes em alta)
- Busca unificada de séries e filmes (TMDB search/multi)
- Tela de Detalhes com capa, nota da TMDB, sinopse, elenco e trailer
- Temporadas e episódios, com marcação individual ou por temporada inteira
- Progresso de episódios assistidos por série
- Login com conta Google (Firebase Authentication)
- Perfil do usuário: apelido editável, avatar (foto do Google ou emoji), e-mail, aniversário (privado)
- Adicionar/remover itens da lista pessoal (Firestore)
- Status de acompanhamento por item, com conjuntos diferentes para série e filme
- Filtro por status na Minha Lista
- Avaliação por estrelas (1 a 5) e média da comunidade
- Opiniões públicas/privadas, com spoiler e emoções marcadas pelo autor
- Chave da API protegida fora do repositório

### Planejado (até a V1.0)

- Curtir/descurtir opiniões da comunidade
- Denunciar uma opinião
- Onde assistir: disponibilidade em serviços de streaming no Brasil (TMDB + JustWatch)
- Página do ator/atriz: biografia e filmografia
- Home organizada por categorias de gênero, com "ver mais" por categoria
- Thumbnail de cada episódio na tela de Detalhes
- Regras de segurança do Firestore para produção
- Tratamento de erros e estados de carregamento em todas as telas
- Tema visual próprio e ícone do app
- Testes

## 🛠️ Tecnologias Utilizadas

- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- Retrofit + Gson
- Coil
- TMDB API
- Firebase Authentication (login com Google via Credential Manager)
- Cloud Firestore
- Git e GitHub

## 📂 Estrutura do Projeto

```
com.seriando.app
│
├── ui/
│   ├── home/        Home, cartões de resultado/destaque/lista, edição de apelido
│   ├── search/       Tela de pesquisa
│   ├── details/      Detalhes (série/filme), avaliação, opiniões, emoções
│   ├── mylist/        Minha Lista, com filtro por status
│   ├── usuario/        Aba Usuário: perfil público e privado, avatar
│   └── login/          Diálogo de login obrigatório
│
├── data/
│   ├── remote/        Retrofit e chamadas à TMDB
│   ├── auth/          Autenticação (Firebase + Credential Manager)
│   ├── repository/    Minha Lista, Progresso, Perfil, Avaliação (Firestore)
│   └── cache/          Cache em memória (pesquisa e aba selecionada)
│
├── model/              classes de dados, status e emoções
├── navigation/          rotas, NavHost e estrutura de abas (MainScaffold)
└── MainActivity.kt
```

## ⚙️ Instalação

Clone o repositório:

```
git clone https://github.com/Lucksander/seriando.git
```

Abra a pasta do projeto no Android Studio e aguarde a sincronização do Gradle.

## 🔧 Configuração

O app precisa de uma chave da TMDB e de um projeto Firebase para funcionar.

**TMDB**

1. Crie uma conta em https://www.themoviedb.org/
2. Nas configurações da conta, acesse a aba API e solicite uma chave (opção Developer)
3. No arquivo `local.properties`, na raiz do projeto, adicione (sem aspas):

```
TMDB_API_KEY=sua_chave_aqui
```

**Firebase**

1. Crie um projeto em https://console.firebase.google.com
2. Registre o app Android com o pacote `com.seriando.app` e adicione o SHA-1 de debug
3. Baixe o `google-services.json` e coloque na pasta `app/`
4. Ative o login com Google em Authentication e crie o banco em Firestore Database
5. Copie o Web Client ID (`client_type: 3` no `google-services.json`) para `app/src/main/res/values/strings.xml`:

```xml
<string name="default_web_client_id">SEU_WEB_CLIENT_ID</string>
```

Nenhum desses arquivos (`local.properties`, `google-services.json`) vai para o repositório — ambos estão no `.gitignore`.

## ▶️ Como Executar

1. Clique em Sync Now, se o Android Studio pedir
2. Escolha um emulador ou conecte um dispositivo Android
3. Clique em Run

Requisitos: Android Studio atualizado e Android 7.0 (API 24) ou superior.

## 📈 Histórico de Versões

### V0.1

- Criação do projeto Android com Jetpack Compose
- Tela inicial com campo de pesquisa
- Configuração do Git/GitHub e estrutura de pastas

### V0.2

- Integração com a API da TMDB usando Retrofit
- Primeira requisição e pesquisa de séries
- Proteção da API Key com local.properties

### V0.3

- Exibição de capa, ano e sinopse
- Cartão de série com Coil

### V0.4

- Navegação entre telas com Navigation Compose
- Separação da Home e da tela de Pesquisa
- Tela de Detalhes da série (capa, temporadas/episódios, sinopse completa)

### V0.5

- Login com Google usando Firebase Authentication e Credential Manager

### V0.6

- Integração com Cloud Firestore
- Adicionar série/filme à lista pessoal

### V0.7

- Navegação por abas (Home / Minha Lista)
- Grade de destaques aleatórios na Home
- Busca unificada de séries e filmes
- Progresso de episódios assistidos

### V0.8

- Status de acompanhamento (diferente para série e filme)
- Nota, elenco e trailer na tela de Detalhes
- Marcar temporada inteira como assistida
- Remover item da lista
- Pesquisa e aba preservadas ao voltar
- Aba Usuário: perfil público/privado, avatar, aniversário

### V0.9

- Avaliação por estrelas e média da comunidade
- Opiniões públicas/privadas, com spoiler
- Emoções marcadas pelo autor da opinião, com indicador colorido

## 📌 Próximas Versões (rumo à V1.0)

### V0.10

- Curtir/descurtir opiniões
- Denunciar uma opinião

### V0.11

- Onde assistir (streaming no Brasil, via TMDB + JustWatch)
- Página do ator/atriz (biografia e filmografia)

### V0.12

- Home organizada por categorias de gênero, com "ver mais"
- Thumbnail de cada episódio na tela de Detalhes

### V1.0 — Versão completa

- Regras de segurança do Firestore para produção
- Tratamento de erros e estados de carregamento em todas as telas
- Tema visual próprio e ícone do app
- Testes
- Publicação na Play Store (futuro)

## 🎯 Objetivos de Aprendizado

Este projeto está sendo utilizado para praticar:

- Kotlin
- Jetpack Compose
- Navigation Compose
- Consumo de APIs REST com Retrofit
- Coroutines
- Autenticação com Firebase e Google Sign-In
- Cloud Firestore
- Organização de projeto Android
- Versionamento com Git
- GitHub
- Documentação técnica
- Boas práticas de programação

## ⚠️ Aviso

Este projeto possui finalidade educacional e está em desenvolvimento.

Este produto usa a API da TMDB, mas não é endossado nem certificado pela TMDB. Os dados e imagens pertencem à TMDB e aos seus respectivos detentores de direitos.

## 📄 Licença

Distribuído sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👨‍💻 Autoria

Lucas Santos

Erika Passos