# GymCats

## Sumário

1. [Introdução](#1-introdução)
2. [Problema e motivação](#2-problema-e-motivação)
3. [Solução proposta](#3-solução-proposta)
4. [Stack e justificativas](#4-stack-e-justificativas)
5. [Arquitetura](#5-arquitetura)
6. [Banco de dados local](#6-banco-de-dados-local)
7. [Telas e fluxo de navegação](#7-telas-e-fluxo-de-navegação)
8. [Autenticação e sessão](#8-autenticação-e-sessão)
9. [Treino](#9-treino)
10. [Integração com a API de exercícios](#10-integração-com-a-api-de-exercícios)
11. [Ciclo menstrual e sintomas](#11-ciclo-menstrual-e-sintomas)
12. [Foto de progresso](#12-foto-de-progresso)
13. [Tela de progresso e analytics](#13-tela-de-progresso-e-analytics)
14. [Notificações](#14-notificações)
15. [Compartilhamento](#15-compartilhamento)
16. [Backend](#16-backend)
17. [Decisões técnicas](#17-decisões-técnicas)
18. [Dificuldades enfrentadas](#18-dificuldades-enfrentadas)
19. [Como rodar](#19-como-rodar)
20. [Testes](#20-testes)
21. [Checklist de requisitos](#21-checklist-de-requisitos)
22. [Conclusão](#22-conclusão)
23. [Referências](#23-referências)

---

## 1. Introdução

GymCats é um aplicativo Android desenvolvido para mulheres que treinam musculação e querem acompanhar treino, ciclo menstrual e evolução física em um único lugar. A ideia não é criar mais um diário de treino nem mais um rastreador de ciclo, mas conectar as duas dimensões: performance e o que o corpo está vivendo em cada fase do mês.

O projeto foi desenvolvido como Atividade Ponderada 4, com o objetivo de construir uma aplicação móvel funcional que atendesse a um problema real com múltiplas telas, backend próprio, banco de dados, consumo de API externa, notificações, compartilhamento e uso de hardware do dispositivo.

A base científica que motivou o GymCats é clara: pesquisadores do British Journal of Sports Medicine acompanharam mais de 6.800 mulheres ativas e constataram que sintomas menstruais afetam diretamente a disponibilidade para treinar (BRUINVELS et al., 2021). Esse impacto varia de mulher para mulher — o que reforça que dados individuais importam mais do que recomendações genéricas (MCNULTY et al., 2020) — e se manifesta desde os primeiros anos de vida esportiva (BROWN et al., 2025). Um app que separa treino de ciclo ignora uma parte relevante da experiência de quem treina.

**Vídeo de demonstração:** [Clique aqui](https://drive.google.com/file/d/195yvhjGhL9X9WgvSRayYuznXIkKpldsB/view?usp=sharing)

---

## 2. Problema e motivação

Apps de treino não falam sobre ciclo. Apps de ciclo não falam sobre treino. A usuária acaba dividindo sua rotina entre dois ou mais lugares, sem conseguir enxergar a relação entre o que sente e como performa.

Essa separação tem um custo real. O British Journal of Sports Medicine acompanhou mais de 6.800 mulheres ativas e constatou que sintomas como fadiga, alterações de humor e cólicas aparecem com alta frequência mesmo em quem treina regularmente — e que quanto mais intensos esses sintomas, maior a chance de modificar o treino, reduzir carga ou faltar à sessão (BRUINVELS et al., 2021). O efeito sobre o desempenho não é igual para todas: pode haver uma pequena queda média no início da menstruação, mas a variação individual é grande, e recomendações genéricas não resolvem (MCNULTY et al., 2020). E esse impacto começa cedo: meninas entre 10 e 16 anos já relatam faltar a treinos e competições por causa da menstruação (BROWN et al., 2025).

O que faltava era um produto que tratasse treino e ciclo como parte do mesmo contexto, não como duas listas separadas.

---

## 3. Solução proposta

O GymCats organiza treino ativo, registro de sintomas, histórico de evolução de carga e fotos de progresso em um único fluxo. Ao fechar um treino, a usuária registra como se sentiu — energia, disposição, humor, cólica, sono. Ao longo do tempo, a tela de progresso conecta esses registros e permite identificar padrões: em quais fases o rendimento cai, em quais a disposição aumenta, como a carga evoluiu por exercício.

O app exibe a fase atual do ciclo (menstrual, folicular, ovulatória, lútea) com uma dica contextual adaptada àquela fase, e usa essas informações para contextualizar os dados históricos na tela de progresso.

---

## 4. Stack e justificativas

### Android

| Tecnologia | Motivo |
|---|---|
| **Kotlin** | Linguagem oficial Android, null safety, corrotinas nativas |
| **Jetpack Compose + Material 3** | UI declarativa, menos boilerplate, integração nativa com ViewModel e StateFlow |
| **Navigation Compose** | Navegação tipada com backstack gerenciado, compatível com Hilt |
| **Hilt** | DI por anotação, integração com ViewModel e WorkManager, reduz acoplamento entre camadas |
| **Room (v3)** | ORM Android com suporte a migrações explícitas, queries assíncronas com Flow e suporte multi-conta |
| **DataStore** | Preferências e estado de sessão que não são domínio — separado do Room intencionalmente |
| **WorkManager** | Tarefas em background que sobrevivem ao processo ser encerrado, ideal para notificações de ciclo e treino |
| **Retrofit + OkHttp** | HTTP declarativo, interceptor para JWT automático, logging para debug |
| **Coil** | Carregamento de imagens local compatível com Compose |
| **Biometric API** | Acesso à autenticação biométrica (digital/face) com fallback gerenciado pelo sistema |

### Backend

| Tecnologia | Motivo |
|---|---|
| **FastAPI** | Rápido de implementar, tipagem com Pydantic, documentação automática |
| **Uvicorn** | Servidor ASGI leve, recarregamento automático em desenvolvimento |
| **HTTPX** | Cliente HTTP assíncrono para chamadas à ExerciseDB |
| **python-jose** | Geração e validação de JWT (HS256) |
| **deep-translator** | Tradução das instruções de exercício via Google Translate |

---

## 5. Arquitetura

O projeto segue arquitetura em camadas (Clean Architecture simplificada) com MVVM na camada de apresentação.

```
┌──────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│                                                              │
│  LoginScreen  OnboardingScreen  HomeScreen  WorkoutScreen    │
│  CycleLogScreen  PhotoScreen  ProgressScreen  ProfileScreen  │
│                                                              │
│  LoginVM  OnboardingVM  HomeVM  WorkoutVM  CycleLogVM        │
│  PhotoVM  ProgressVM  ExerciseProgressionVM  ProfileVM       │
└───────────────────────┬──────────────────────────────────────┘
                        │ StateFlow / UiState
┌───────────────────────▼──────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│                                                              │
│  CyclePhase (enum: MENSTRUAL | FOLICULAR |                   │
│              OVULATORIA | LUTEA)                             │
│                                                              │
│  GetCyclePhaseUseCase     GetExerciseProgressionUseCase      │
└───────────────────────┬──────────────────────────────────────┘
                        │ suspend functions / Flow
┌───────────────────────▼──────────────────────────────────────┐
│                       DATA LAYER                             │
│                                                              │
│  Repositories:                                               │
│    WorkoutRepository   AccountRepository   UserRepository    │
│    CycleRepository     ExerciseRepository  ProgressRepository│
│                                                              │
│  LOCAL (Room v3)          REMOTE (Retrofit)                  │
│    AccountDao               GymCatsApi (interface)           │
│    UserProfileDao           ExerciseRemoteDataSource         │
│    WorkoutDao               AuthInterceptor (OkHttp)         │
│    ExerciseLogDao           TokenManager                     │
│    CycleLogDao                                               │
│    ProgressPhotoDao       SESSION (DataStore)                │
│                             SessionManager                   │
└──────────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────────────┐
│                    INFRASTRUCTURE                            │
│                                                              │
│  DI: NetworkModule  DatabaseModule  AppModule                │
│                                                              │
│  Workers: CycleReminderWorker  WorkoutReminderWorker         │
│           SymptomsReminderWorker                             │
│                                                              │
│  Utils: NotificationHelper  BiometricHelper  ShareHelper     │
│         ExerciseTranslator  DateUtils  PasswordSecurity      │
└──────────────────────────────────────────────────────────────┘
```

### Padrão de estado

Cada ViewModel expõe um `UiState` como `StateFlow`. A tela coleta o fluxo e recompõe apenas o que mudou. Eventos pontuais (erros, navegação) são emitidos como `SharedFlow` para evitar re-entrega no recompose.

### WorkManager + Hilt

O `WorkManager` foi configurado manualmente via `Configuration.Provider` no `GymCatsApp` porque o Hilt precisa injetar dependências nos Workers. O `InitializationProvider` automático foi desabilitado no manifest para que a configuração customizada com `HiltWorkerFactory` seja usada. Novos Workers devem usar `@HiltWorker` + `@AssistedInject`.

---

## 6. Banco de dados local

Room versão 2. Seis entidades com relações de chave estrangeira:

```
accounts
  ├── id (PK autoincrement)
  ├── email (unique)
  ├── phone (unique)
  └── password

user_profile
  ├── accountId (PK, FK → accounts)
  ├── name
  ├── goal
  ├── lastPeriodDate
  ├── cycleLength (dias)
  ├── periodLength (dias)
  ├── preferredTrainingDays
  ├── preferredTrainingHour
  └── notificationsEnabled

workouts
  ├── id (PK autoincrement)
  ├── accountId (FK → accounts)
  ├── name
  ├── date (YYYY-MM-DD)
  ├── durationMinutes
  ├── cyclePhase             ← fase no momento da criação
  ├── notes
  └── isOpen (boolean)

exercise_logs
  ├── id (PK autoincrement)
  ├── workoutId (FK → workouts, CASCADE delete)
  ├── exerciseName
  ├── exerciseApiId
  ├── muscleGroup
  ├── sets
  ├── reps
  └── weight (float, kg)

cycle_logs
  ├── id (PK autoincrement)
  ├── accountId (FK → accounts)
  ├── date
  ├── energyLevel (1–5)
  ├── disposition (1–5)
  ├── mood (Exausta | Cansada | Neutra | Animada | Eufórica)
  ├── cramps (boolean)
  ├── sleepQuality (1–5)
  ├── notes
  └── cyclePhase

progress_photos
  ├── id (PK autoincrement)
  ├── accountId (FK → accounts)
  ├── imagePath (caminho absoluto)
  ├── date
  ├── workoutId (nullable, FK → workouts)
  └── notes
```

### Migrações

**1 → 2 (principal):** o fluxo foi projetado inicialmente para uma única usuária por aparelho. Quando surgiu a necessidade de suportar múltiplas contas no mesmo dispositivo — para testes, demo e casos reais de compartilhamento — foi necessário criar a tabela `accounts`, adicionar `accountId` em todas as tabelas e reestruturar `user_profile` para usar `accountId` como chave primária. Dados preexistentes foram vinculados à primeira conta criada para não perder histórico.

---

## 7. Telas e fluxo de navegação

```
[Login] ──── conta nova? ──→ [Onboarding (3 passos)]
   │                               │
   └───── conta existente ─────────┘
                                   │
                               [Home]
                            ┌──────┴──────┐
                    [Iniciar treino]    [Ver progresso]
                            │                │
                       [Workout]         [Progress]
                            │                │
                    (fechar treino)    [ExerciseProgression]
                            │
                       [CycleLog]
                            │
                     [PhotoCapture]
                            │
                         [Home] ←─────────────────┐
                                                   │
                        [Profile] ─── logout ──→ [Login]
```

| Tela | Função |
|---|---|
| **Login** | Cadastro de conta ou entrada com email/telefone + senha, atalho biométrico se configurado |
| **Onboarding** (3 steps) | Nome e objetivo → dados do ciclo → dias e horário de treino |
| **Home** | Fase atual, saudação, treinos do mês, check-in de hoje, último treino, CTAs |
| **Workout** | Treino aberto com timer, CRUD de exercícios, busca na API via bottom sheet |
| **CycleLog** | Registro de sintomas ao fechar treino: energia, disposição, sono, cólica, humor |
| **PhotoCapture** | Captura de foto de progresso com câmera nativa, vinculada ao treino atual |
| **Progress** | Analytics históricos: filtros por período, gráficos, fotos, botão de compartilhamento |
| **ExerciseProgression** | Evolução de carga de um exercício específico ao longo do tempo |
| **Profile** | Edição de perfil, preferências de treino e ciclo, logout |

---

## 8. Autenticação e sessão

### Fluxo

1. A usuária cria conta com email/telefone e senha, ou entra em conta existente.
2. O `SessionManager` (DataStore) persiste o `accountId` autenticado.
3. Se é o primeiro acesso, o app navega para o onboarding antes da home.
4. Opcionalmente, a usuária pode registrar a biometria como atalho para a conta atual.

### Decisão: biometria como atalho, não como identidade

No desenho inicial, a biometria funcionava como desbloqueio do app, sem estar atrelada a uma conta específica. Em dispositivos com múltiplas contas, isso gerava contaminação: a biometria desbloqueava a última conta ativa, não necessariamente a da usuária do dedo. A solução foi vincular o `biometricAccountId` explicitamente à conta que registrou a biometria, tornando-a um atalho de login dessa conta, e não um bypass de autenticação.

### SessionManager (DataStore)

- `authenticatedAccountIdFlow`: conta logada no momento
- `lastAccountIdFlow`: última conta usada (para biometria)
- `biometricAccountIdFlow`: conta vinculada ao sensor biométrico do aparelho

### Hashing de senha

As senhas são armazenadas com PBKDF2WithHmacSHA256 (120.000 iterações, salt aleatório de 16 bytes, chave de 256 bits). O formato gravado no banco é `pbkdf2$iterações$salt_base64$hash_base64`. O `PasswordSecurity.verify()` aceita senhas em texto plano como fallback para compatibilidade com contas criadas antes da migração. O `PasswordSecurity.validateStrength()` exige mínimo de 8 caracteres, letra maiúscula, minúscula e número — a mesma regra é exibida na tela de cadastro antes do campo de confirmação de senha.

O `AccountDao` expõe `updatePassword(accountId, password)` para suportar a troca de senha sem recriar a conta. A tela de login exibe o botão "Esqueci minha senha", que atualmente informa a usuária que a recuperação por código será implementada em passo futuro.

### Por que local e não remoto

A autenticação é local. As contas e os dados ficam no dispositivo. A única comunicação com o backend é para obter tokens de acesso à API de exercícios. Essa decisão foi intencional para simplificar a arquitetura dentro do escopo do projeto, mas o caminho natural de evolução seria autenticação remota com sincronização de dados.

---

## 9. Treino

O fluxo de treino é o núcleo operacional do app.

### Ciclo de vida de um treino

1. A usuária informa o nome do treino e o inicia. O app cria uma entidade `Workout` com `isOpen = true`, a data atual e a fase do ciclo calculada no momento.
2. Durante o treino, a usuária adiciona exercícios — manualmente ou via busca na API. O timer conta o tempo decorrido desde a criação.
3. Ao fechar o treino, o app calcula `durationMinutes`, seta `isOpen = false` e navega para o CycleLog.
4. Após o CycleLog, o app oferece captura de foto antes de retornar à home.

### Adição de exercícios via API

Quando a usuária seleciona um exercício da API, o app não o salva imediatamente. Ele abre o formulário de adição com os campos de nome e grupo muscular pré-preenchidos, mas séries, reps e carga ficam em branco para preenchimento. Isso evita que exercícios sejam gravados com carga `0.0` e distorçam os gráficos de progressão. Só após confirmar os valores o exercício é persistido como `ExerciseLog`.

### Por que `cyclePhase` é armazenada no `Workout`

A fase do ciclo é calculada a partir da data da última menstruação no momento em que o treino é criado. Gravar a fase no `Workout` é um snapshot: garante que o histórico reflita o que era verdade naquele dia, mesmo que a usuária atualize a data do ciclo posteriormente. Recalcular a fase na leitura introduziria inconsistência retroativa.

---

## 10. Integração com a API de exercícios

### Por que backend intermediário

A ExerciseDB (via RapidAPI) exige uma chave de API que não pode ser exposta no app Android. Além disso, a tradução das instruções para o português é feita no servidor, o que evita que o cliente precise de lógica adicional de localização. O backend também centraliza a autenticação: o app recebe um JWT próprio do backend e o usa nas chamadas protegidas.

### Fluxo de autenticação com o backend

```
App ──→ POST /auth/token { device_id } ──→ Backend
App ←── JWT (válido 30 dias)            ←── Backend
App ──→ GET /exercise/search/{nome}
        Authorization: Bearer <token>   ──→ Backend ──→ ExerciseDB
App ←── lista de exercícios             ←──────────────────────────
```

### Tradução sob demanda

A listagem retorna exercícios com nome e grupo muscular em inglês — sem tradução. A tradução das instruções detalhadas é feita apenas no endpoint `/exercise/detail/{id}`, chamado somente quando a usuária seleciona um exercício específico. Isso evita N chamadas ao Google Translate para uma lista que pode ter dezenas de itens.

### Endpoints do backend

| Método | Rota | Descrição |
|---|---|---|
| GET | `/health` | Health check |
| POST | `/auth/token` | Gera JWT para o device_id |
| GET | `/bodyparts` | Lista partes do corpo disponíveis |
| GET | `/targets` | Lista músculos-alvo |
| GET | `/equipment` | Lista equipamentos |
| GET | `/exercise/search/{name}` | Busca exercícios por nome |
| GET | `/exercise/bodypart/{bodypart}` | Filtra por parte do corpo |
| GET | `/exercise/target/{target}` | Filtra por músculo-alvo |
| GET | `/exercise/equipment/{equipment}` | Filtra por equipamento |
| GET | `/exercise/detail/{id}` | Detalhe com instruções traduzidas |
| GET | `/tips` | Dicas contextuais de treino |

---

## 11. Ciclo menstrual e sintomas

### Cálculo de fase

`GetCyclePhaseUseCase` calcula a fase a partir de dois dados do perfil: `lastPeriodDate` e `cycleLength`. A lógica é:

```
diasDesdeUltimaMenstruacao = (hoje - lastPeriodDate)
diaAtualNoCiclo = diasDesdeUltimaMenstruacao % cycleLength + 1

MENSTRUAL    → dias 1–5     "Priorize recuperação e treinos leves."
FOLICULAR    → dias 6–13    "Boa fase para treinos intensos e novos recordes."
OVULATÓRIA   → dias 14–16   "Pico de energia, aproveite para treinos pesados."
LÚTEA        → dias 17+     "Foque em técnica, mobilidade e bem-estar."
```

A home exibe a fase atual com a dica correspondente e um aviso quando `lastPeriodDate` está há mais de 60 dias sem atualização.

### Registro de sintomas (CycleLog)

Ao fechar um treino, o app navega automaticamente para o CycleLog. Os campos coletados são:

- **Energia** (1–5): slider
- **Disposição** (1–5): slider
- **Sono** (1–5): slider
- **Cólicas**: toggle (sim/não)
- **Humor**: chips com 5 opções categóricas — Exausta, Cansada, Neutra, Animada, Eufórica
- **Observações**: campo livre opcional

O humor foi modelado como categórico, não como escala numérica, porque os valores têm semântica distinta que uma média apagaria. Na tela de progresso, humor é exibido como distribuição percentual por fase, não como média.

### Por que check-in é vinculado ao treino

O CycleLog é criado ao final de um treino, não de forma independente. Essa decisão mantém o sintoma contextualizado: saber como a usuária se sentiu num dia genérico é menos informativo do que saber como ela se sentiu depois de treinar. O check-in diário independente poderia ser adicionado como evolução futura.

---

## 12. Foto de progresso

### Fluxo

1. Ao final do CycleLog, o app navega para `PhotoCaptureScreen`.
2. A usuária pode tirar uma foto (câmera nativa via intent), substituir por outra ou pular.
3. O arquivo é salvo em `context.filesDir/photo_<timestamp>.jpg`.
4. A entidade `ProgressPhoto` é persistida com o caminho absoluto, a data e o `workoutId`.

### Decisões técnicas

**FileProvider:** necessário para compartilhar o URI do arquivo com o app de câmera a partir do Android 7+. Configurado no manifest com `res/xml/file_paths.xml`.

**Caminho absoluto no banco:** gravar o caminho absoluto evita ambiguidade na leitura — o arquivo está exatamente onde o caminho diz. A alternativa de gravar só o nome e reconstruir o caminho na leitura quebrava quando o diretório de destino mudava entre versões do app.

**Substituição limpa:** ao capturar uma nova foto, o arquivo anterior é deletado antes da captura para evitar acúmulo de arquivos órfãos em `filesDir`.

### Dificuldades

O fluxo de câmera passou por três iterações. Na primeira versão, a foto era salva em cache (`cacheDir`) e o caminho expirava. Na segunda, o caminho era reconstruído a partir do nome do arquivo, e quebrava quando `filesDir` mudava. Na versão atual, o caminho absoluto é salvo diretamente na entidade. A tela de progresso mantém um fallback que tenta carregar pelo nome do arquivo se o caminho absoluto falhar, preservando compatibilidade com registros antigos.

---

## 13. Tela de progresso e analytics

A tela de progresso foi desenhada como espaço de análise histórica — deliberadamente separada da home, que foca no contexto do dia atual.

### Filtros de período

A usuária escolhe entre 1, 3 e 6 meses. Todos os dados da tela são recalculados a partir do filtro.

### Métricas exibidas

| Métrica | Tipo | Observação |
|---|---|---|
| Treinos realizados | Número | Total no período |
| Energia média | Número (1 decimal) | Média dos cycle_logs no período |
| Disposição média | Número | Idem |
| Sono médio | Número | Idem |
| Duração média de treino | Minutos | Média dos workouts com duração > 0 |
| Cólica por fase | Gráfico de barras | Média (0–1) por fase, escala 0–100% |
| Humor por fase | Barras empilhadas | Distribuição percentual de cada categoria por fase |
| Progressão por exercício | Lista clicável | Navegação para ExerciseProgressionScreen |
| Fotos do período | Grade | Fotos com data e treino vinculado |

### ExerciseProgressionScreen

Exibe a evolução de carga (kg) de um exercício ao longo do tempo em gráfico de linhas. A granularidade é por sessão de treino, não por data, porque a mesma data pode ter múltiplas séries com cargas diferentes.

### Compartilhamento

O botão de compartilhamento na tela de progresso gera um texto resumido com as métricas do período selecionado e usa o `ShareHelper` com a intent nativa do Android (`ACTION_SEND`), permitindo enviar pelo canal que a usuária escolher (WhatsApp, email, etc.).

---

## 14. Notificações

O app implementa três tipos de lembrete via WorkManager:

| Worker | Gatilho | Mensagem |
|---|---|---|
| `WorkoutReminderWorker` | Horário habitual de treino definido no onboarding | "Hora de registrar seu treino de hoje!" |
| `CycleReminderWorker` | Véspera da data prevista para a próxima menstruação | "Seu período está previsto para começar amanhã." |
| `SymptomsReminderWorker` | Após treino sem check-in de sintomas | Lembrete para registrar como se sentiu |

### Canal de notificação

Canal único `"GymCats"` com importância `IMPORTANCE_HIGH`. A importância não pode ser alterada após o primeiro registro do canal no sistema — se o canal for recriado com importância menor, o Android mantém a configuração original.

### Problema encontrado: notificação na bandeja sem pop-up

Em alguns dispositivos, notificações com importância padrão iam direto para a bandeja sem exibir pop-up. O ajuste foi aumentar a importância do canal para `IMPORTANCE_HIGH` e adicionar `setPriority(NotificationCompat.PRIORITY_HIGH)` no builder. Ambos são necessários: o canal define o teto de importância do sistema, e o builder define a prioridade individual da notificação.

---

## 15. Compartilhamento

O `ShareHelper` usa a intent nativa `Intent.ACTION_SEND` com `type = "text/plain"`. O sistema operacional exibe o chooser nativo, deixando a usuária escolher o app de destino (WhatsApp, email, etc.), sem dependência de SDK de terceiros.

O texto gerado contém:

- **Período** — rótulo do filtro selecionado (1, 3 ou 6 meses)
- **Treinos realizados** — total de treinos no período
- **Exercício mais frequente** — nome do exercício com mais registros
- **Energia média** — média dos cycle_logs no período, em escala de 1 a 5
- **Exercício que mais evoluiu carga** — nome do exercício com maior ganho de peso entre a primeira e a última sessão do período

---

## 16. Backend

### Estrutura

```
gymcats-api/
├── main.py            ← aplicação FastAPI, todas as rotas
├── requirements.txt
├── .env.example
└── .env               ← não versionado
```

### Variáveis de ambiente

```
SECRET_KEY=chave-secreta-para-jwt
EXERCISEDB_KEY=sua-chave-rapidapi
```

### Autenticação do backend

O backend não mantém estado de sessão. O token JWT é gerado a partir do `device_id` com validade de 30 dias. O `AuthInterceptor` no app bloqueia a thread de rede até obter o token (via `runBlocking`) e o injeta no header `Authorization: Bearer` de todas as requisições protegidas.

### Configuração da URL base

No emulador Android, `10.0.2.2` é o alias para o host. Em celular físico, essa rota não existe — é necessário o IP local da máquina na rede. A solução foi tornar a URL base configurável via `gradle.properties`:

```properties
# gradle.properties
API_BASE_URL=http://192.168.1.X:8000/
```

O valor padrão é `http://10.0.2.2:8000/` (emulador). A chave é lida no `build.gradle.kts` e injetada como `BuildConfig.API_BASE_URL`.

### WSL e rede local

Rodar o backend no WSL nem sempre é suficiente: o processo pode escutar em `127.0.0.1` do WSL sem estar acessível na rede local da máquina Windows. Em vários momentos durante o desenvolvimento foi necessário rodar o backend diretamente no Windows (PowerShell) para que o celular físico conseguisse alcançar o servidor. Firewall do Windows e isolamento de rede do WSL2 impactam diretamente.

---

## 17. Decisões técnicas

| Decisão | Alternativa descartada | Motivo da escolha |
|---|---|---|
| Room + DataStore separados | Tudo no Room | Room para domínio, DataStore para sessão/config — responsabilidades distintas |
| JWT local (device_id) | Autenticação remota completa | Simplifica o backend dentro do escopo do projeto; o dado real fica local |
| Contas locais, sem sincronização | Supabase / Firebase | Elimina dependência de serviço externo para dado sensível (histórico de ciclo) |
| Cálculo de fase no cliente | Endpoint de fase no backend | Funciona offline, é determinístico, sem latência |
| Tradução apenas no detalhe | Traduzir lista inteira | Reduz chamadas ao Google Translate; a usuária filtra antes de precisar da tradução |
| Backend intermediário para ExerciseDB | Chamar API diretamente no app | Protege a chave RapidAPI, centraliza autenticação e tradução |
| `cyclePhase` como snapshot no Workout | Recalcular na leitura | Preserva o histórico correto mesmo após atualização retroativa do perfil |
| Biometria vinculada à conta | Biometria como desbloqueio do app | Previne contaminação entre contas no mesmo dispositivo |
| WorkManager com `Configuration.Provider` | Inicialização automática do WorkManager | Necessário para que o Hilt injete dependências nos Workers via `HiltWorkerFactory` |
| Humor como enum categórico (5 valores) | Escala numérica | Semântica distinta entre categorias — uma média de humor não faz sentido |
| PBKDF2WithHmacSHA256 para senhas | Texto plano / MD5 | Segurança adequada para dado local sem overhead de servidor; fallback de texto plano garante compatibilidade com contas antigas |

---

## 18. Dificuldades enfrentadas

### Multi-conta e reorganização de autenticação

A arquitetura inicial assumia uma única usuária por dispositivo. Quando surgiu a necessidade de múltiplas contas — para testes com usuários diferentes e demo da aplicação — foi necessário criar a tabela `accounts`, adicionar `accountId` em todas as tabelas existentes, escrever migrações que preservassem os dados de quem já usava o app, reorganizar a biometria para ser por conta e não por dispositivo, e revisar toda a lógica de sessão. Foi a mudança de maior impacto no projeto.

### Backend acessível no celular físico

O emulador Android aceita `10.0.2.2` como alias para o host. O celular físico não. Descobrir isso durante o desenvolvimento foi frustrante: o app funcionava no emulador e parava de funcionar assim que conectado ao celular real. A solução — URL configurável via `gradle.properties` — é simples, mas chegar lá exigiu entender a interação entre WSL2, firewall do Windows e a rede local.

### Persistência de fotos

A primeira versão salvava fotos em `cacheDir`, que o sistema pode limpar a qualquer momento. A segunda versão gravava apenas o nome do arquivo e reconstruía o caminho na leitura, o que quebrou quando o path mudou entre versões. A versão final grava o caminho absoluto diretamente no banco. A tela de progresso mantém um fallback para compatibilidade com registros antigos.

### Instruções em inglês

A ExerciseDB retorna todas as instruções em inglês. A decisão de traduzir no backend (no endpoint de detalhe, não na listagem) foi tomada para equilibrar custo de chamadas e experiência do usuário. Implementar isso exigiu entender o fluxo completo: a usuária vê a lista em inglês, seleciona, e só então o detalhe traduzido é buscado.

### WorkManager com Hilt

A inicialização automática do WorkManager não funciona quando os Workers precisam de injeção de dependência via Hilt. A solução é desabilitar o `WorkManagerInitializer` no manifest e configurar manualmente o WorkManager com `HiltWorkerFactory` no `Application`. Não é óbvio pela documentação que os dois sistemas entram em conflito por padrão.

### Migrações de banco sem apagar dados

Apagar e recriar o banco a cada mudança de schema é tentador durante o desenvolvimento, mas inviável quando há dados reais ou um seed que precisa ser preservado. Escrever migrações explícitas com `ALTER TABLE`, `CREATE TABLE` e `INSERT INTO ... SELECT` obrigou a pensar nas transições de schema com cuidado e garantiu que os dados históricos fossem preservados.

### Notificações não aparecendo como pop-up

Notificações com importância padrão iam para a bandeja sem exibir pop-up em vários dispositivos de teste. Descobrir que o canal precisa ser criado com `IMPORTANCE_HIGH` antes do primeiro uso — e que o Android ignora tentativas de alterar a importância de um canal já registrado — tomou tempo de debug.

### Seed de debug

O app depende fortemente de dados históricos para demonstrar valor. Sem treinos, sem logs e sem fotos, a tela de progresso fica vazia e o problema que o app resolve não fica evidente. Criar um seed que populasse dados distribuídos no tempo, com progressão de carga, humor variado e fotos, foi essencial para as demonstrações.

---

## 19. Como rodar

### Pré-requisitos

- Android Studio Hedgehog ou superior
- Python 3.10+
- Chave RapidAPI com acesso à ExerciseDB

### Backend

**Windows (PowerShell):**
```powershell
cd gymcats-api
.\venv-win\Scripts\Activate.ps1
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

**Ubuntu / WSL:**
```bash
cd gymcats-api
source venv/bin/activate
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

**Instalar dependências (primeira vez):**
```bash
pip install -r requirements.txt
```

**Variáveis de ambiente** — crie `gymcats-api/.env` a partir do `.env.example`:
```
SECRET_KEY=qualquer-string-longa-e-aleatoria
EXERCISEDB_KEY=sua-chave-rapidapi
```

### App Android

1. Abra o projeto no Android Studio.
2. Se for usar **celular físico**, edite `gradle.properties` e defina o IP local da sua máquina:
   ```properties
   API_BASE_URL=http://192.168.1.X:8000/
   ```
3. Se for usar **emulador**, o valor padrão `http://10.0.2.2:8000/` já funciona.
4. Rode:
   ```
   .\gradlew.bat assembleDebug
   ```
   ou use o botão Run do Android Studio.

### Atenção com WSL

Se o backend estiver no WSL2 e o teste for em celular físico, o processo pode não estar acessível na rede local. Nesse caso, rode o backend diretamente no Windows (não no WSL) e desabilite o firewall para a porta 8000, ou crie uma regra de entrada.

---

## 20. Testes

**Unit tests** (JVM, sem dispositivo):
- `GetCyclePhaseUseCaseTest`: verifica cálculo de fase para cada janela do ciclo, incluindo dia 1, transições e ciclos personalizados.
- `DateUtilsTest`: formatação de datas ISO, cálculo de diferença em dias.

**Instrumented** (requer dispositivo ou emulador):
- `MainActivityTest`: smoke test — verifica que o app abre sem crash.

Para rodar os testes unitários:
```
.\gradlew.bat test
```

Para rodar o teste instrumented (com emulador ou dispositivo conectado via USB com depuração ativada):
```
.\gradlew.bat connectedAndroidTest
```
O resultado fica em `app/build/reports/androidTests/connected/index.html`.

---

## 21. Checklist de requisitos

| Requisito | Status | Implementação |
|---|---|---|
| Implementação mobile (Kotlin) | ✅ | Android nativo com Kotlin + Jetpack Compose |
| Mais de duas telas | ✅ | 9 telas: Login, Onboarding, Home, Workout, CycleLog, PhotoCapture, Progress, ExerciseProgression, Profile |
| Navegação funcional | ✅ | Navigation Compose com NavGraph tipado |
| Backend funcional | ✅ | FastAPI com endpoints de exercícios e autenticação por JWT |
| Banco de dados | ✅ | Room v3 local com 6 entidades e migrações explícitas |
| API externa | ✅ | ExerciseDB (RapidAPI) — busca, filtro e detalhe de exercícios |
| Sistema de notificações | ✅ | WorkManager com 3 workers: treino, ciclo e sintomas |
| Compartilhamento | ✅ | ShareHelper com `Intent.ACTION_SEND` — resumo de progresso |
| Hardware do celular | ✅ | Câmera (foto de progresso) + Biometria (digital/face no login) |
| Interface organizada | ✅ | Material 3, temas, navegação bottom bar, estados de carregamento e erro |
| Tratamento de erros | ✅ | Estados de loading/error nos ViewModels, feedback visual nas telas |
| Documentação mínima | ✅ | Este README |
| Código-fonte em repositório | ✅ | Repositório público |
| Vídeo de demonstração | ✅ | [Clique aqui](https://drive.google.com/file/d/195yvhjGhL9X9WgvSRayYuznXIkKpldsB/view?usp=sharing) |

---

---

## 22. Conclusão

O GymCats foi construído como uma solução integrada entre treino e ciclo menstrual, com foco em contexto real de uso e não apenas em armazenamento de dados. A base científica que motivou o projeto é clara: os sintomas menstruais afetam a disponibilidade para treinar (BRUINVELS et al., 2021), o impacto sobre o desempenho é individual e variável (MCNULTY et al., 2020), e essa dificuldade se manifesta desde as fases iniciais da vida esportiva (BROWN et al., 2025). Um aplicativo que trata treino e ciclo como dimensões separadas ignora uma parte relevante da experiência da usuária.

As decisões de produto e de engenharia seguiram a mesma linha: privilegiar o que faria sentido para a usuária final e manter a arquitetura suficientemente organizada para crescer sem fragilidade. Ao longo do desenvolvimento, vários ajustes foram necessários — separar login de perfil, tornar a biometria coerente com múltiplas contas, resolver a rede entre backend e celular físico, corrigir persistência de fotos, refinar o papel da home, consolidar a tela de progresso como espaço analítico e introduzir migrações de banco para preservar dados.

O resultado é um app funcional com backend de apoio, arquitetura clara em camadas, persistência local consistente e fluxo de treino conectado ao ciclo. A experiência de desenvolvimento reforçou que decisões de modelagem — como gravar a fase do ciclo como snapshot, vincular biometria à conta ou separar humor como categórico — têm impacto direto na qualidade dos dados gerados e na coerência do produto ao longo do tempo. Ainda há espaço para amadurecer autenticação remota, implementar o fluxo completo de recuperação de senha por código, aprofundar testes de UI e refinar detalhes visuais, mas a solução já entrega um recorte completo do problema que se propôs a resolver.

## 23. Referências

BROWN, N. et al. What are my options here? I don't want to stop training or miss competitions. Navigating the impact of early years of menstruation in organised sports, among girls 10-16 years. **Journal of Science and Medicine in Sport**, 2025. DOI: 10.1016/j.jsams.2025.07.006.

BRUINVELS, G. et al. Prevalence and frequency of menstrual cycle symptoms are associated with availability to train and compete: a study of 6812 exercising women recruited using the Strava exercise app. **British Journal of Sports Medicine**, v. 55, n. 8, p. 438-443, 2021. DOI: 10.1136/bjsports-2020-102792.

MCNULTY, K. L. et al. The Effects of Menstrual Cycle Phase on Exercise Performance in Eumenorrheic Women: A Systematic Review and Meta-Analysis. **Sports Medicine**, v. 50, n. 10, p. 1813-1827, 2020. DOI: 10.1007/s40279-020-01319-3.
