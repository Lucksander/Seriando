# 📺 Seriando

Aplicativo Android para organizar as séries que você assiste. Pesquise séries, acompanhe temporadas e episódios, marque o que já viu e nunca perca onde parou.

Projeto desenvolvido em Kotlin com foco em aprendizado, consumo de APIs, autenticação, persistência de dados e boas práticas de desenvolvimento Android.

## 📌 Versão Atual

Versão: V0.3 (em desenvolvimento)

Novidades da V0.3

- Exibição da capa, ano de estreia e sinopse de cada série
- Cartão visual para os resultados da pesquisa
- Carregamento de imagens com Coil

## 📖 Sobre o Projeto

O Seriando é um aplicativo Android que utiliza a API da TMDB para buscar informações de séries. A ideia é que o usuário faça login com a conta Google, monte sua lista de séries e acompanhe o progresso de cada uma, episódio por episódio, com tudo salvo na nuvem.

O projeto está sendo desenvolvido de forma incremental, com o objetivo de praticar Kotlin, Jetpack Compose, integração com APIs REST, Firebase, persistência de dados e versionamento com Git/GitHub.

## 🚀 Funcionalidades

### Já implementado

- Tela inicial com campo de pesquisa
- Pesquisa de séries pela API da TMDB
- Exibição de capa, nome, ano de estreia e sinopse
- Chave da API protegida fora do repositório

### Planejado

- Login com conta Google (Firebase Authentication)
- Identificação do usuário autenticado e vínculo da lista à conta
- Tela de detalhes da série
- Visualização de temporadas e episódios
- Adicionar séries à lista pessoal
- Marcar episódios como assistidos
- Acompanhar o progresso de cada série
- Sincronização na nuvem com Cloud Firestore
- Cache local para uso offline com Room

## 🛠️ Tecnologias Utilizadas

- Kotlin
- Jetpack Compose (Material 3)
- Retrofit + Gson
- Coil
- TMDB API
- Firebase Authentication e Google Sign-In (planejado)
- Cloud Firestore e Room (planejado)
- Git e GitHub

## 📂 Estrutura do Projeto

```
com.seriando.app
│
├── ui/
│   ├── home/
│   ├── search/
│   ├── details/
│   └── mylist/
│
├── data/
│   ├── remote/        Retrofit e chamadas à TMDB
│   ├── local/         Room
│   └── repository/
│
├── model/             classes de dados
├── navigation/        navegação entre telas
└── MainActivity.kt
```

## ⚙️ Instalação

Clone o repositório:

```
git clone https://github.com/Lucksander/seriando.git
```

Abra a pasta do projeto no Android Studio e aguarde a sincronização do Gradle.

## 🔧 Configuração da API Key

O app precisa de uma chave da TMDB para funcionar.

1. Crie uma conta em https://www.themoviedb.org/
2. Nas configurações da conta, acesse a aba API e solicite uma chave (opção Developer)
3. No arquivo `local.properties`, na raiz do projeto, adicione (sem aspas):

```
TMDB_API_KEY=sua_chave_aqui
```

O `local.properties` já é ignorado pelo Git, então a chave não vai para o repositório.

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

## 📌 Próximas Melhorias

### V0.4

- Navegação entre telas
- Tela de pesquisa dedicada
- Tela de detalhes da série

### V0.5

- Login com Google usando Firebase Authentication
- Identificação do usuário autenticado

### V0.6

- Minha lista de séries
- Salvar séries, episódios assistidos e progresso no Firestore
- Cache local com Room

### V1.0

- Tratamento de erros, estado offline e loading
- Melhorias de interface
- Testes
- Publicação na Play Store (futuro)

## 🎯 Objetivos de Aprendizado

Este projeto está sendo utilizado para praticar:

- Kotlin
- Jetpack Compose
- Consumo de APIs REST com Retrofit
- Coroutines
- Autenticação com Firebase e Google Sign-In
- Cloud Firestore e Room
- Organização de projeto Android
- Versionamento com Git
- GitHub
- Documentação técnica
- Boas práticas de programação

## ⚠️ Aviso

Este projeto possui finalidade educacional e está em desenvolvimento.

Este produto usa a API da TMDB, mas não é endossado nem certificado pela TMDB. Os dados e imagens das séries pertencem à TMDB e aos seus respectivos detentores de direitos.

## 📄 Licença

Distribuído sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👨‍💻 Autoria

Lucas Santos

Erika Passos