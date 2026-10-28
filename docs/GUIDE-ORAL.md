# Guide de révision pour l'oral — projet I311 « Discord »

Guide écrit pour Yannis, mis à jour après chaque fonctionnalité. Il sert de base aux révisions
pour l'oral du jeudi 5 novembre 2026.

**Comment le lire.** Commence par la partie 1 (les couches, avec l'analogie du restaurant), puis
la partie 3 (trajets d'une requête). Les fiches de fichiers (partie 2) servent de pense-bête.
Chaque fonctionnalité (partie 4) se termine par 3 questions d'oral probables.

**Sources du cours de M. Bruno** (citées dans la suite par leur nom court) :

| Nom court | Lien |
| --- | --- |
| Sujet I311 | https://bruno.univ-tln.fr/teaching-material/lecture-UTLN-M2_Info_Math-DEV-EXP/develop/01-notebook-java-revisions/Content/revisions/01_I311_L_revisions.html |
| Annotations 1/2 | https://bruno.univ-tln.fr/teaching-material/lecture-UTLN-M2_Info_Math-DEV-EXP/develop/02-notebook-java-annotations/01_java_annotations_L_introduction.html |
| Annotations 2/2 (processeurs) | https://bruno.univ-tln.fr/teaching-material/lecture-UTLN-M2_Info_Math-DEV-EXP/develop/02-notebook-java-annotations/02_java_annotations_L_processor.html |
| Bean Validation | https://bruno.univ-tln.fr/teaching-material/lecture-UTLN-M2_Info_Math-DEV-EXP/develop/03-notebook-java-beanvalidation/01_java_beanvalidation_L_Introduction.html |
| JPA | https://bruno.univ-tln.fr/notebooks/notebook-java-jpa/ |
| REST JAX-RS | https://bruno.univ-tln.fr/notebooks/notebook-java-restfulws/ |
| Fonctionnel (M1) | https://bruno.univ-tln.fr/teaching-material/lecture-UTLN-M1_Info_Math-DEV-OOCOL/previews/feature-retone-2027/11-notebook-java-after8/01_java_after8_L_Functionnal.html |
| Streams (M1) | https://bruno.univ-tln.fr/teaching-material/lecture-UTLN-M1_Info_Math-DEV-OOCOL/previews/feature-retone-2027/11-notebook-java-after8/02_java_after8_L_Stream.html |
| Collections, equals/hashCode (M1) | https://bruno.univ-tln.fr/teaching-material/lecture-UTLN-M1_Info_Math-DEV-OOCOL/previews/feature-retone-2027/06-notebook-java-collections/06_java_101_L_Collections.html |

---

## 0. Commandes

Une commande par bloc (zsh).

Démarrer PostgreSQL (après un redémarrage du Mac) :

```bash
docker start product-catalog-postgres
```

Lancer l'application en mode dev (rechargement à chaud) :

```bash
DB_PORT=15432 ./mvnw quarkus:dev
```

Swagger UI : <http://localhost:8080/swagger-ui/> (et non `/q/swagger-ui`, à cause de
`quarkus.swagger-ui.path=/swagger-ui` dans `application.properties`).

Lancer **tous** les tests (unitaires, ArchUnit, intégration ; Docker doit tourner) :

```bash
./mvnw clean verify
```

Lancer seulement les tests unitaires (plus rapide) :

```bash
./mvnw test
```

---

## 1. L'architecture en 4 couches

**Analogie du restaurant.**

| Couche | Paquetage | Dans le restaurant | Rôle technique |
| --- | --- | --- | --- |
| api | `org.acme.catalog.product.api` | le serveur | traduit HTTP ↔ Java, choisit le code de réponse |
| application | `…application` | le chef | enchaîne les étapes d'un cas d'usage, gère la transaction |
| domain | `…domain` | les règles de la maison | records immuables, règles métier, **aucun framework** |
| infrastructure | `…infrastructure` | la réserve | base de données (entités, repositories), conversions (mappers) |

Les DTO ne sont **pas** une 5e couche : ce sont des sous-dossiers d'`application`
(`application/dto` et `application/dto/usecases`).

**Règles de dépendance**, vérifiées automatiquement par ArchUnit
(`src/test/java/org/acme/architecture/LayerArchitecture.java`) :
`api` → `application` seulement ; `application` → `domain` et `infrastructure` ;
`infrastructure` → `domain` et `application` ; `domain` → rien.

**Formulation pour l'oral :** « Mon application est découpée en quatre couches. L'API traduit
HTTP en appels Java, l'application orchestre les cas d'usage dans des transactions, le domaine
contient les règles métier sans aucun framework, et l'infrastructure parle à la base. Des tests
ArchUnit font échouer le build si une couche dépend d'une couche interdite. »

---

## 2. Fiches des fichiers du projet Discord

Tout le code est dans `src/main/java/org/acme/catalog/product/`. Les quatre entités (`User`,
`Guild`, `Channel`, `Role`) suivent **le même schéma de 8 fichiers**. Les fichiers `Product*`
sont l'exemple du starter de M. Bruno, gardés comme référence.

**Qui a écrit quoi :** les 8 fichiers `User*` ont été écrits par Yannis, guidé pas à pas ; les
fichiers Guild/Channel/Role, les UML et tout ce qui est ajouté à partir de ce guide ont été écrits
par l'assistant à la demande de Yannis.

### 2.1 Couche domain

| Fichier | Rôle | Annotations |
| --- | --- | --- |
| `domain/User.java` | Record immuable d'un utilisateur. Le constructeur compact refuse id, pseudo, nom affiché ou date absents/vides ; `User.of()` génère l'UUID et la date. | aucune (domaine pur) |
| `domain/Guild.java` | Record d'une guilde (serveur Discord) : id, nom, date de création. `Guild.of(name)`. | aucune |
| `domain/Channel.java` | Record d'un salon : id, id de la guilde, nom, type, sujet (optionnel). La guilde est référencée par son **id** seulement. | aucune |
| `domain/ChannelType.java` | Enum `TEXT` / `VOICE`. | aucune |
| `domain/Role.java` | Record d'un rôle : id, id de la guilde, nom, position (≥ 0, plus haut = plus de privilèges). | aucune |

### 2.2 Couche infrastructure — persistance

| Fichier | Rôle | Annotations et ce qu'elles font |
| --- | --- | --- |
| `persistence/UserEntity.java` | Ligne de la table `users`. | `@Entity` (classe gérée par JPA) ; `@Table(name="users")` (nom de table) ; `@Id` (clé primaire) ; `@Column(unique=true, nullable=false)` (contraintes SQL) |
| `persistence/GuildEntity.java` | Table `guilds`. Côté **inverse** des relations avec salons et rôles. | `@OneToMany(mappedBy="guild", cascade=ALL, orphanRemoval=true)` : la clé étrangère est chez l'enfant ; supprimer la guilde supprime ses salons et rôles (composition UML) |
| `persistence/ChannelEntity.java` | Table `channels`. Côté **propriétaire** : porte la clé étrangère `guild_id`. | `@ManyToOne(optional=false)` + `@JoinColumn(name="guild_id")` ; `@Enumerated(EnumType.STRING)` (stocke `"TEXT"` et non `0`) ; `@UniqueConstraint(guild_id, name)` (nom unique **par guilde**) |
| `persistence/RoleEntity.java` | Table `roles`, même schéma que `ChannelEntity`. | `@ManyToOne`, `@JoinColumn`, `@UniqueConstraint` |
| `persistence/UserRepository.java` | Accès à `users`. `findByUsername`. | `@ApplicationScoped` (un seul bean CDI) ; implémente `PanacheRepositoryBase<UserEntity, UUID>` |
| `persistence/GuildRepository.java` | Accès à `guilds`, CRUD hérité seulement. | idem |
| `persistence/ChannelRepository.java` | `findByGuild`, `existsByGuildAndName`. | idem |
| `persistence/RoleRepository.java` | `findByGuild` (triés par position décroissante), `existsByGuildAndName`. | idem |

### 2.3 Couche infrastructure — mappers

| Fichier | Rôle |
| --- | --- |
| `mapper/UserMapper.java` | `toEntity(request)` : requête → `User.of(...)` (règles vérifiées) → `UserEntity`. `toDto(entity)` : entité → `UserDTO`. |
| `mapper/GuildMapper.java`, `ChannelMapper.java`, `RoleMapper.java` | Même chose. `ChannelMapper` et `RoleMapper` reçoivent aussi la `GuildEntity` déjà chargée. |

Classes utilitaires : `final`, constructeur privé, méthodes `static`. Aucune annotation.

### 2.4 Couche application

| Fichier | Rôle | Annotations |
| --- | --- | --- |
| `UserService.java` | Cas d'usage : lister, chercher par pseudo, créer (409 si doublon), supprimer (404 si absent). | `@ApplicationScoped` ; `@Transactional` sur les méthodes qui écrivent |
| `GuildService.java` | Idem pour les guildes. | idem |
| `ChannelService.java`, `RoleService.java` | Création **dans une guilde existante** (404 sinon), 409 si nom déjà pris dans la guilde. | idem |
| `dto/UserDTO.java` (et `GuildDTO`, `ChannelDTO`, `RoleDTO`) | Forme du JSON **renvoyé**. Record. | `@Schema` (documentation OpenAPI seulement) |
| `dto/usecases/CreateUserRequest.java` (et `CreateGuild…`, `CreateChannel…`, `CreateRole…`) | Forme du JSON **reçu** à la création. | `@NotBlank`, `@NotNull`, `@Size`, `@PositiveOrZero` (Bean Validation) ; `@Schema` |

### 2.5 Couche api

| Fichier | Rôle | Annotations |
| --- | --- | --- |
| `UserResource.java` | `/api/v1/users` : GET (liste), GET `/{username}`, POST, DELETE `/{username}`. | `@Path`, `@GET`/`@POST`/`@DELETE`, `@PathParam`, `@Produces`/`@Consumes(JSON)`, `@Valid` (déclenche Bean Validation), `@RequestScoped`, `@Inject`, `@Operation`/`@APIResponse`/`@Tag` (OpenAPI) |
| `GuildResource.java` | `/api/v1/guilds`, par id (UUID). | idem |
| `ChannelResource.java`, `RoleResource.java` | `/api/v1/channels`, `/api/v1/roles`. `GET ?guildId=…` filtre par guilde. | `@QueryParam("guildId")` (paramètre **après le `?`**, optionnel) — à opposer à `@PathParam` (morceau du chemin) |
| `error/NotFoundMapper.java` | `NoSuchElementException` → 404. | `@Provider` (enregistré automatiquement par JAX-RS) |
| `error/ConflictMapper.java` | `IllegalStateException` → 409. | `@Provider` |
| `error/DomainExceptionMapper.java` | `IllegalArgumentException` → 400. | `@Provider` |
| `error/JacksonMapper.java` | JSON mal formé → 400. | `@Provider` |
| `error/GlobalExceptionMapper.java` | Toute autre erreur → 500, sans fuite de détails techniques. | `@Provider`, `@Priority` |

---

## 3. Trajet d'une requête

### 3.1 `POST /api/v1/users` (création)

1. **api** — `UserResource.create(@Valid CreateUserRequest request)` : Jackson transforme le JSON
   en record ; `@Valid` lance Bean Validation. Si `@NotBlank` échoue → 400, la méthode n'est pas
   appelée.
2. **application** — `UserService.create` (`@Transactional` ouvre une transaction).
3. **infrastructure** — `UserMapper.toEntity` appelle **domain** `User.of(...)` : UUID + date
   générés, constructeur compact vérifie les règles (`IllegalArgumentException` → 400).
4. **application** — doublon ? `repository.findByUsername(...)` → `IllegalStateException` → 409.
5. **infrastructure** — `repository.persist(entity)` : l'entité entre dans le contexte de
   persistance ; l'`INSERT` part au plus tard au commit.
6. Retour : `UserMapper.toDto(entity)` → `UserDTO` → **api** renvoie `201 Created` + JSON.

### 3.2 `GET /api/v1/users/{username}` (lecture)

1. **api** — `@PathParam("username")` lit le pseudo dans l'URL.
2. **application** — `getByUsername` renvoie un `Optional<UserDTO>`.
3. **api** — `orElseThrow(() -> new NoSuchElementException(...))` : si vide, `NotFoundMapper`
   renvoie 404 ; sinon 200 + JSON.

### 3.3 `PUT /api/v1/roles/{id}` (modification) — le schéma « frontière » du tableau

```
 extérieur  │  frontière (BORDER)  │           intérieur
────────────┼──────────────────────┼──────────────────────────────────────
RoleUpdateQuery ──────────────────────▶ Role (Modèle, domaine) ◀──▶ RoleEntity ──▶ DB
RoleDTO ◀──────────────────────────────────────────────────────── RoleEntity
```

1. **api** — `RoleResource.update(@PathParam("id") UUID id, @Valid RoleUpdateQuery query)` :
   Bean Validation vérifie `@NotBlank` / `@PositiveOrZero` → 400 si invalide.
2. **application** — `RoleService.update` (`@Transactional`) charge l'entité :
   `roles.findByIdOptional(id).orElseThrow(...)` → 404 si absente. L'entité est maintenant
   **managée** : elle est dans le contexte de persistance de la transaction.
3. **application** — si le nom change et qu'un autre rôle de la guilde l'a déjà → 409.
4. **infrastructure → domain** — `RoleMapper.toDomain(entity)` recrée le record `Role`, puis
   `.withName(...)` et `.withPosition(...)` produisent un **nouveau** `Role` (le record est
   immuable) ; son constructeur compact revérifie les règles (`IllegalArgumentException` → 400).
5. **infrastructure** — `RoleMapper.updateEntity(entity, updated)` recopie les champs avec les
   setters. **Pas de `persist`** : au commit, Hibernate compare l'entité managée à son état
   initial (*dirty checking*) et envoie lui-même l'`UPDATE`.
6. Retour : `RoleMapper.toDto(entity)` → **api** renvoie `200 OK` + JSON.

---

## 4. Fonctionnalités ajoutées

### 4.1 Nettoyage (fonctionnalité 1)

**Ce qui a été fait et pourquoi :**

- **`docs/architecture-discord.md` et `docs/idees.md` commités** : ils existaient sur le disque
  mais pas dans git. Je n'ai pas modifié leur contenu (ce sont tes notes). À corriger par toi :
  dans `architecture-discord.md`, l'URL Swagger est écrite `/swagger-x/` au lieu de
  `/swagger-ui/`, et un paragraphe en première personne traîne au milieu.
- **`;` isolé supprimé à la fin de `ProductResource.java`** : Java le tolère (déclaration vide
  au niveau du fichier), mais c'est un reste de frappe qui n'a aucun sens.
- **`UserRepository` passe à `PanacheRepositoryBase<UserEntity, UUID>`**, comme les trois autres
  repositories. Pourquoi : `PanacheRepository<E>` est un raccourci pour
  `PanacheRepositoryBase<E, Long>`, il suppose un id `Long`. Notre `@Id` est un `UUID` : avec
  l'ancienne version, `findById(UUID)` ne compilait pas. Ce que je n'ai **pas** fait : changer
  `findByUsername`, qui fonctionnait déjà.
- **`User` refuse aussi un `displayName` vide** (`displayName.isBlank()`). Pourquoi : le message
  d'erreur disait déjà « cannot be null or empty », mais le code ne testait que `null`. Le code et
  le message disent maintenant la même chose.
- **Espaces superflus de `User.of ( … )` enlevés** : pure mise en forme.
- **Javadoc des 8 fichiers `User*`**, et suppression des restes de copier-coller dans
  `UserResource` (« catalog », « production-ready API », exemple `Username123`).
- **Choix de langue** : la Javadoc ajoutée est en français, alors que celle du starter est en
  anglais. Pourquoi : elle sert d'abord à tes révisions. Les messages d'erreur et les textes
  OpenAPI restent en anglais, comme dans le reste du projet.

**Notion de cours liée :** classe immuable + fabrique + contraintes métier (thème « créez une classe
`Personne` ») ; génériques (`PanacheRepositoryBase<E, ID>`).

**Questions d'oral probables :**

1. *Pourquoi `PanacheRepositoryBase<UserEntity, UUID>` et pas `PanacheRepository<UserEntity>` ?*
   — Parce que `PanacheRepository` fixe le type de l'identifiant à `Long`. Le second paramètre
   générique de `PanacheRepositoryBase` est le type de l'`@Id`, ici `UUID`.
2. *Que fournit un `record` automatiquement, et que reste-t-il à décider ?* — Constructeur
   canonique, accesseurs, `equals`/`hashCode` (sur **tous** les composants) et `toString`, avec des
   champs `final`. Reste à décider : les règles de validité (constructeur compact), la fabrique,
   et si l'égalité « tous les champs » est la bonne (pour un utilisateur, on pourrait vouloir
   comparer seulement l'id).
3. *Pourquoi valider dans `CreateUserRequest` ET dans le record `User` ?* — Bean Validation protège
   l'entrée de l'API et renvoie des messages par champ ; le constructeur du record protège le
   domaine quel que soit l'appelant (un test, un futur bot…). Ce sont deux remparts à deux endroits
   différents.

### 4.2 Contrainte Bean Validation personnalisée `@ValidUsername` (fonctionnalité 2)

**Fichiers :** `application/validation/ValidUsername.java` (l'annotation),
`application/validation/ValidUsernameValidator.java` (la vérification), posée sur
`CreateUserRequest.username`. Tests : `ValidUsernameValidatorTest` (unitaire) et
`UserResourceIT` (API, vérifie le 400).

**Analogie.** L'annotation est une **étiquette** collée sur le champ (« à contrôler : pas
d'espace ») ; le validateur est le **contrôleur** qui lit l'étiquette et fait le contrôle.
`@Constraint(validatedBy = …)` est la ligne qui dit quel contrôleur correspond à quelle étiquette.

**Comment ça marche, dans l'ordre** (mécanisme du Bean Validator) :
1. `UserResource.create(@Valid CreateUserRequest request)` : `@Valid` demande la validation.
2. Le validateur (Hibernate Validator, implémentation de Bean Validation) **reçoit l'instance**,
   fait une **introspection** de sa classe : il cherche les annotations de contrainte sur les champs.
3. Sur `username`, il trouve `@NotBlank` et `@ValidUsername`. Pour `@ValidUsername`, il lit
   `@Constraint(validatedBy = ValidUsernameValidator.class)`, crée le validateur et appelle
   `isValid("mon pseudo", …)` → `false`.
4. Une contrainte au moins a échoué : la méthode `create` **n'est pas appelée**, le client reçoit 400.

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Règle unique : `!value.contains(" ")` | Vérifiable sans expression régulière, une ligne à expliquer. | Pas de longueur 2–32, pas de liste de caractères autorisés : la règle devait rester simple. |
| `@Target(ElementType.FIELD)` | On ne la pose que sur un champ. Dans un record, l'annotation écrite sur le composant `username` est recopiée par Java sur le **champ** privé (car `FIELD` est autorisé). | Pas de `METHOD`, `PARAMETER`, `TYPE_USE` comme dans `CheckCase` : on n'en a pas l'usage. Conséquence à connaître : l'annotation n'est **pas** recopiée sur le composant du record lui-même (voir 4.x Introspection). |
| `@Retention(RetentionPolicy.RUNTIME)` | Le validateur lit l'annotation **pendant l'exécution**, par introspection. Avec `SOURCE` (effacée après compilation) ou `CLASS` (dans le `.class` mais invisible à la réflexion), il ne la verrait pas. | — |
| `@Constraint(validatedBy = ValidUsernameValidator.class)` | Obligatoire : c'est ce qui fait de l'annotation une contrainte Bean Validation. | — |
| `@Documented` | Comme `CheckCase` : la contrainte apparaît dans la Javadoc des champs qui la portent. | Pas de `@Repeatable` (présent dans `CheckCase`) : la poser deux fois n'aurait aucun sens. |
| Éléments `message`, `groups`, `payload` | Les trois sont **exigés** par la spécification Bean Validation pour toute contrainte. `message` a une valeur par défaut en anglais, comme les autres messages du projet. | Aucun élément de configuration en plus (le `value()` de `CheckCase` choisit majuscules/minuscules ; nous n'avons rien à configurer). Donc pas de méthode `initialize`. |
| `isValid(null)` renvoie `true` | Comme dans le cours (`CheckCaseValidator` : `if (object == null) return true;`). Chaque contrainte vérifie **une** chose ; la présence, c'est `@NotBlank`. Sinon un pseudo absent donnerait deux messages d'erreur. | — |
| Paquetage `application/validation` | L'annotation est utilisée par les DTO de la couche application ; un sous-dossier dédié, comme `dto/`. | Pas dans `domain` : le domaine ne doit dépendre d'aucun framework (`jakarta.validation` en est un). |

**Ce qui est vérifié :** `POST /api/v1/users` avec `"username": "mon pseudo"` renvoie **400**
(`UserResourceIT`) ; le validateur seul est testé sur 3 cas (avec espace, sans espace, `null`).

**Notion de cours :** Bean Validation, contrainte personnalisée (cours Bean Validation de M. Bruno,
exemple `CheckCase` / `CheckCaseValidator`) ; rétention des annotations (cours Annotations 1/2).

**Questions d'oral probables :**

1. *Quels sont les deux fichiers d'une contrainte personnalisée, et qu'est-ce qui les relie ?* —
   L'annotation (`@interface ValidUsername`) et la classe qui implémente
   `ConstraintValidator<ValidUsername, String>`. Le lien est
   `@Constraint(validatedBy = ValidUsernameValidator.class)` sur l'annotation.
2. *Pourquoi `RUNTIME` ?* — Parce que le validateur lit l'annotation par introspection pendant
   que l'application tourne. `SOURCE` (comme `@Override`) disparaît à la compilation ; `CLASS` est
   dans le bytecode mais pas accessible par réflexion.
3. *Que renvoie `isValid` pour `null`, et pourquoi ?* — `true` : la nullité est déléguée à
   `@NotNull` / `@NotBlank`, comme dans le cours. Une contrainte = une règle.

### 4.3 Bot Discord « ping-pong » (fonctionnalité 3)

**Fichiers** (`infrastructure/discord/`) :

| Fichier | Rôle | Annotations |
| --- | --- | --- |
| `DiscordBot.java` | **Interface** : une seule méthode `start(String token)`. Le reste de l'application ne connaît qu'elle. | aucune |
| `Discord4jBot.java` | **Implémentation** avec Discord4J : se connecte, écoute `MessageCreateEvent`, répond `pong!` à `!ping`. La logique de réponse est isolée dans `answer(String)`, testable sans Discord. | `@ApplicationScoped` ; `@PreDestroy` (ferme la connexion quand le bean est détruit) |
| `DiscordBotStartup.java` | Au démarrage de Quarkus, démarre le bot **si** un jeton est configuré, sinon écrit un avertissement. Ne dépend que de l'interface. | `@ApplicationScoped` ; `@Observes StartupEvent` (méthode appelée au démarrage) ; `@ConfigProperty(name = "discord.token")` (lit la configuration) ; `@Inject` |

Tests : `DiscordBotStartupTest` (Mockito : sans jeton → `start` jamais appelé ; avec jeton →
appelé ; si `start` échoue → l'application continue) et `Discord4jBotTest` (`answer`).
Documentation pour toi : `docs/discord-bot.md` (portail Discord et lancement).

**Analogie.** L'interface `DiscordBot` est une **prise électrique** : `DiscordBotStartup` branche
« quelque chose qui sait démarrer ». En production on branche le vrai bot Discord4J ; dans un test
on branche un faux (un *mock*) qui note seulement s'il a été appelé.

**Comment ça marche :**
1. Quarkus démarre et envoie l'événement `StartupEvent` ; `DiscordBotStartup.onStart` le reçoit.
2. Le jeton vient de la propriété `discord.token`. MicroProfile Config la remplit
   automatiquement depuis la variable d'environnement `DISCORD_TOKEN` (règle : points remplacés
   par `_`, majuscules). Si elle est absente, l'`Optional` est vide → avertissement, fin.
3. Sinon, `Discord4jBot.start(token)` construit la connexion : `DiscordClient.create(token)`
   → `.gateway()` → `.setEnabledIntents(...)` → `.withGateway(gateway -> gateway.on(MessageCreateEvent.class, this::onMessage))`
   → `.subscribe(...)`.
4. À chaque message, `onMessage` appelle `answer(contenu)` ; si c'est `"pong!"`, il récupère le
   salon (`getChannel()`, un `Mono`) puis envoie la réponse (`flatMap(channel -> channel.createMessage(answer))`).

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Interface + implémentation | Exigence du sujet (« abstraction mockable ») : on teste le démarrage sans réseau. | Pas de couche « service de bot » ni de « port » supplémentaire : une interface suffit. |
| Une 3e classe `DiscordBotStartup` | Il faut quelqu'un qui **utilise** l'interface ; sinon elle ne servirait à rien et il n'y aurait rien à mocker. C'est elle qu'on teste avec le mock. | Je n'ai pas mis le démarrage dans `Discord4jBot` : on ne pourrait plus le tester sans Discord. |
| Paquetage `infrastructure/discord` | Discord est une technique extérieure, comme la base de données : place naturelle dans l'infrastructure. | — |
| Discord4J **3.3.0**, pas la 3.3.3 (la plus récente) | La 3.3.3 tire Reactor Netty 1.3.6, qui exige **Netty 4.2**. Quarkus 3.31 impose **Netty 4.1.130** (son BOM). Résultat constaté : `NoSuchFieldError … SslContext` au démarrage. La 3.3.0 utilise Reactor Netty 1.1.31, de la génération Netty 4.1 : compatible. L'API utilisée est la même. | Je n'ai pas forcé Netty 4.2 : cela aurait cassé Quarkus. |
| `subscribe()` et non `block()` (l'exemple du README fait `login().block()`) | `block()` arrêterait le démarrage de Quarkus le temps de la connexion à Discord, et une erreur de connexion le ferait planter. | — |
| `subscribe(null, error -> LOG.error(...))` | Si Discord refuse le jeton plus tard (401), on écrit un message clair au lieu de l'erreur générique de Reactor « ErrorCallbackNotImplemented ». | — |
| `try/catch` autour de `bot.start` | Discord4J vérifie le **format** du jeton tout de suite et lance `IllegalArgumentException`. Sans le `catch`, un jeton mal copié empêchait l'API REST de démarrer (constaté). Le bot est un bonus : il ne doit pas casser le reste. | — |
| Intent `MESSAGE_CONTENT` | Depuis 2022, Discord envoie un contenu vide aux bots qui n'ont pas cet intent privilégié (README de Discord4J). | — |
| `@PreDestroy` + `dispose()` | En mode dev, le rechargement à chaud recrée les beans : sans fermeture, l'ancien bot resterait connecté et répondrait en double. | — |
| 2 `ignoreDependency` dans `LayerArchitecture` (`discord4j..`, `reactor..`) | Ce test refuse toute dépendance vers un paquetage non listé ; le starter ajoute déjà ainsi Mutiny, Jackson, SLF4J. Ta décision du 28/10. | Le bot n'est pas sorti des 4 couches. |
| Jeton par variable d'environnement + `.env.example` | Jamais de secret dans git (sujet). `.env` est déjà dans le `.gitignore`. | — |

**Notion de cours :** programmation vers une interface, patron Adapter, mock Mockito ; CDI
(`@ApplicationScoped`, `@Observes`, `@PreDestroy`) ; MicroProfile Config ; programmation
asynchrone avec Reactor (`Mono` : 0 ou 1 valeur à venir ; rien ne se passe avant `subscribe()`).

**Questions d'oral probables :**

1. *Pourquoi une interface `DiscordBot` ?* — Pour que `DiscordBotStartup` ne dépende pas de
   Discord4J : dans le test, je passe un mock Mockito et je vérifie avec `verify(bot).start(...)`
   ou `verify(bot, never())` sans me connecter à Discord. C'est l'inversion des dépendances.
2. *Que se passe-t-il si `DISCORD_TOKEN` n'est pas défini ?* — L'`Optional<String>` injecté par
   `@ConfigProperty` est vide : on écrit un avertissement et on ne démarre pas le bot. L'API, les
   tests et la CI fonctionnent normalement.
3. *Quelle différence entre `subscribe()` et `block()` sur un `Mono` ?* — `block()` attend le
   résultat en bloquant le thread courant ; `subscribe()` lance le traitement et rend la main
   tout de suite, le résultat arrivera plus tard. Au démarrage on ne veut pas bloquer Quarkus.

### 4.4 Processeur d'annotations `@DomainModel` (fonctionnalité 4)

**Fichiers :**

| Fichier | Rôle | Annotations |
| --- | --- | --- |
| `domain/DomainModel.java` | L'annotation, posée sur `User`, `Guild`, `Channel`, `Role`. | `@Target(ElementType.TYPE)`, `@Retention(RetentionPolicy.SOURCE)` |
| `src/main/java/org/acme/processor/DomainModelProcessor.java` | Le processeur : **vérifie** (erreur si pas un record) et **génère** `XxxFields.java`. | `@SupportedAnnotationTypes("…DomainModel")`, `@SupportedSourceVersion(RELEASE_21)` |
| `pom.xml` (plugin `maven-compiler-plugin`) | Compile le processeur **avant** le reste et l'active. | — |
| `src/test/java/org/acme/processor/DomainModelProcessorTest.java` | Lance `javac` depuis le test : classe ordinaire → erreur ; record → `GoodFields.java` généré. | — |

Fichiers générés (dans `target/generated-sources/annotations/org/acme/catalog/product/domain/`) :
`UserFields`, `GuildFields`, `ChannelFields`, `RoleFields`. Exemple réel :

```java
public final class UserFields {
    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String DISPLAYNAME = "displayName";
    public static final String JOINEDAT = "joinedAt";
}
```

**Analogie.** L'introspection, c'est un contrôleur qui vient vérifier le restaurant **pendant le
service** (l'application tourne). Le processeur d'annotations, c'est l'architecte qui relit les
**plans avant la construction** (la compilation) : il peut refuser un plan, ou dessiner une pièce
en plus, mais il n'est plus là une fois le bâtiment ouvert.

**Comment ça marche (vocabulaire du cours) :**
1. `javac` compile le projet. Il voit `@DomainModel` et sait (grâce à `-processor`, voir le pom)
   que `DomainModelProcessor` la traite : `@SupportedAnnotationTypes` le dit.
2. Il appelle `process(annotations, roundEnv)`. Le `RoundEnvironment` décrit le « tour » en cours.
   `roundEnv.getElementsAnnotatedWith(annotation)` renvoie les éléments du code (classes,
   records…) qui portent l'annotation. Ce ne sont **pas** des objets `Class` (le code n'est pas
   encore compilé) mais des `Element`, une représentation du source.
3. **Vérifier** : si `element.getKind() != ElementKind.RECORD`, le **`Messager`**
   (`processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, …, element)`) émet une erreur
   attachée à cet élément. `javac` affiche l'erreur et la compilation échoue.
4. **Générer** : sinon, le **`Filer`** (`processingEnv.getFiler().createSourceFile(...)`) crée un
   nouveau fichier source ; on l'écrit avec un `PrintWriter`, une ligne par composant
   (`getRecordComponents()`).
5. Un fichier a été généré : `javac` refait un tour (round) pour compiler ce nouveau fichier.
   `XxxFields` ne porte pas `@DomainModel`, donc le processeur n'a rien à faire : **pas de boucle
   infinie** (bonne pratique citée dans le cours).
6. `process` renvoie `true` : l'annotation est « réclamée », aucun autre processeur ne la traite.

**La configuration Maven (le point le plus technique) :**

Un processeur doit exister **sous forme compilée** avant la compilation du code qu'il traite.
Or il est dans le même projet. D'où deux étapes du `maven-compiler-plugin` :

| Étape | Phase Maven | Ce qu'elle compile | Réglages |
| --- | --- | --- | --- |
| `compile-processor` | `process-sources` (avant `compile`) | seulement `org/acme/processor/**` (`<includes>`) | `<proc>none</proc>` (aucun processeur pendant cette étape) ; `<useIncrementalCompilation>false</useIncrementalCompilation>` |
| `default-compile` (la compilation habituelle) | `compile` | tout **sauf** `org/acme/processor/**` (`<excludes>`) | `<annotationProcessors>` = `org.acme.processor.DomainModelProcessor` (option `-processor` de javac ; le processeur est trouvé sur le classpath, dans `target/classes`, grâce à l'étape 1) |

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Deux exécutions dans le même projet | Le plus simple qui marche : aucun déplacement de fichiers, aucune nouvelle dépendance. Paramètres tous documentés (`includes`, `excludes`, `proc`, `annotationProcessors`, doc officielle du maven-compiler-plugin). La doc Maven ne décrit **pas** ce montage précis : je l'ai vérifié à la main (build complet, mode dev depuis `clean`, rechargement à chaud). | **Pas** de projet Maven multi-modules (un module « processeur » + un module « application ») : c'est la solution classique, mais elle oblige à déplacer tout le code, le `pom` et la CI. Pas d'AutoService ni de fichier `META-INF/services` : on déclare le processeur explicitement. |
| `<annotationProcessors>` explicite | Depuis le JDK 23, `javac` n'exécute plus les processeurs trouvés automatiquement : il faut les déclarer (doc Maven). C'est aussi plus sûr : seul notre processeur s'exécute. | — |
| `<excludes>` dans l'étape 2 | Sans lui, quand l'étape 2 recompile tout, elle efface d'abord les `.class` de **ses** sources, donc le processeur dont elle a besoin. | — |
| `useIncrementalCompilation=false` dans l'étape 1 | Constaté en mode dev : les deux étapes partageaient le même suivi incrémental, et l'étape 2 croyait le source du processeur « supprimé » → elle effaçait son `.class` → « Annotation processor … not found ». L'étape 1 ne compile qu'un fichier : se passer du suivi ne coûte rien. | — |
| `@Retention(SOURCE)` | Seul le compilateur lit `@DomainModel`. Elle n'existe plus dans le `.class` : c'est voulu, et c'est ce qu'on montre au test d'introspection (4.x). | Pas `RUNTIME` : inutile. |
| `@Target(TYPE)` | Il n'existe pas de cible « record seulement » : `TYPE` couvre classes, interfaces, enums et records. D'où la vérification « est-ce un record ? » dans le processeur. | — |
| Paquetage `org.acme.processor` | C'est un outil de compilation, pas une couche de l'application ; il reste hors de `org.acme.catalog.product`, que les tests ArchUnit de couches analysent. | — |
| Annotation dans `domain` | Les records du domaine l'importent ; le domaine ne doit dépendre d'aucune autre couche (ArchUnit). Elle n'a aucune dépendance (Java pur). | — |
| Générer `XxxFields` (constantes des noms de composants) | Le plus petit fichier utile : il remplace des chaînes écrites à la main (comme `"username"` dans `find("username", …)`) par des constantes vérifiées par le compilateur. | Pas de génération de DAO, de builder ni de getters : plus long à écrire et à expliquer. |
| `XxxFields` **n'est pas utilisée** dans le code principal | Testé puis retiré : ton IDE (VS Code) compile avec son propre compilateur (Eclipse JDT), qui n'exécute pas notre processeur. Il ne voyait pas `UserFields`, marquait `UserRepository` en erreur et écrivait un `.class` cassé dans `target/classes`, ce qui a fait échouer les tests. La génération est prouvée par `DomainModelProcessorTest`. | — |
| `@DomainModel` sur les 4 records Discord seulement | Ce sont nos modèles. | Pas sur `Product` (code de référence du starter, laissé intact). |

**Limite connue :** dans VS Code, les classes `XxxFields` peuvent apparaître inconnues ; seul
Maven les génère. **Non vérifié** : si un **nouveau** record `@DomainModel` créé pendant
`quarkus:dev` obtient sa classe `XxxFields` sans relancer Maven (le rechargement d'un record
existant, lui, a été vérifié).

**Notion de cours :** processeurs d'annotations (cours Annotations 2/2 de M. Bruno) ; rétention
`SOURCE` / `CLASS` / `RUNTIME` (cours Annotations 1/2). Rappel des trois rétentions :
`SOURCE` = effacée à la compilation (`@Override`, `@DomainModel`, celles de Lombok) ;
`CLASS` = dans le `.class` mais invisible à la réflexion (valeur par défaut) ;
`RUNTIME` = lisible à l'exécution (Bean Validation, `@Entity`, `@Path`).

**Questions d'oral probables :**

1. *Quelle différence entre un processeur d'annotations et l'introspection ?* — Le processeur lit
   les annotations **à la compilation**, dans `javac` ; il se contente de la rétention `SOURCE`.
   Il peut produire des erreurs de compilation ou générer du code. L'introspection lit les
   annotations **à l'exécution**, par réflexion (`getAnnotation`) ; elle exige `RUNTIME`.
2. *À quoi servent `Messager` et `Filer` ?* — Le `Messager` signale des messages au compilateur
   (une erreur de type `Diagnostic.Kind.ERROR` fait échouer la compilation) ; le `Filer` crée de
   nouveaux fichiers (sources, classes, ressources) que le compilateur prend ensuite en compte.
3. *Pourquoi faut-il compiler le processeur avant le reste, et comment fais-tu avec Maven ?* —
   Parce que `javac` doit pouvoir **exécuter** le processeur pendant qu'il compile notre code.
   J'ai deux exécutions du `maven-compiler-plugin` : l'une compile seulement le processeur plus
   tôt (phase `process-sources`), l'autre compile le reste en déclarant le processeur avec
   `<annotationProcessors>`.

### 4.5 Programmation fonctionnelle (fonctionnalité 5)

**Ce qui a été ajouté :**

| Où | Quoi |
| --- | --- |
| `GuildService.getChannelStats` + `GuildResource` | `GET /api/v1/guilds/{id}/stats` → par exemple `{"TEXT": 2, "VOICE": 1}` |
| `UserService.getAll(String search)` + `UserResource` | `GET /api/v1/users?search=yan` (pseudo **ou** nom affiché, sans tenir compte des majuscules) |
| `UserMapper.toDomain(entity)` et `UserMapper.toDto(User)` | Nécessaires pour filtrer sur le **record du domaine** `User` (`Predicate<User>`) |
| Tests | `GuildResourceIT` (stats 200 et 404), `UserResourceIT.searchFiltersUsers` |

**Analogie.** Un stream est une **chaîne de montage** : les objets passent un par un devant des
postes. `map` = un poste qui transforme l'objet ; `filter` = un poste qui jette ceux qui ne
conviennent pas ; `collect` / `toList` = le carton à la fin de la chaîne. Une lambda
(`x -> …`) est la consigne donnée à un poste.

**1) Le filtre `?search=` avec un `Predicate<User>`**

```java
Predicate<User> matchesSearch = user -> search == null
        || user.username().toLowerCase().contains(search.toLowerCase())
        || user.displayName().toLowerCase().contains(search.toLowerCase());

return repository.listAll().stream()
        .map(UserMapper::toDomain)   // Function<UserEntity, User>
        .filter(matchesSearch)       // Predicate<User>
        .map(UserMapper::toDto)      // Function<User, UserDTO>
        .toList();
```

Le même code en boucle `for` classique :

```java
List<UserDTO> result = new ArrayList<>();
for (UserEntity entity : repository.listAll()) {
    User user = UserMapper.toDomain(entity);
    if (matchesSearch.test(user)) {
        result.add(UserMapper.toDto(user));
    }
}
return result;
```

**2) Les statistiques avec `Collectors`**

```java
return guild.getChannels().stream()
        .collect(Collectors.groupingBy(ChannelEntity::getType, Collectors.counting()));
```

En boucle `for` :

```java
Map<ChannelType, Long> result = new HashMap<>();
for (ChannelEntity channel : guild.getChannels()) {
    ChannelType type = channel.getType();
    result.put(type, result.getOrDefault(type, 0L) + 1);
}
return result;
```

`groupingBy(clé, collecteurAval)` : le premier argument dit **comment ranger** (par type), le
second dit **quoi faire de chaque groupe** (le compter). C'est la remarque du cours : quand on
veut enchaîner un traitement sur le résultat d'un stream, regarder du côté de `Collectors`.

**3) Où sont les 4 interfaces fonctionnelles dans le projet (usages réels, rien d'ajouté exprès) :**

| Interface | Méthode abstraite | Où dans le projet |
| --- | --- | --- |
| `Predicate<T>` | `boolean test(T t)` | `matchesSearch` dans `UserService.getAll` ; `filter(...)` attend un `Predicate` |
| `Function<T, R>` | `R apply(T t)` | `.map(UserMapper::toDto)` dans tous les services (`map` attend une `Function`) ; `ChannelEntity::getType` donné à `groupingBy` ; `withGateway(gateway -> …)` dans le bot |
| `Supplier<T>` | `T get()` | `orElseThrow(() -> new NoSuchElementException("…"))` dans les ressources et services : la lambda **fournit** l'exception, seulement si l'`Optional` est vide |
| `Consumer<T>` | `void accept(T t)` | `subscribe(null, error -> LOG.error(…))` dans `Discord4jBot` : la lambda **consomme** l'erreur sans rien renvoyer ; `forEach(row -> …)` dans `ProductSteps` (tests du starter) |

`UserMapper::toDto` est une **référence de méthode** : une écriture courte pour la lambda
`entity -> UserMapper.toDto(entity)`.

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Filtrer sur le record `User` (domaine) | La consigne demandait un `Predicate<User>` ; c'est aussi l'occasion d'avoir une conversion entité → domaine (`toDomain`) qui resservira pour le PUT. | Pas de filtre en SQL : le filtrage en mémoire charge tous les utilisateurs. C'est acceptable pour l'oral ; la pagination et les filtres SQL sont prévus en priorité D. |
| Deux méthodes `toDto` (surcharge) | Même nom, même rôle (« vers le DTO ») ; Java choisit d'après le type. La référence `UserMapper::toDto` marche sur un stream d'entités comme sur un stream de `User`. | — |
| `toLowerCase()` des deux côtés | Recherche insensible aux majuscules, sans expression régulière. | — |
| `search == null` dans le prédicat | Sans paramètre, le prédicat est toujours vrai : un seul chemin de code. | — |
| Renvoyer une `Map<ChannelType, Long>` | Jackson la transforme directement en `{"TEXT": 2}` ; pas besoin d'un DTO de plus. | Un type sans salon n'apparaît pas (on n'ajoute pas `"VOICE": 0`) : c'est le comportement de `groupingBy`, le compléter demanderait du code en plus. |
| `guild.getChannels()` (la relation `@OneToMany`) | Réutilise la relation existante. Le chargement est LAZY (par défaut pour `@OneToMany`) : la liste est lue en base au premier accès. Constaté par le test : cela fonctionne sans `@Transactional` sur la méthode. | Je ne peux pas confirmer le mécanisme exact de Quarkus qui garde la session ouverte ici sans vérifier sa doc ; si on veut être prudent, `@Transactional` sur la méthode le garantit. |
| Pas de `Comparable` sur `Role` | Consigne : un `compareTo` sur la seule position serait **incohérent avec `equals`** : deux rôles différents (« Modo » et « Admin ») de même position donneraient `compareTo == 0` alors que `equals` est `false`. Un `TreeSet<Role>` en garderait alors un seul (il se fie à `compareTo`). Pour trier, on utilise un `Comparator` à part, par exemple `Comparator.comparingInt(Role::position)`, sans changer la définition de l'égalité. | Le tri des rôles existant (`order by position desc` dans `RoleRepository`) est fait en SQL, rien à ajouter. |

**Notion de cours :** programmation fonctionnelle et streams (cours Java 8 de M1 de M. Bruno,
« Fonctionnel » et « Streams ») ; `Comparable` / `equals` (cours Collections de M1).

**Questions d'oral probables :**

1. *Qu'est-ce qu'un `Predicate`, et où l'utilises-tu ?* — Une interface fonctionnelle dont la
   méthode `test(T)` renvoie un `boolean`. Dans `UserService.getAll`, `matchesSearch` dit si un
   `User` correspond à la recherche ; je le passe à `filter`.
2. *Que fait `Collectors.groupingBy(ChannelEntity::getType, Collectors.counting())` ?* — Il range
   les salons dans une `Map` dont la clé est le type, et `counting()` remplace la liste de chaque
   groupe par son nombre d'éléments : `{TEXT=2, VOICE=1}`.
3. *Dans `orElseThrow(() -> new NoSuchElementException(...))`, quelle interface fonctionnelle
   est utilisée, et pourquoi une lambda plutôt que l'exception directement ?* — Un `Supplier` :
   l'exception n'est créée que si l'`Optional` est vide. On passe « la façon de la fabriquer »,
   pas l'exception elle-même.

### 4.6 Modification (PUT) sur les 4 entités (fonctionnalité 6)

**Endpoints ajoutés :**

| Endpoint | Objet reçu | Champs modifiables | Codes |
| --- | --- | --- | --- |
| `PUT /api/v1/roles/{id}` | `RoleUpdateQuery` | `name`, `position` | 200, 400, 404, 409 |
| `PUT /api/v1/users/{username}` | `UserUpdateQuery` | `displayName` | 200, 400, 404 |
| `PUT /api/v1/guilds/{id}` | `GuildUpdateQuery` | `name` | 200, 400, 404 |
| `PUT /api/v1/channels/{id}` | `ChannelUpdateQuery` | `name`, `topic` | 200, 400, 404, 409 |

**Le même schéma pour chacune, en 5 morceaux** (exemple `Role`, trajet détaillé en 3.3) :

| Morceau | Fichier | Rôle |
| --- | --- | --- |
| Objet reçu | `dto/usecases/RoleUpdateQuery` | Record avec les seuls champs modifiables + Bean Validation |
| Withers | `domain/Role` : `withName`, `withPosition` | Fabriquent un nouveau record modifié |
| Conversions | `RoleMapper.toDomain(entity)`, `RoleMapper.updateEntity(entity, role)` | Entité → domaine, puis domaine → entité (setters) |
| Cas d'usage | `RoleService.update(id, query)` | 404, 409, withers, recopie, DTO |
| Endpoint | `RoleResource.update` | `@PUT @Path("/{id}")` + `@Valid` |

Tests : `RoleResourceIT` (les 4 codes), `UserResourceIT.updateDisplayName` (200, 404),
`GuildResourceIT.updateName` (200, 400), `ChannelResourceIT` (200, 409).

**Analogie.** On ne **rature** pas une fiche du domaine (immuable) : on en **recopie une neuve**
avec la correction (le *wither*), le contrôleur (constructeur compact) la vérifie, puis on recopie
la nouvelle version dans le classeur (l'entité). Le classeur est déjà sur la table (entité
managée) : pas besoin de le ranger à nouveau (`persist`), Hibernate voit qu'il a changé.

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Un `XxxUpdateQuery` distinct du `CreateXxxRequest` | Schéma du tableau ; les champs ne sont pas les mêmes : pas de `guildId` (un rôle ou un salon ne change jamais de guilde), pas de `type` pour un salon. | Pas de réutilisation de `CreateXxxRequest` : on pourrait sinon envoyer un `guildId` qui serait ignoré, ce qui est trompeur. |
| Nom « UpdateQuery » et place dans `dto/usecases` | Nom écrit au tableau ; à côté des objets de création, car ce sont aussi des objets **reçus** pour un cas d'usage. | — |
| `PUT` (remplacement des champs modifiables) | Tous les champs modifiables sont envoyés à chaque fois : simple à valider (`@NotBlank` partout). Pour un salon, `topic` absent ou `null` efface le sujet. | Pas de `PATCH` (modification partielle) : il faudrait distinguer « champ absent » et « champ à `null` ». |
| Withers dans les records | Le domaine est immuable ; le wither crée une copie et **revérifie les invariants** par le constructeur compact (même pattern que `Product.withName` du starter). | Pas de setters dans les records (impossible : champs `final`). |
| `toDomain` + `updateEntity` dans les mappers | Les mappers sont déjà l'endroit où l'on traduit entre les modèles. | — |
| Pas de `persist` après la modification | L'entité a été chargée dans la transaction : elle est **managée**. Au commit, Hibernate détecte les changements (*dirty checking*) et fait l'`UPDATE`. | — |
| 409 seulement pour `Role` et `Channel` | Seuls leurs noms sont uniques (par guilde, `@UniqueConstraint`). On ne lève le 409 que si le nom **change** : renvoyer le même nom n'est pas un doublon. | `User` : le pseudo n'est pas modifiable (ta décision du 28/10), donc pas de doublon possible. `Guild` : aucune unicité sur le nom. |
| `User` : `displayName` seul | Ta décision : le pseudo sert d'identifiant dans l'URL ; le modifier changerait l'adresse de la ressource. | — |
| `Channel` : `type` non modifiable | Un salon textuel et un salon vocal n'ont pas le même usage ; changer de type, c'est créer un autre salon. | — |
| Jamais l'id, la guilde, `joinedAt`, `createdAt` | Consigne : ce sont des données d'identité ou d'historique. | — |
| Réponse `200 OK` avec le DTO | Le client voit l'état final. | Pas de `204 No Content` : il devrait refaire un `GET` pour voir le résultat. |

**Notion de cours :** immuabilité et pattern *wither* ; DTO et frontière de l'application ; JPA :
contexte de persistance, entité managée, *dirty checking* (cours JPA de M. Bruno) ; REST : verbe
`PUT`, codes 200 / 400 / 404 / 409 (cours REST JAX-RS).

**Questions d'oral probables :**

1. *Pourquoi un `RoleUpdateQuery` et pas le `CreateRoleRequest` ?* — Parce que les champs
   modifiables ne sont pas ceux de la création : on ne change ni l'id ni la guilde d'un rôle.
   L'objet reçu décrit exactement ce que le client a le droit de demander.
2. *Ton record est immuable : comment le modifies-tu ?* — Je ne le modifie pas : `withName`
   renvoie un **nouveau** `Role` avec le nouveau nom et les autres valeurs recopiées. Le
   constructeur compact s'exécute, donc les règles sont revérifiées.
3. *Tu n'appelles pas `persist` dans `update` : comment la base est-elle mise à jour ?* — L'entité
   a été chargée par `findByIdOptional` dans une méthode `@Transactional` : elle est managée par
   le contexte de persistance. Au commit, Hibernate compare son état à celui du chargement et
   envoie l'`UPDATE` (*dirty checking*).
