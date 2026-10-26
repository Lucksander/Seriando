package com.seriando.app.ui.legal

/**
 * Textos legais do Seriando (Termos de Uso e Política de Privacidade), guardados no próprio
 * código para funcionarem sem internet e para o visitante, que não tem conta.
 *
 * Cada documento é uma lista de blocos simples ([BlocoLegal]); a tela [TextoLegalScreen]
 * desenha cada tipo de bloco. Para mudar um texto, basta editar a lista correspondente
 * e atualizar a data em [DocumentoLegal.atualizadoEm].
 */

/** E-mail de contato que aparece nos dois documentos. Para trocá-lo, basta mudar aqui. */
const val EMAIL_DE_CONTATO = "lucasjsantos.dev@gmail.com"

/** Um pedaço de um documento legal. */
sealed interface BlocoLegal {
    /** Título de seção, ex: "1. Quem somos". */
    data class Secao(val titulo: String) : BlocoLegal

    /** Parágrafo de texto corrido. */
    data class Paragrafo(val texto: String) : BlocoLegal

    /** Lista com marcadores. */
    data class Lista(val itens: List<String>) : BlocoLegal

    /**
     * Tabela do documento original. Na tela de celular, cada linha vira um cartão: a primeira
     * coluna é o título e as demais aparecem como "cabeçalho: valor".
     */
    data class Tabela(val cabecalhos: List<String>, val linhas: List<List<String>>) : BlocoLegal
}

/** Um documento legal completo. */
data class DocumentoLegal(
    val titulo: String,
    val atualizadoEm: String,
    val blocos: List<BlocoLegal>
)

// ======================================================================================
// TERMOS DE USO
// ======================================================================================

val TERMOS_DE_USO = DocumentoLegal(
    titulo = "Termos de Uso",
    atualizadoEm = "26 de outubro de 2026",
    blocos = listOf(
        BlocoLegal.Paragrafo("Ao usar o Seriando, você concorda com estes termos. Leia também a Política de Privacidade."),

        BlocoLegal.Secao("1. O que é o Seriando"),
        BlocoLegal.Paragrafo(
            "O Seriando é um aplicativo Android para organizar séries e filmes, acompanhar o progresso de " +
                "episódios, ver onde assistir no Brasil e compartilhar avaliações e opiniões com outros usuários. " +
                "É um projeto de finalidade educacional e não comercial, oferecido gratuitamente."
        ),

        BlocoLegal.Secao("2. Quem pode usar"),
        BlocoLegal.Lista(
            listOf(
                "Você pode usar o app como visitante, sem conta, para pesquisar e consultar obras.",
                "Para criar lista, marcar episódios, avaliar e opinar, é preciso entrar com uma conta Google ou com e-mail e senha (com o e-mail confirmado).",
                "Menores de 12 anos devem usar o app com ciência e acompanhamento de um responsável.",
                "O app oculta obras acima da faixa etária calculada a partir da data de nascimento informada por você. " +
                    "Esse recurso é um auxílio e não substitui a supervisão dos responsáveis. " +
                    "Você se compromete a informar uma data verdadeira."
            )
        ),

        BlocoLegal.Secao("3. Sua conta"),
        BlocoLegal.Paragrafo(
            "Você é responsável pelo uso da sua conta (Google ou e-mail e senha) e por manter sua senha em sigilo. " +
                "Para contas de e-mail e senha, é preciso confirmar o e-mail antes de usá-las, e a exclusão da conta pede " +
                "a senha novamente. A data de nascimento pode ser informada " +
                "e corrigida uma única vez. Você pode sair da conta ou excluí-la a qualquer momento na aba Conta; " +
                "a exclusão apaga seus dados conforme a Política de Privacidade e não pode ser desfeita."
        ),

        BlocoLegal.Secao("4. Conteúdo que você publica"),
        BlocoLegal.Paragrafo(
            "Avaliações e opiniões são de responsabilidade de quem as escreve. Ao publicar uma opinião pública, " +
                "você autoriza o Seriando a exibi-la a outros usuários logados enquanto ela existir. Você continua " +
                "sendo o autor e pode editá-la ou removê-la quando quiser. Opiniões privadas são visíveis apenas para você."
        ),
        BlocoLegal.Paragrafo("Ao publicar, você se compromete a não postar:"),
        BlocoLegal.Lista(
            listOf(
                "conteúdo ofensivo, discriminatório, que incite ódio ou violência;",
                "assédio, bullying ou ameaças;",
                "spam ou propaganda;",
                "informação falsa ou que se passe por outra pessoa;",
                "dados pessoais de terceiros;",
                "conteúdo ilegal ou que viole direitos de terceiros."
            )
        ),
        BlocoLegal.Paragrafo(
            "Use a marcação de spoiler quando sua opinião revelar partes importantes da obra. " +
                "Opiniões marcadas ficam ocultas até o leitor tocar para revelar."
        ),

        BlocoLegal.Secao("5. Moderação e denúncias"),
        BlocoLegal.Paragrafo(
            "Qualquer usuário pode denunciar uma opinião. As denúncias são analisadas pelos desenvolvedores, que " +
                "podem remover conteúdo que viole estes termos e, em caso de reincidência, restringir o acesso à comunidade."
        ),

        BlocoLegal.Secao("6. Dados de catálogo, streaming e propriedade intelectual"),
        BlocoLegal.Paragrafo(
            "Os dados de filmes, séries, elenco, imagens e classificação indicativa vêm da TMDB (The Movie Database), " +
                "e os dados de disponibilidade em streaming vêm da JustWatch. Esses dados e imagens pertencem aos seus " +
                "respectivos detentores. O Seriando utiliza a API da TMDB, mas não é endossado nem certificado por ela. " +
                "Nomes e imagens de obras são usados apenas para identificação."
        ),
        BlocoLegal.Paragrafo(
            "O código e a identidade visual do Seriando pertencem aos seus autores. É proibido copiar, descompilar ou " +
                "adulterar o app para contornar suas proteções, inclusive o controle de idade e as regras de segurança."
        ),

        BlocoLegal.Secao("7. Funcionamento, internet e sincronização"),
        BlocoLegal.Paragrafo(
            "O app guarda uma cópia local de parte dos seus dados para uso sem internet e os sincroniza quando a " +
                "conexão volta. Comunidade, login e catálogo exigem internet. Se você sair da conta com alterações não " +
                "sincronizadas, o app avisa quantas serão perdidas; confirmar a saída significa aceitar essa perda."
        ),
        BlocoLegal.Paragrafo(
            "As informações de classificação indicativa e de streaming dependem de serviços externos e podem estar " +
                "incompletas, desatualizadas ou indisponíveis."
        ),

        BlocoLegal.Secao("8. Limitação de responsabilidade"),
        BlocoLegal.Paragrafo(
            "Por ser um projeto educacional, o Seriando é oferecido \"como está\", sem garantia de disponibilidade " +
                "contínua ou de ausência de erros. Na extensão permitida pela lei, os autores não respondem por " +
                "indisponibilidade do serviço, por informações de terceiros (TMDB e JustWatch), por perda de dados " +
                "decorrente de alterações não sincronizadas ou de falha do aparelho, nem pelo conteúdo publicado por " +
                "outros usuários. Nada nestes termos exclui direitos garantidos por lei, como os do Código de Defesa " +
                "do Consumidor e da LGPD, quando aplicáveis."
        ),

        BlocoLegal.Secao("9. Encerramento"),
        BlocoLegal.Paragrafo(
            "Você pode parar de usar o app e excluir a conta quando quiser. Os autores podem suspender o acesso de " +
                "quem violar estes termos ou encerrar o serviço, com aviso quando possível."
        ),

        BlocoLegal.Secao("10. Mudanças nos termos"),
        BlocoLegal.Paragrafo(
            "Os termos podem ser atualizados. A data de atualização fica no topo, e mudanças relevantes serão " +
                "avisadas no app. O uso após a mudança significa concordância com a nova versão."
        ),

        BlocoLegal.Secao("11. Lei aplicável e contato"),
        BlocoLegal.Paragrafo(
            "Estes termos são regidos pela legislação brasileira. Dúvidas, denúncias e pedidos: $EMAIL_DE_CONTATO"
        )
    )
)

// ======================================================================================
// POLÍTICA DE PRIVACIDADE
// ======================================================================================

val POLITICA_DE_PRIVACIDADE = DocumentoLegal(
    titulo = "Política de Privacidade",
    atualizadoEm = "26 de outubro de 2026",
    blocos = listOf(
        BlocoLegal.Paragrafo(
            "Esta política explica quais dados pessoais o Seriando trata, para quê, com quem são compartilhados e " +
                "como você exerce seus direitos, conforme a Lei Geral de Proteção de Dados (Lei nº 13.709/2018, LGPD)."
        ),

        BlocoLegal.Secao("1. Quem somos"),
        BlocoLegal.Paragrafo(
            "O Seriando é um aplicativo Android de finalidade educacional e não comercial, desenvolvido por Lucas " +
                "Santos e Erika Passos. Os desenvolvedores são os controladores dos dados tratados pelo app."
        ),
        BlocoLegal.Paragrafo("Contato para assuntos de privacidade: $EMAIL_DE_CONTATO"),

        BlocoLegal.Secao("2. Quais dados tratamos"),
        BlocoLegal.Tabela(
            cabecalhos = listOf("Dado", "Quando é coletado", "Para quê", "Fica visível para outros?"),
            linhas = listOf(
                listOf(
                    "E-mail e identificador da conta (UID)",
                    "Ao entrar com o Google ou ao criar a conta com e-mail e senha",
                    "Identificar sua conta e associar seus dados a ela",
                    "Não"
                ),
                listOf(
                    "Apelido",
                    "Ao entrar (sugerido a partir do primeiro nome da conta Google, ou digitado no cadastro com e-mail e senha) e quando você edita",
                    "Identificar você nas opiniões",
                    "Sim, para usuários logados"
                ),
                listOf(
                    "Avatar",
                    "Quando você escolhe a foto da conta Google ou um avatar do app",
                    "Identificar você nas opiniões",
                    "Sim, para usuários logados"
                ),
                listOf(
                    "Foto de perfil própria (opcional)",
                    "Quando você escolhe uma foto da galeria",
                    "Mostrar a sua foto no seu aparelho",
                    "Não; fica só no aparelho e nunca é enviada"
                ),
                listOf(
                    "Data de nascimento (opcional)",
                    "Quando você a informa na aba Conta",
                    "Calcular sua idade para ocultar obras acima da sua faixa de classificação",
                    "Não"
                ),
                listOf(
                    "Lista, status e progresso de episódios",
                    "Quando você usa o app",
                    "Entregar a função principal do app e sincronizar entre aparelhos",
                    "Não"
                ),
                listOf(
                    "Avaliações, opiniões, emoções, marcação de spoiler e votos",
                    "Quando você participa da comunidade",
                    "Exibir a comunidade",
                    "Opiniões públicas e notas: sim, para usuários logados. Opiniões privadas: não"
                ),
                listOf(
                    "Denúncias que você faz",
                    "Quando você denuncia uma opinião",
                    "Moderação e ocultar, só para você, a opinião denunciada",
                    "Não"
                ),
                listOf(
                    "Histórico de busca e obras vistas recentemente",
                    "Ao pesquisar e abrir obras",
                    "Facilitar novas buscas",
                    "Não; fica só no aparelho"
                ),
                listOf(
                    "Cópia local de lista, progresso, perfil e dados das obras",
                    "Ao usar o app",
                    "Permitir o uso sem internet",
                    "Não; fica só no aparelho"
                )
            )
        ),
        BlocoLegal.Paragrafo(
            "O Seriando não lê nem grava sua senha: ao criar conta com e-mail e senha, ela é tratada apenas pelo " +
                "Firebase Authentication (Google). Contas de e-mail e senha só são ativadas depois que você confirma o " +
                "endereço pelo link enviado ao seu e-mail. Não coletamos sua localização, seus contatos nem dados de " +
                "pagamento. A foto da conta Google, " +
                "quando usada como avatar, é carregada a partir da própria conta Google e não é copiada para os nossos " +
                "bancos. A sua idade não é gravada: ela é calculada na hora do uso a partir da data de nascimento."
        ),
        BlocoLegal.Paragrafo("Usar o app como visitante não grava nenhum dado seu em nossos servidores."),

        BlocoLegal.Secao("3. Para que usamos e em qual base legal"),
        BlocoLegal.Tabela(
            cabecalhos = listOf("Finalidade", "Base legal (LGPD, art. 7º)"),
            linhas = listOf(
                listOf(
                    "Autenticar sua conta, manter lista, progresso, avaliações e opiniões",
                    "Execução do serviço que você solicita (inciso V)"
                ),
                listOf(
                    "Calcular sua idade e filtrar a classificação indicativa",
                    "Consentimento, ao informar a data de nascimento (inciso I); e, para menores, o melhor interesse da criança e do adolescente (art. 14)"
                ),
                listOf(
                    "Moderar opiniões denunciadas e proteger a comunidade",
                    "Legítimo interesse (inciso IX)"
                ),
                listOf(
                    "Manter a segurança e o funcionamento do serviço",
                    "Legítimo interesse (inciso IX)"
                )
            )
        ),
        BlocoLegal.Paragrafo(
            "Você pode retirar o consentimento a qualquer momento, excluindo a conta. Informar a data de nascimento é " +
                "opcional; sem ela, o app aplica a faixa restrita (até 12 anos)."
        ),

        BlocoLegal.Secao("4. Crianças e adolescentes"),
        BlocoLegal.Paragrafo(
            "Para reduzir a exposição de menores a conteúdo inadequado, o app oculta obras com classificação indicativa " +
                "do Brasil acima da idade calculada e bloqueia a tela de detalhes dessas obras. Visitantes e usuários sem " +
                "data de nascimento são tratados como menores, com acesso limitado a obras de até 12 anos."
        ),
        BlocoLegal.Paragrafo("Informamos o seguinte, com transparência:"),
        BlocoLegal.Lista(
            listOf(
                "A data de nascimento é informada por você e não é verificada. O app não é um mecanismo de verificação de idade.",
                "A data só pode ser informada e corrigida uma vez, para evitar trocas repetidas.",
                "Coletamos o mínimo possível de dados de menores e não os usamos para publicidade nem para perfis comerciais.",
                "Menores de 12 anos devem usar o app com ciência e acompanhamento de um responsável. " +
                    "Responsáveis podem pedir a exclusão dos dados da criança pelo contato indicado na seção 1."
            )
        ),

        BlocoLegal.Secao("5. Com quem compartilhamos"),
        BlocoLegal.Tabela(
            cabecalhos = listOf("Terceiro", "O que recebe", "Função"),
            linhas = listOf(
                listOf(
                    "Google (Firebase Authentication e Cloud Firestore)",
                    "Dados da sua conta e os dados descritos na seção 2 que são gravados na nuvem",
                    "Login, envio dos e-mails de confirmação e de redefinição de senha, e armazenamento na nuvem"
                ),
                listOf(
                    "TMDB (The Movie Database)",
                    "Consultas de catálogo feitas pelo app, como busca e identificador da obra, além do endereço IP do aparelho, como em qualquer acesso à internet",
                    "Dados de filmes, séries, elenco, classificação indicativa e streaming"
                ),
                listOf(
                    "JustWatch (via TMDB)",
                    "Nenhum dado nosso; é a fonte dos dados de streaming",
                    "Disponibilidade em serviços de streaming no Brasil"
                ),
                listOf(
                    "YouTube (Google)",
                    "Dados técnicos do acesso, ao reproduzir um trailer",
                    "Exibição de trailers"
                )
            )
        ),
        BlocoLegal.Paragrafo(
            "Enviamos à TMDB apenas o necessário para a consulta; não enviamos seu e-mail, apelido, lista nem opiniões. " +
                "Não vendemos dados pessoais nem os usamos para publicidade. O Seriando não é endossado nem certificado pela TMDB."
        ),
        BlocoLegal.Paragrafo(
            "Seu apelido, avatar, notas e opiniões públicas ficam visíveis para outros usuários logados. Antes de " +
                "publicar, lembre-se de que o conteúdo público pode ser lido por eles."
        ),

        BlocoLegal.Secao("6. Transferência internacional"),
        BlocoLegal.Paragrafo(
            "Os serviços do Google e da TMDB podem armazenar e tratar dados em servidores fora do Brasil. Essa " +
                "transferência é necessária para o funcionamento do serviço, conforme o art. 33 da LGPD, e depende das " +
                "garantias oferecidas por esses provedores."
        ),

        BlocoLegal.Secao("7. Por quanto tempo guardamos"),
        BlocoLegal.Lista(
            listOf(
                "Seus dados ficam guardados enquanto sua conta existir.",
                "Ao excluir a conta na aba Conta, apagamos seu perfil, sua lista, seu progresso, suas avaliações, " +
                    "suas opiniões (públicas e privadas) e os votos que você deu.",
                "Mantemos, como registro de moderação, as denúncias que você fez.",
                "Votos de outras pessoas nas suas opiniões ficam sem identificação visível, porque só cada pessoa pode apagar o próprio voto.",
                "Os dados guardados no aparelho (banco local) são apagados ao sair da conta e ao excluí-la.",
                "A foto de perfil própria, se você escolher uma, fica só no aparelho e é apagada ao sair da conta, ao " +
                    "excluí-la ou ao escolher outro avatar.",
                "O histórico de busca fica só no aparelho, não é apagado ao sair da conta e pode ser limpo por você " +
                    "pelos botões \"Limpar\" da tela de busca. Ele some se você desinstalar o app."
            )
        ),

        BlocoLegal.Secao("8. Uso sem internet e sincronização"),
        BlocoLegal.Paragrafo(
            "O app guarda uma cópia local da sua lista, do progresso e do perfil para funcionar sem internet, e " +
                "sincroniza com a nuvem quando a conexão volta. Se você sair da conta com alterações ainda não " +
                "sincronizadas, o app avisa quantas serão perdidas, porque o banco local é apagado no logout."
        ),

        BlocoLegal.Secao("9. Segurança"),
        BlocoLegal.Paragrafo(
            "Adotamos medidas como comunicação por HTTPS, regras de acesso no banco de dados para que cada pessoa " +
                "acesse apenas os próprios dados privados, validação dos campos gravados e limpeza dos dados locais ao " +
                "sair. Nenhum sistema é totalmente seguro. Em especial, o banco local do aparelho não é criptografado; " +
                "por isso, proteja seu aparelho com bloqueio de tela."
        ),
        BlocoLegal.Paragrafo(
            "Em caso de incidente de segurança que possa causar dano relevante, comunicaremos os usuários afetados e a " +
                "Autoridade Nacional de Proteção de Dados (ANPD), na forma da lei."
        ),

        BlocoLegal.Secao("10. Seus direitos"),
        BlocoLegal.Paragrafo(
            "Pela LGPD (art. 18), você pode pedir: confirmação de que tratamos seus dados; acesso; correção; " +
                "anonimização, bloqueio ou eliminação de dados desnecessários; portabilidade; informação sobre " +
                "compartilhamento; e a revogação do consentimento."
        ),
        BlocoLegal.Paragrafo(
            "Dentro do app, você pode editar apelido e avatar, escolher ou remover a foto própria, redefinir a senha " +
                "(conta de e-mail e senha), informar ou corrigir a data de nascimento (uma " +
                "correção), apagar suas avaliações, opiniões e votos, e excluir a conta e todos os dados. Para os demais " +
                "pedidos, escreva para $EMAIL_DE_CONTATO. Você também pode reclamar à ANPD (www.gov.br/anpd)."
        ),

        BlocoLegal.Secao("11. Alterações desta política"),
        BlocoLegal.Paragrafo(
            "Podemos atualizar esta política. A data de atualização fica no topo. Mudanças relevantes serão avisadas no app."
        )
    )
)
