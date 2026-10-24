# Seriando — Modelo de Ameaças (STRIDE) e Checklist de Segurança (OWASP MASVS)

Versão de 23/10/2026. Baseado em requisitos-seriando.md, arquitetura.md, modelagem-banco.md, diagrama-sequencia-sincronizacao.md e firestore.rules. Os itens que dependiam de conferência no código e nas configurações do projeto foram verificados em 24/10/2026 (AndroidManifest.xml, app/build.gradle.kts, regras de backup e busca no código-fonte); o resultado está marcado em cada item.

## 1. Escopo e ativos

Componentes analisados: app Android (Compose), banco local Room, preferências locais (DataStore e SharedPreferences), Sincronizador, Firebase Authentication, Cloud Firestore e API da TMDB.

| Ativo | Onde fica | Sensibilidade |
|---|---|---|
| E-mail e UID da conta Google | Firebase Auth e `usuarios/{uid}` | Pessoal |
| Data de nascimento (AAAA-MM-DD) | `usuarios/{uid}` e Room (`perfil`) | Pessoal; envolve controle de idade e possível dado de menor |
| Apelido e avatar | `perfisPublicos/{uid}` e Room | Público para usuários logados |
| Lista, status e progresso | `usuarios/{uid}/series` e Room | Pessoal (hábitos de consumo) |
| Opinião privada | `usuarios/{uid}/opinioesPrivadas` | Pessoal, só o dono lê |
| Notas, opiniões públicas e votos | `avaliacoes/...` | Público para usuários logados |
| Denúncias | `denuncias/{id}` | Restrito ao administrador |
| Histórico de busca | SharedPreferences | Pessoal, só no aparelho |
| Chave da API da TMDB | `BuildConfig` no APK | Baixa; cota de uso |

Atores de ameaça considerados: usuário mal-intencionado com o app instalado, pessoa com acesso físico ao aparelho, atacante com APK adulterado ou chamadas diretas às APIs, e usuário menor de idade tentando acessar conteúdo acima da faixa etária.

Fronteiras de confiança: aparelho, Firebase (Google Cloud) e TMDB. O app cliente nunca é confiável; só as regras do Firestore são impostas no servidor.

## 2. Análise STRIDE

Legenda de risco residual: Baixo, Médio, Alto. "Aceito" significa que o risco foi reconhecido e não será tratado nesta entrega.

### 2.1 Spoofing (falsificação de identidade)

| ID | Ameaça | Mitigação existente | Risco residual |
|---|---|---|---|
| S1 | Alguém grava dados na conta de outro usuário | Regras exigem `request.auth.uid == uid` em toda a subárvore do usuário (RNF01) | Baixo |
| S2 | Forjar o apelido ou o avatar de outra pessoa dentro de uma opinião | `apelidoOk` e `avatarOk` comparam com `perfisPublicos` do próprio autor; `autorUid` precisa igualar o id do documento | Baixo |
| S3 | Denunciar "em nome de" outra pessoa | `denuncianteUid == request.auth.uid` nas regras | Baixo |
| S4 | Roubo de sessão por conta Google comprometida | Login delegado ao Google (Credential Manager); o app não guarda senha | Aceito (fora do controle do app) |
| S5 | Excluir a conta sem ser o dono, com aparelho desbloqueado | Reautenticação antes da exclusão (RF29) | Baixo |
| S6 | Data de nascimento falsa para ver conteúdo acima da idade | Nenhuma verificação possível; a data é informada pelo próprio usuário | Alto, aceito (ver 4.1) |

### 2.2 Tampering (adulteração)

| ID | Ameaça | Mitigação existente | Risco residual |
|---|---|---|---|
| T1 | Alterar votos, notas ou opiniões de outros | Só o autor escreve a própria opinião; só o votante escreve o próprio voto (RN06, RNF04) | Baixo |
| T2 | Votar na própria opinião ou votar mais de uma vez | Id do voto é o UID do votante; regra `votanteUid != autorUid` (RN04) | Baixo |
| T3 | Gravar campos extras ou valores fora do formato | `keys().hasOnly(...)` e validação de tipo e tamanho em todos os documentos | Baixo |
| T4 | Trocar a data de nascimento repetidas vezes | Trava de uma única correção (`aniversarioAlterado`), imposta pelas regras (RN17) | Baixo para troca repetida; Alto para data falsa inicial (S6) |
| T5 | Relógio do celular alterado para fazer uma alteração antiga vencer a sincronização | Nenhuma; `atualizadoEm` vem do relógio local (RN23) | Médio, aceito: o dano fica restrito aos dados do próprio usuário |
| T6 | App adulterado ou chamadas diretas ao Firestore contornam a lógica do cliente | As regras valem no servidor e independem do app | Baixo para dados; ver 4.1 para o filtro de idade |
| T7 | Lista de 24 emoções não validada no servidor | Só o limite de 6 é imposto; um cliente adulterado grava ids inventados | Baixo, aceito: o app ignora ids desconhecidos (verificado: `OpiniaoCard` só exibe emoções encontradas em `EMOCOES_DISPONIVEIS`) |
| T8 | Campo `data` da opinião falsificado | Regra só exige tipo inteiro; vem do relógio do celular | Baixo, aceito: afeta apenas a ordenação |
| T9 | Banco Room alterado por usuário com root | Sem criptografia nem verificação de integridade; o Sincronizador conferiria a nuvem, mas uma alteração local mais recente vence | Baixo, aceito: só atinge a conta do próprio usuário |

### 2.3 Repudiation (repúdio)

| ID | Ameaça | Mitigação existente | Risco residual |
|---|---|---|---|
| R1 | Autor nega ter publicado uma opinião ofensiva | Opinião gravada com `autorUid`; denúncias guardam autor, obra, denunciante e motivo | Baixo |
| R2 | Denúncia apagada ou alterada | `denuncias` não permite leitura, edição ou exclusão pelo app (somente criação) | Baixo |
| R3 | Opinião denunciada é apagada pelo autor antes da moderação | A denúncia guarda ids, mas não o texto | Médio. Sugestão: copiar o texto na denúncia, ou aceitar o risco |
| R4 | Sem registro de quem fez cada operação | Não há log de auditoria além do console do Firebase | Aceito (escala do projeto) |

### 2.4 Information disclosure (vazamento de informação)

| ID | Ameaça | Mitigação existente | Risco residual |
|---|---|---|---|
| I1 | Opinião privada lida por terceiros | Fica em `usuarios/{uid}/opinioesPrivadas`, fora da coleção pública (RN12) | Baixo |
| I2 | Leitura da data de nascimento e do e-mail de outras pessoas | `usuarios/{uid}` só é legível pelo dono (RNF03) | Baixo |
| I3 | Qualquer usuário logado lê todas as opiniões e perfis públicos, inclusive por consulta em todas as obras | Intencional (comunidade); a regra `collectionGroup` só alcança `opinioes`, não `opinioesPrivadas` nem `votos` de outras coleções | Baixo; usuários precisam saber que apelido, avatar e opiniões públicas são visíveis (informar na Política) |
| I4 | Dados pessoais no aparelho legíveis com acesso físico ou backup do Android | Banco apagado no logout e na exclusão (RN25); e-mail não é guardado no Room (RNF08); porém o Room não é criptografado | Médio. Verificado: `allowBackup="true"` e os arquivos `backup_rules.xml` e `data_extraction_rules.xml` eram os modelos vazios do Android, então o backup automático incluía o banco Room e as preferências. Corrigido em 24/10/2026: `allowBackup="false"` (RNF16). Risco residual: Baixo |
| I5 | Histórico de busca permanece após o logout | Decisão documentada (RN29): só no aparelho, apagável pelo usuário | Baixo, aceito; informar na Política |
| I6 | Chave da TMDB extraída do APK | Fora do repositório (RNF02), mas fica embutida no APK compilado | Baixo, aceito: dado público de catálogo; o risco é consumo da cota |
| I7 | `google-services.json` e chaves do Firebase expostos | Chaves do Firebase não são segredos; a proteção está nas regras | Baixo. Recomendação: ativar Firebase App Check |
| I8 | Dados de streaming e catálogo interceptados na rede | HTTPS em todas as comunicações (RNF05) | Baixo. Verificado: o manifesto não ativa `usesCleartextTraffic` (com targetSdk 37 o padrão é bloquear HTTP) e não há URL `http://` no código |
| I9 | Votos órfãos de terceiros ficam no banco após a exclusão da conta do autor da opinião | Documentado (RN19): não identificáveis por outra pessoa, mas o voto de uma pessoa permanece | Baixo, aceito; informar na Política |
| I10 | Logs com dados pessoais | Verificado: os `Log.d/e/w` do código registram apenas mensagens de erro, contagens e nomes de obras; não há e-mail, UID nem data de nascimento. Em contrapartida, o build release não está otimizado (R8 desativado) e os logs continuam nele | Baixo. Recomendação: manter assim e não registrar dados pessoais nos logs |

### 2.5 Denial of service (negação de serviço)

| ID | Ameaça | Mitigação existente | Risco residual |
|---|---|---|---|
| D1 | Spam de denúncias, votos ou opiniões inflando leituras e escritas | Limites de tamanho e de campos; sem limite de taxa | Médio, aceito. Sugestão: App Check e alertas de orçamento no Firebase |
| D2 | Esgotar a cota de leituras do Firestore | Recebimento completo só roda ao abrir o app, no login e quando a internet volta (RN28) | Baixo |
| D3 | Esgotar a cota da TMDB | Cache em memória e no Room (RNF13) e semáforo de 10 consultas paralelas | Baixo |
| D4 | Usuário sem internet fica sem funcionalidades | Modo offline cobre lista, progresso e perfil; comunidade e catálogo exigem rede (RN24) | Aceito, com aviso em tela (RF62) |
| D5 | Falha do Firestore ou da TMDB deixa telas vazias | Estados de erro e botão "Tentar de novo" (RNF06) | Baixo |

### 2.6 Elevation of privilege (elevação de privilégio)

| ID | Ameaça | Mitigação existente | Risco residual |
|---|---|---|---|
| E1 | Usuário comum lê ou grava denúncias | Regras negam leitura, edição e exclusão; só criação | Baixo |
| E2 | Visitante grava no Firestore ou lê a comunidade | Regras exigem login; o app não grava dados do visitante (RN08, RN18) | Baixo |
| E3 | Burlar a trava de Detalhes (RF51) abrindo a obra por outro caminho | Detalhes consulta `obraPermitida` mesmo quando aberta por outra rota | Baixo para uso normal; ver 4.1 |
| E4 | Regra permissiva por engano abre o banco | Tudo negado por padrão; regras versionadas em `firestore.rules` | Baixo. Recomendação: testar as regras com o emulador do Firebase antes de alterá-las |
| E5 | Minha Lista mostra obras acima da idade | O bloqueio só ocorre ao abrir os detalhes | Baixo (a obra foi adicionada pelo próprio usuário); decisão pendente |

## 3. Resumo dos riscos mais relevantes

1. Data de nascimento falsa e app adulterado contornam o controle de idade (S6, T6): risco Alto, parcialmente aceito.
2. Dados locais não criptografados e (I4): risco Médio; o backup do Android foi desativado, resta a falta de criptografia do arquivo.
3. Ausência de limite de taxa e de App Check (D1): risco Médio, aceito.
4. Relógio do celular decide conflitos de sincronização (T5): risco Médio, aceito.
5. Texto das denúncias não preservado (R3): risco Médio, decisão pendente.

## 4. Riscos com tratamento específico

### 4.1 Controle de idade (RF50, RF51)

O controle depende de três coisas fora do alcance do servidor: a data informada pelo usuário, a classificação indicativa vinda da TMDB e o filtro aplicado dentro do app. Por isso:

- Uma data falsa informada de início não é detectável. A trava de correção única só impede trocas repetidas.
- Um app adulterado ou chamadas diretas à TMDB ignoram o filtro. O catálogo da TMDB é público; o app não é uma barreira de acesso a ele.
- Na dúvida, o app restringe: visitante, data ausente ou data inválida valem 12 anos (RN16); obra sem classificação no Brasil exige 18+ (RN14); falha de consulta vale como "sem classificação" (RN31).
- Decisão: o risco é parcialmente aceito. O controle reduz a exposição acidental de menores a conteúdo impróprio dentro do app, mas não é uma verificação de idade. A Política de Privacidade e os Termos de Uso informam isso ao usuário.

### 4.2 Dados pessoais no aparelho (modo offline, RF33)

O Room guarda lista, progresso, perfil (apelido, avatar, data de nascimento) e cópia das respostas da TMDB. Não guarda e-mail. O banco é apagado no logout e na exclusão de conta, e o app registra o dono do banco para apagá-lo se sobrar para outro usuário (RN25). Pontos em aberto: criptografia do arquivo `seriando.db` (não implementada) e regras de backup do Android (corrigido em 24/10/2026: `allowBackup="false"`; antes, o banco entrava no backup automático).

### 4.3 Limitações conhecidas das regras do Firestore

- A data de nascimento não pode ser comparada com a data de hoje nas regras; a verificação de data futura fica só no app.
- A lista de 24 emoções não é validada no servidor, apenas o limite de 6.
- O campo `data` das opiniões e o `atualizadoEm` vêm do relógio do aparelho.
- Votos de outras pessoas nas opiniões de quem excluiu a conta ficam órfãos, porque só cada autor pode apagar o próprio voto (RN19).
- Não há limite de taxa por usuário.

### 4.4 Dados de crianças e adolescentes

O controle de idade implica tratar a data de nascimento, e o app pode ser usado por menores. O tratamento está descrito na Política de Privacidade (seção sobre crianças e adolescentes). Medidas já existentes: coleta mínima (RNF08), data opcional e privada (RNF03), idade não armazenada (RN13), opiniões privadas fora da coleção pública e exclusão de conta dentro do app (RF29).

## 5. Checklist OWASP MASVS

Situação: Atende, Parcial, Não atende.

| Categoria | Controle | Situação | Observação |
|---|---|---|---|
| STORAGE | Dados sensíveis não ficam em local público ou em logs | Atende | Logs sem e-mail, UID ou data de nascimento (I10) |
| STORAGE | Dados locais protegidos | Parcial | Room sem criptografia; apagado no logout; e-mail não guardado |
| STORAGE | Exclusão dos dados locais ao sair | Atende | RN25 |
| STORAGE | Backup do sistema não expõe dados sensíveis | Atende | `allowBackup="false"` desde 24/10/2026 (RNF16); antes estava ativo com regras vazias |
| CRYPTO | Sem criptografia própria nem segredos hardcoded | Atende | Chave da TMDB fora do repositório; sem criptografia caseira |
| AUTH | Autenticação delegada a provedor confiável | Atende | Google via Credential Manager |
| AUTH | Autorização imposta no servidor | Atende | Regras do Firestore por UID |
| AUTH | Reautenticação em ação sensível | Atende | Exclusão de conta |
| NETWORK | Tráfego só por TLS | Atende | RNF05; manifesto sem `usesCleartextTraffic` e sem URL `http://` |
| NETWORK | Validação de certificado e pinning | Parcial | Validação padrão do Android; sem pinning (aceito) |
| PLATFORM | Componentes exportados e permissões mínimas | Atende | Só `MainActivity` (launcher) é exportada; permissões: INTERNET e ACCESS_NETWORK_STATE |
| PLATFORM | Compartilhamento seguro | Atende | RF73 compartilha só texto e link público da TMDB |
| PLATFORM | WebViews | Atende | Não há WebView; o trailer abre o YouTube por Intent (`ACTION_VIEW`) |
| CODE | Dependências atualizadas | Atende | BoM do Firebase duplicado removido em 24/10/2026 (fica só a 34.19.0). Build release sem otimização (R8) e `versionName` 1.0 |
| CODE | Tratamento de erros e entrada | Atende | RNF06; regras validam campos |
| RESILIENCE | Detecção de root, ofuscação, anti-adulteração | Não atende | Fora do escopo; ver 4.1 |
| PRIVACY | Coleta mínima e finalidade documentada | Atende | RNF08; Política de Privacidade |
| PRIVACY | Exclusão de conta e dados | Atende | RF29, RN19 |
| PRIVACY | Transparência sobre dados e terceiros | Atende | Política de Privacidade e Termos de Uso |
| PRIVACY | Tratamento de dados de menores | Parcial | Controle de idade e coleta mínima; sem verificação de idade |

## 6. Recomendações priorizadas

1. Feito em 24/10/2026: backup automático desativado (`allowBackup="false"`, RNF16) e BoM do Firebase duplicado removido.
2. Opcional: ativar R8 no build release e alinhar `versionName` com a versão do app.
3. Ativar Firebase App Check e alertas de orçamento no Firestore.
4. Decidir se o texto da denúncia será copiado para o registro (R3).
5. Decidir se a Minha Lista oculta obras acima da idade (E5).
6. Testar as regras com o emulador do Firebase.
7. Considerar criptografia do Room (SQLCipher) apenas se o app for distribuído para público amplo.
