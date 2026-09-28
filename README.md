# Seriando

Aplicativo Android para organizar as séries que você assiste. Pesquise séries, acompanhe temporadas e episódios, marque o que já viu e nunca perca onde parou.

> Projeto em desenvolvimento.

## Funcionalidades

Já implementado:

- Pesquisa de séries pela API da TMDB
- Exibição de capa, ano de estreia e sinopse

Planejadas:

- Login com conta Google
- Visualização de temporadas e episódios
- Marcação de episódios assistidos
- Acompanhamento de progresso por série
- Lista de séries de interesse
- Sincronização na nuvem vinculada à conta do usuário

## Tecnologias

- Kotlin
- Jetpack Compose (Material 3)
- Retrofit + Gson
- Coil (carregamento de imagens)
- TMDB API
- Firebase Authentication e Google Sign-In (planejado)
- Cloud Firestore e Room (planejado)

## Como rodar o projeto

1. Clone o repositório e abra a pasta no Android Studio.
2. Crie uma conta na [TMDB](https://www.themoviedb.org/) e gere uma API Key na seção API das configurações da conta.
3. No arquivo `local.properties`, na raiz do projeto, adicione a linha abaixo (sem aspas):

   ```
   TMDB_API_KEY=sua_chave_aqui
   ```

   O `local.properties` já é ignorado pelo Git, então a chave não vai para o repositório.
4. Sincronize o Gradle (Sync Now) e execute o app em um emulador ou dispositivo.

## Estrutura do projeto

```
com.seriando.app
├── ui/           telas e componentes (home, search, details, mylist)
├── data/         remote (Retrofit/TMDB), local (Room) e repository
├── model/        classes de dados
└── navigation/   navegação entre telas
```

## Autoria

- Lucas Santos
- Erika Passos

## Licença

Distribuído sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## Créditos

Este produto usa a API da TMDB, mas não é endossado nem certificado pela TMDB.# Seriando

