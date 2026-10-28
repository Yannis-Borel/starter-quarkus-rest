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

Lancer l'application **avec le bot Discord** (remplacer `ton-jeton` ; étapes du portail dans
`docs/discord-bot.md`) :

```bash
DISCORD_TOKEN=ton-jeton DB_PORT=15432 ./mvnw quarkus:dev
```

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
| `error/JacksonMapper.java` | JSON bien formé mais du mauvais type (`MismatchedInputException`) → 400. | `@Provider` |
| `error/ValidationMapper.java` | `ConstraintViolationException` (Bean Validation) → 400 avec la liste des champs. | `@Provider` |
| `error/ErrorResponse.java` | Le **format unique** de toutes les erreurs (`status`, `error`, `message`, `fields`) et la méthode `build` utilisée par tous les mappers. | aucune |
| `error/GlobalExceptionMapper.java` | Toute autre erreur → 500, sans fuite de détails techniques. | `@Provider`, `@Priority` |

### 2.6 Fichiers ajoutés depuis le 28 octobre (index)

Le détail de chacun est dans la section de sa fonctionnalité (partie 4).

| Fichier | Couche | Rôle en une phrase | Annotations principales | Section |
| --- | --- | --- | --- | --- |
| `application/validation/ValidUsername` | application | Contrainte Bean Validation : pseudo sans espace. | `@Constraint`, `@Target(FIELD)`, `@Retention(RUNTIME)`, `@Documented` | 4.2 |
| `application/validation/ValidUsernameValidator` | application | Vérification de `@ValidUsername`. | aucune (implémente `ConstraintValidator`) | 4.2 |
| `infrastructure/discord/DiscordBot` | infrastructure | Interface du bot (mockable). | aucune | 4.3 |
| `infrastructure/discord/Discord4jBot` | infrastructure | Bot Discord4J : `!ping` → `pong!`. | `@ApplicationScoped`, `@PreDestroy` | 4.3 |
| `infrastructure/discord/DiscordBotStartup` | infrastructure | Démarre le bot si `DISCORD_TOKEN` existe. | `@ApplicationScoped`, `@Observes StartupEvent`, `@ConfigProperty`, `@Inject` | 4.3 |
| `domain/DomainModel` | domain | Annotation traitée à la compilation. | `@Target(TYPE)`, `@Retention(SOURCE)` | 4.4 |
| `org/acme/processor/DomainModelProcessor` | hors couches (outil de compilation) | Processeur : vérifie « est un record » et génère `XxxFields`. | `@SupportedAnnotationTypes`, `@SupportedSourceVersion` | 4.4 |
| `dto/usecases/RoleUpdateQuery`, `UserUpdateQuery`, `GuildUpdateQuery`, `ChannelUpdateQuery` | application | JSON reçu pour un `PUT` : champs modifiables seulement. | Bean Validation, `@Schema` | 4.6 |
| `api/error/ErrorResponse` | api | Format unique des erreurs + méthode `build`. | aucune | 4.7 |
| `api/error/ValidationMapper` | api | `ConstraintViolationException` → 400 + champs. | `@Provider` | 4.7 |
| `api/error/GoneMapper` | api | `InvitationExpiredException` → 410. | `@Provider` | 4.12 |
| `domain/Invitation` | domain | Record du lien d'invitation, règle `isExpired`. | `@DomainModel` | 4.12 |
| `persistence/InvitationEntity`, `InvitationRepository` | infrastructure | Table `invitations`, `findByCode`. | `@Entity`, `@ManyToOne`, `@ApplicationScoped` | 4.12 |
| `mapper/InvitationMapper` | infrastructure | Conversions de l'invitation. | aucune | 4.12 |
| `application/InvitationService`, `InvitationExpiredException` | application | Créer un lien, rejoindre ; exception « expiré ». | `@ApplicationScoped`, `@Transactional` | 4.12 |
| `dto/InvitationDTO`, `dto/usecases/JoinGuildRequest` | application | JSON renvoyé / reçu. | `@Schema`, `@NotBlank` | 4.12 |
| `api/InvitationResource` | api | `POST /api/v1/invitations/{code}/join`. | `@Path`, `@POST`, `@PathParam`, `@Valid` | 4.12 |

Fichiers existants **modifiés** : les 4 records (withers, `@DomainModel`), les 4 mappers
(`toDomain`, `updateEntity`), les 4 services et ressources (`update`, `PUT`), `GuildService` /
`GuildResource` (stats, membres, invitations), `UserService` / `UserResource` (recherche,
retrait des guildes à la suppression), `GuildEntity` (`members`, `invitations`), `UserEntity`
(`@NotBlank`, `guilds`), les 5 mappers d'erreurs (format commun), `pom.xml` (Discord4J,
processeur), `LayerArchitecture` (2 `ignoreDependency`).

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
| `@Target(ElementType.FIELD)` | On ne la pose que sur un champ. Dans un record, l'annotation écrite sur le composant `username` est recopiée par Java sur le **champ** privé (car `FIELD` est autorisé). | Pas de `METHOD`, `PARAMETER`, `TYPE_USE` comme dans `CheckCase` : on n'en a pas l'usage. Conséquence à connaître : l'annotation n'est **pas** recopiée sur le composant du record lui-même (voir 4.9 Introspection). |
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
| `@Retention(SOURCE)` | Seul le compilateur lit `@DomainModel`. Elle n'existe plus dans le `.class` : c'est voulu, et c'est ce qu'on montre au test d'introspection (4.9). | Pas `RUNTIME` : inutile. |
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

### 4.7 Gestion des erreurs : un seul format JSON (fonctionnalité 6 bis)

**Toutes les erreurs ressemblent maintenant à ceci :**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fields": [
    { "field": "username", "message": "Username must not contain spaces" },
    { "field": "displayName", "message": "displayName is required" }
  ]
}
```

`fields` n'est rempli que pour un 400 de validation ; sinon c'est `[]`.

**Quelle exception donne quel code, et par quel mapper** (tous dans `api/error/`) :

| Situation | Exception Java | Code | Mapper |
| --- | --- | --- | --- |
| Contrainte Bean Validation (`@NotBlank`, `@ValidUsername`…) sur un objet `@Valid` | `ConstraintViolationException` | 400 + `fields` | `ValidationMapper` (nouveau) |
| Règle du domaine violée (constructeur compact d'un record) | `IllegalArgumentException` | 400 | `DomainExceptionMapper` |
| JSON bien formé mais du mauvais type (`[1,2]`, `"type": "PHOTO"`) | `MismatchedInputException` (Jackson) | 400 | `JacksonMapper` |
| JSON **mal formé** (accolade manquante) | `BadRequestException` (Quarkus enveloppe l'erreur Jackson) | 400 | `GlobalExceptionMapper` (branche `WebApplicationException`) |
| Ressource absente (`orElseThrow`) | `NoSuchElementException` | 404 | `NotFoundMapper` |
| URL inconnue, ou UUID invalide dans l'URL | `NotFoundException` (JAX-RS) | 404 | `GlobalExceptionMapper` |
| Verbe non autorisé (`DELETE /api/v1/users`) | `NotAllowedException` (JAX-RS) | 405 | `GlobalExceptionMapper` |
| Mauvais `Content-Type` | `NotSupportedException` (JAX-RS) | 415 | `GlobalExceptionMapper` |
| Doublon (pseudo, nom de salon/rôle dans la guilde) | `IllegalStateException` | 409 | `ConflictMapper` |
| Tout le reste (bug, base injoignable…) | n'importe quel `Throwable` | 500, message générique, vraie erreur dans les logs | `GlobalExceptionMapper` |

Les lignes JSON mal formé, URL inconnue, 405 et 415 ont été **constatées** en interrogeant
l'application lancée ; les autres sont couvertes par `ErrorFormatIT` et
`GlobalExceptionMapperTest`.

**Comment JAX-RS choisit le mapper :** quand une exception sort d'une ressource, il cherche le
`ExceptionMapper<E>` dont le type `E` est **le plus proche** de la classe de l'exception dans la
hiérarchie d'héritage. `NoSuchElementException` → `NotFoundMapper` (type exact) plutôt que
`GlobalExceptionMapper` (`Throwable`, très loin). C'est pour cela que `GlobalExceptionMapper`
n'attrape que ce que personne d'autre ne prend.

**Analogie.** Les mappers sont des **guichets** spécialisés ; `GlobalExceptionMapper` est
l'accueil général. `ErrorResponse` est le **formulaire unique** que tous les guichets remplissent :
le client reçoit toujours le même papier, quel que soit le guichet.

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Un seul record `ErrorResponse` + `build(...)` | Consigne « ne rien dupliquer » : chaque mapper tient en une ligne. `Response.StatusType` en paramètre accepte aussi bien `Response.Status.NOT_FOUND` qu'un statut venu d'une exception JAX-RS (405, 415…). | Pas de classe de base abstraite pour les mappers : une méthode statique suffit. |
| Champs `status` + `error` + `message` | `status` répète le code pour le client ; `error` est le nom standard (`getReasonPhrase()`) ; `message` est l'explication lisible. | Pas de `timestamp` ni de `path` : non demandés. |
| `fields` = liste de `FieldError(field, message)` | Une **liste** et pas une `Map` : un même champ peut échouer sur deux contraintes (ex. `"  "` échoue à `@NotBlank` **et** à `@ValidUsername`). | — |
| Nom du champ = dernier morceau du chemin (`create.request.username` → `username`) | Le client n'a pas à connaître le nom de la méthode Java. Un `substring` suffit. | Pas d'expression régulière. |
| `ValidationMapper` nouveau | Sans lui, Quarkus renvoie déjà 400, mais avec **son** format JSON. | — |
| `JacksonMapper` retypé en `MismatchedInputException` | Constaté : Quarkus a un mapper intégré pour ce type exact (`BuiltinMismatchedInputExceptionMapper`, 400 **sans corps**), qui passait devant le nôtre, déclaré sur la classe mère. Même type → le nôtre gagne (vérifié par le test). | — |
| Exceptions JAX-RS < 500 : on garde leur code mais au format commun | Avant, `GlobalExceptionMapper` renvoyait la réponse d'origine telle quelle (texte ou vide). | Je n'ai pas réécrit leurs messages (« HTTP 404 Not Found ») : ils ne révèlent rien de technique. |
| 500 : message générique, vraie erreur dans les logs (`LOG.error`) | Exigence de sécurité du starter (NFR-SEC-03) : ne pas montrer de pile d'appels ni de SQL au client. | — |
| Les messages de `Product` passent aussi au format JSON | Un seul format pour toute l'API. Les scénarios Cucumber du starter (« the error message should contain … ») passent toujours : le message est dans le JSON. | — |

**Notion de cours :** REST JAX-RS, `ExceptionMapper` et `@Provider` (cours REST de M. Bruno) ;
Bean Validation : `ConstraintViolation`, chemin de propriété, message.

**Questions d'oral probables :**

1. *Qui transforme une `NoSuchElementException` en 404 ?* — `NotFoundMapper`, un
   `ExceptionMapper<NoSuchElementException>` annoté `@Provider`. Le service ne connaît pas HTTP ;
   le mapper fait le lien.
2. *Si plusieurs mappers peuvent traiter une exception, lequel est choisi ?* — Celui dont le type
   est le plus proche de la classe de l'exception. `GlobalExceptionMapper` (`Throwable`) ne sert
   que si aucun mapper plus précis n'existe.
3. *Pourquoi ne jamais renvoyer le message d'une exception imprévue au client ?* — Il peut
   contenir des détails internes (requête SQL, nom de classe, pile d'appels) utiles à un attaquant.
   On renvoie un message générique et on écrit la vraie erreur dans les logs pour les développeurs.

### 4.8 JPA + Bean Validation (fonctionnalité 6 ter)

**Ce qui a été fait :** `@NotBlank` sur le champ `username` de `UserEntity`, et le test
`infrastructure/persistence/UserEntityValidationIT` : il crée une `UserEntity` avec
`username = "  "`, appelle `repository.persist(...)` puis `repository.flush()`, et vérifie qu'une
`jakarta.validation.ConstraintViolationException` est lancée.

**Contre-épreuve faite :** en retirant `@NotBlank`, le test **échoue** (la base accepte `"  "`,
qui n'est pas `NULL`). C'est donc bien Hibernate qui applique la contrainte Bean Validation.

**Comment ça marche** (doc Quarkus « Hibernate ORM », section *Validation*, version 3.31) : quand
l'extension `quarkus-hibernate-validator` est présente, la propriété
`quarkus.hibernate-orm.validation.mode` vaut `auto` par défaut, ce qui active deux choses :
- **`callback`** : Hibernate valide l'entité lors des événements de son cycle de vie (avant
  l'insertion et la mise à jour) et lance `ConstraintViolationException` si une contrainte échoue ;
- **`ddl`** : certaines contraintes influencent le schéma généré (par exemple `@NotNull` → `NOT
  NULL`, `@Size(max = 50)` → `varchar(50)`).

C'est le sens de la note du cours : « JPA produit des erreurs en utilisant les annotations
Bean Validation ».

**Analogie.** Bean Validation sur le DTO, c'est le **videur à l'entrée** du restaurant ; sur
l'entité, c'est le **contrôle à la porte de la réserve** : même si quelqu'un est passé par une
autre porte (un test, un script, un futur bot), la réserve refuse une marchandise non conforme.

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| `@NotBlank` sur `UserEntity.username` | La règle existe déjà dans le DTO et le domaine : on ne change pas le comportement de l'API, on montre seulement le mécanisme JPA. `@NotBlank` refuse `"  "`, que `nullable = false` (SQL) laisse passer. | Pas de `@Size(max = 32)` : une longueur maximale **seulement** dans l'entité serait vérifiée au commit de la transaction. L'exception risquerait alors d'arriver enveloppée (pas vérifié), donc en 500 au lieu de 400. |
| `persist` **puis** `flush` dans le test | Comme l'exemple officiel de la doc Quarkus : `flush()` force l'envoi en base tout de suite, donc la validation a lieu pendant le test. | — |
| `@TestTransaction` | Annotation de Quarkus (`io.quarkus.test.TestTransaction`) : le test s'exécute dans une transaction **annulée** (rollback) à la fin ; rien ne reste en base. | Pas de `@Transactional` : il tenterait un commit à la fin d'une transaction en échec. |
| Test d'intégration (`…IT`, `@QuarkusTest`) | Il faut un vrai Hibernate et une vraie base (PostgreSQL de Testcontainers). | — |

**Notion de cours :** Bean Validation, intégration avec JPA (cours Bean Validation de M. Bruno :
validation automatique « avant de persister une entité JPA ») ; cycle de vie d'une entité.

**Questions d'oral probables :**

1. *Comment JPA utilise-t-il les annotations Bean Validation ?* — Hibernate valide l'entité avant
   de l'insérer ou de la mettre à jour ; si une contrainte échoue, il lance une
   `ConstraintViolationException` et rien n'est écrit. Il s'en sert aussi pour le schéma
   (`@NotNull` → colonne `NOT NULL`).
2. *Quelle différence entre `@Column(nullable = false)` et `@NotNull` ?* — `nullable = false` est
   une instruction pour la **base** (contrainte SQL dans le DDL), vérifiée par PostgreSQL.
   `@NotNull` est une contrainte **Bean Validation**, vérifiée en Java par Hibernate Validator
   avant l'envoi en base, avec un message clair.
3. *Pourquoi valider à trois endroits (DTO, domaine, entité) ?* — Le DTO protège l'API et donne
   des messages par champ ; le domaine protège les règles métier quel que soit l'appelant ;
   l'entité est le dernier rempart avant la base. Chaque niveau peut être atteint sans passer par
   les autres.

### 4.9 Introspection : lire une annotation à l'exécution (fonctionnalité 6 quater)

**Fichier :** `src/test/java/org/acme/catalog/product/application/validation/IntrospectionTest.java`
(3 tests).

```java
Field field = CreateUserRequest.class.getDeclaredField("username");
ValidUsername annotation = field.getAnnotation(ValidUsername.class);   // non null
annotation.message();   // "Username must not contain spaces"

CreateUserRequest.class.getRecordComponents()[0].getAnnotation(ValidUsername.class);   // null
User.class.getAnnotation(DomainModel.class);   // null
```

**Ce que montrent les 3 tests :**

1. `@ValidUsername` (`RUNTIME`) se lit **sur le champ** `username` : c'est exactement ce que fait
   le validateur Bean Validation (il reçoit l'instance, fait une introspection de sa classe, trouve
   les annotations de contrainte, puis valide). On peut même lire ses éléments (`message()`).
2. Elle n'est **pas** sur le **composant** du record : avec `@Target(FIELD)`, Java la recopie
   seulement sur le champ. Pour la voir aussi via `getRecordComponents()`, il aurait fallu
   ajouter `ElementType.RECORD_COMPONENT` au `@Target`. C'est pour cela que le test utilise
   `getDeclaredField`.
3. `@DomainModel` (`SOURCE`) est **invisible** à l'exécution : `getAnnotation` renvoie `null`
   alors que `User` porte bien l'annotation dans son source. Elle a disparu à la compilation,
   après avoir été lue par le processeur.

**Tableau de synthèse à savoir par cœur :**

| | Introspection | Processeur d'annotations |
| --- | --- | --- |
| Quand ? | À l'**exécution** | À la **compilation** (dans `javac`) |
| Qui lit ? | Le code Java, par réflexion (`getAnnotation`) | Une classe qui étend `AbstractProcessor` |
| Rétention minimale | `RUNTIME` | `SOURCE` suffit |
| Ce qu'on manipule | des `Class`, `Field`, `Method` (code compilé) | des `Element` (représentation du source) |
| Peut… | lire et agir sur les objets (valider, injecter…) | refuser de compiler (`Messager`), générer du code (`Filer`) |
| Dans le projet | Bean Validation (`@ValidUsername`), Hibernate (`@Entity`), Quarkus (`@Path`) | `DomainModelProcessor` (`@DomainModel`) |

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Un test, pas du code de production | Consigne : montrer le mécanisme sur ton propre code ; un test se relance à volonté et prouve ce qu'il affirme. | Pas d'« introspecteur » maison dans l'application : Bean Validation le fait déjà. |
| `getDeclaredField` (et non `getField`) | `getField` ne voit que les champs **publics** ; les champs d'un record sont **privés**. | — |
| Test 2 sur `getRecordComponents()` | Il répond d'avance à la question « pourquoi pas `getRecordComponents()` ? » et prouve la règle de propagation des annotations des records. | Pas d'ajout de `RECORD_COMPONENT` au `@Target` : inutile pour Bean Validation. |

**Notion de cours :** lecture des annotations par introspection (cours Annotations 1/2 :
`getDeclaredMethods()`, `getAnnotation()`, `invoke()`), et lien avec les processeurs (cours 2/2).

**Questions d'oral probables :**

1. *Pourquoi une annotation en `RetentionPolicy.SOURCE` est-elle inutilisable par Hibernate ou
   Quarkus ?* — Parce qu'ils lisent les annotations à l'exécution par réflexion ; une annotation
   `SOURCE` n'est plus dans le `.class`, `getAnnotation` renvoie `null` (prouvé par le test 3).
2. *Comment le validateur trouve-t-il `@ValidUsername` ?* — Par introspection : il prend la classe
   de l'objet reçu, parcourt ses champs (`getDeclaredFields()`), appelle `getAnnotation` et
   regarde quelles annotations sont des contraintes (`@Constraint`).
3. *Pourquoi `getDeclaredField` et pas `getField` ?* — `getField` ne renvoie que les champs
   publics, hérités compris ; `getDeclaredField` renvoie tous les champs déclarés dans la classe,
   privés compris. Les champs d'un record sont privés.

### 4.10 Tests : unitaires (JUnit + Mockito) et d'intégration (Testcontainers) (fonctionnalité 7)

**Les deux familles de tests du projet :**

| | Test unitaire | Test d'intégration |
| --- | --- | --- |
| Nom de classe | `…Test` | `…IT` |
| Lancé par | Surefire (`./mvnw test`) | Failsafe (phase `verify` : `./mvnw verify`) |
| Démarre Quarkus ? | Non | Oui (`@QuarkusTest`) |
| Base de données | Aucune : le repository est un **mock** Mockito | Un **vrai PostgreSQL** dans un conteneur Docker (Testcontainers) |
| Vitesse | Millisecondes | Quelques secondes (démarrage du conteneur et de Quarkus) |
| Exemples | `UserServiceTest`, `ValidUsernameValidatorTest`, `DiscordBotStartupTest`, `DomainModelProcessorTest`, `IntrospectionTest`, `GlobalExceptionMapperTest`, ArchUnit | `UserResourceIT`, `GuildResourceIT`, `RoleResourceIT`, `ChannelResourceIT`, `ErrorFormatIT`, `UserEntityValidationIT` |

**Nouveau : `application/UserServiceTest`** (5 tests) — création (pseudo libre → `persist`
appelé ; doublon → `IllegalStateException` et `persist` **jamais** appelé), recherche, modification,
utilisateur inconnu.

```java
repository = mock(UserRepository.class);              // faux repository
service = new UserService(repository);                // le vrai service, sans Quarkus
when(repository.findByUsername("yannis")).thenReturn(Optional.empty());   // on décide la réponse
service.create(new CreateUserRequest("yannis", "Yannis"));
verify(repository).persist(any(UserEntity.class));    // on vérifie l'appel
```

**D'où vient le PostgreSQL des tests d'intégration** (vérifié dans la doc Quarkus 3.31,
« Testing » et « Dev Services ») :
- la classe du starter `src/test/java/org/acme/testresources/PostgreSqlTestResource` démarre un
  conteneur `postgres:15-alpine` avec **Testcontainers** et donne son URL à Quarkus ;
- elle est déclarée sur `CatalogAcceptanceIT` avec `@QuarkusTestResource`, mais les ressources
  de test sont **globales** : elle sert à **tous** les `@QuarkusTest` (doc : « Test resources are
  global… ») ;
- les **Dev Services** de Quarkus (qui utilisent aussi Testcontainers) ne sont **pas** utilisés
  ici : ils ne démarrent que si l'URL de la base n'est pas configurée, or `application.properties`
  la définit.

Docker doit donc tourner pour `./mvnw verify`.

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Tester `UserService` | C'est le service que tu as écrit ; il a de la logique (doublon, recherche, modification). | Pas de test Mockito pour les 3 autres services : même schéma, rien de nouveau à montrer. |
| `mock(...)` dans `@BeforeEach` | Lisible : on voit tout dans la méthode. | Pas de `@Mock` + `MockitoAnnotations.openMocks` (utilisé par `ProductServiceTest`) : une étape magique de plus à expliquer. |
| Réutiliser `PostgreSqlTestResource` | Elle existe, marche, et correspond au tableau (« Test ? → TestContainers »). | Pas de H2 : le tableau tranche pour Testcontainers ; et H2 n'est pas PostgreSQL (dialecte, types). Pas de bascule vers les Dev Services : il faudrait retirer l'URL de la configuration, sans rien gagner. |

**Notion de cours :** tests unitaires et d'intégration (sujet I311) ; mock ; Testcontainers.

**Questions d'oral probables :**

1. *Quelle différence entre un test unitaire et un test d'intégration dans ton projet ?* — Le test
   unitaire teste une classe seule, ses dépendances sont des mocks (pas de base, pas de Quarkus) ;
   le test d'intégration démarre l'application avec une vraie base PostgreSQL et envoie de vraies
   requêtes HTTP.
2. *À quoi sert `verify(repository, never()).persist(any())` ?* — À prouver que, en cas de
   doublon, le service n'enregistre rien : on vérifie un **comportement** (un appel qui n'a pas
   eu lieu), pas seulement une valeur renvoyée.
3. *Pourquoi Testcontainers plutôt que H2 ?* — On teste contre le **même** SGBD qu'en production
   (PostgreSQL), dans un conteneur jetable créé pour les tests. H2 peut accepter ou refuser des
   choses différemment.

### 4.11 Appartenance User ↔ Guild : `@ManyToMany` (fonctionnalité 8, partie 1)

**Ce qui a été fait :**

```java
// GuildEntity : côté PROPRIÉTAIRE, décrit la table de jointure
@ManyToMany
@JoinTable(name = "guild_members",
        joinColumns = @JoinColumn(name = "guild_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
private Set<UserEntity> members = new HashSet<>();

// UserEntity : côté INVERSE
@ManyToMany(mappedBy = "members")
private Set<GuildEntity> guilds = new HashSet<>();
```

Plus `GET /api/v1/guilds/{id}/members` (`GuildService.getMembers`) et, dans
`UserService.delete`, une boucle qui retire l'utilisateur de ses guildes avant de le supprimer.
Test : `GuildResourceIT.membersOfNewGuildIsEmpty`. On **devient** membre par une invitation (4.12).

**En base**, une `@ManyToMany` devient une **table de jointure** à deux colonnes :

```
guilds (id, name, …)      guild_members (guild_id, user_id)      users (id, username, …)
```

**Analogie.** La table de jointure, c'est le **registre des inscriptions** d'un club : une ligne
« tel membre, tel club ». Le club (la guilde) tient le registre (côté propriétaire) ; le membre
garde seulement une copie de ses clubs (côté inverse), qui ne fait pas foi.

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| `@ManyToMany` simple | Consigne ; prévue dans l'UML. | Pas d'entité `Membership` (date d'arrivée, surnom) : une `@ManyToMany` ne peut **pas** porter d'attribut sur le lien. Pour en avoir, il faudrait une entité `Membership` et deux `@ManyToOne` (Membership → User, Membership → Guild). |
| Guilde = côté **propriétaire** | L'action métier « rejoindre une guilde » modifie la guilde : `guild.getMembers().add(user)`. Seul le côté propriétaire est lu par Hibernate pour écrire la table de jointure. Supprimer une guilde supprime ses lignes de jointure automatiquement. | — |
| Utilisateur = côté **inverse** (`mappedBy`) | Nécessaire pour retrouver les guildes d'un utilisateur au moment de le supprimer. | — |
| Boucle de retrait dans `UserService.delete` | Hibernate n'efface les lignes de jointure que depuis le côté **propriétaire**. Sans la boucle, supprimer un membre échouerait à cause de la clé étrangère `user_id` de `guild_members`. | Pas de `cascade` vers les utilisateurs : supprimer une guilde ne doit pas supprimer ses membres. |
| `Set` et non `List` | Un utilisateur n'est membre qu'une fois ; `add` d'un élément déjà présent ne fait rien. | Pas de `equals`/`hashCode` redéfinis sur les entités : dans un même contexte de persistance, Hibernate garantit **un seul objet Java par ligne**, donc l'égalité par référence (celle d'`Object`) suffit ici. |
| Chargement **LAZY** (par défaut pour `@ManyToMany`) | Lire une guilde ne charge pas tous ses membres ; ils sont lus au premier accès à `getMembers()`. | — |
| `GET /{id}/members` | Seul moyen de voir et tester l'appartenance. | Pas de `GET /users/{username}/guilds` : non demandé. |
| Pas de `@ManyToMany` User ↔ Role | Hors du §0 (seul User ↔ Guild est demandé). | — |

**Notion de cours :** JPA, relations `@ManyToMany`, côté propriétaire / côté inverse
(`mappedBy`), table de jointure, LAZY / EAGER (cours JPA de M. Bruno).

**Questions d'oral probables :**

1. *Qu'est-ce que le côté propriétaire d'une relation ?* — Le côté dont Hibernate lit l'état pour
   écrire en base (ici la table `guild_members`). L'autre côté porte `mappedBy` : le modifier seul
   n'a aucun effet en base.
2. *`@ManyToMany` : EAGER ou LAZY par défaut ?* — LAZY (comme `@OneToMany`). `@ManyToOne` et
   `@OneToOne` sont EAGER par défaut.
3. *Pourquoi une `@ManyToMany` ne peut-elle pas porter la date d'arrivée d'un membre ?* — Parce que
   la table de jointure ne contient que les deux clés étrangères. Pour ajouter une donnée sur le
   lien, on crée une entité `Membership` : la `@ManyToMany` devient deux `@ManyToOne`
   (`Membership` → `User` et `Membership` → `Guild`), et chaque côté a une `@OneToMany` inverse.

### 4.12 Lien d'invitation « Invitation Link » (fonctionnalité 8, partie 2)

**Endpoints :**

| Requête | Corps | Réponse | Codes |
| --- | --- | --- | --- |
| `POST /api/v1/guilds/{id}/invitations` | aucun | `{"code": "3f2a9c1b", "guildId": "…", "expiresAt": "…"}` | 201, 404 (guilde) |
| `POST /api/v1/invitations/{code}/join` | `{"username": "Mirnis84"}` | la guilde rejointe (`GuildDTO`) | 200, 400, 404 (code ou utilisateur), **410** (expiré) |

**Fichiers (même schéma 4 couches que les autres entités) :**

| Couche | Fichier | Rôle |
| --- | --- | --- |
| domain | `Invitation` | Record : id, code, guildId, expiresAt. `of(guildId)` génère le code (8 premiers caractères d'un UUID) et l'expiration (`now + VALIDITY`, 7 jours). Règle métier `isExpired(now)`. `@DomainModel`. |
| infrastructure | `InvitationEntity` | Table `invitations` ; `@ManyToOne` vers la guilde (`guild_id`) ; `code` `unique`. |
| infrastructure | `InvitationRepository` | `findByCode`. |
| infrastructure | `InvitationMapper` | `toDto`, `toEntity(invitation, guild)`, `toDomain`. |
| infrastructure | `GuildEntity.invitations` | `@OneToMany(mappedBy, cascade = ALL, orphanRemoval = true)` : supprimés avec la guilde. |
| application | `InvitationService` | `create(guildId)` ; `join(code, request)` : 404, puis 410 si expiré, puis 404 si l'utilisateur n'existe pas, puis `guild.getMembers().add(user)`. |
| application | `InvitationDTO`, `usecases/JoinGuildRequest` | JSON renvoyé / reçu. |
| application | `InvitationExpiredException` | Exception dédiée au lien expiré. |
| api | `InvitationResource` (`/api/v1/invitations`), `GuildResource.createInvitation` | Les deux endpoints. |
| api | `error/GoneMapper` | `InvitationExpiredException` → 410, au format commun. |

Tests : `domain/InvitationTest` (code, durée, `isExpired`) et `api/InvitationResourceIT` (créer →
rejoindre → membre visible → suppression du membre ; code inconnu 404 ; lien expiré 410, en
enregistrant directement en base un lien expiré avec `QuarkusTransaction.requiringNew().run(...)`).

**Les choix et leur justification :**

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| Une **entité** `Invitation`, pas un champ de `Guild` | Une guilde peut avoir plusieurs liens en même temps, chacun avec sa date d'expiration. | Pas de nombre d'utilisations maximal, pas de révocation, pas de « créé par » : consigne « le minimum complet ». |
| Code = 8 premiers caractères d'un UUID aléatoire | Court à partager, aucune bibliothèque. L'unicité est garantie par `unique = true` en base. | Pas de nouvel essai en cas de collision (très improbable) : la contrainte SQL ferait échouer la création (500), on pourrait alors réessayer. |
| Durée fixe de 7 jours (`VALIDITY`) | La durée par défaut des invitations Discord ; aucun corps à envoyer à la création. | Pas de durée choisie par le client : paramètre optionnel, hors consigne. |
| `isExpired(Instant now)` dans le **domaine** | C'est une règle métier ; recevoir `now` en paramètre la rend testable sans attendre 7 jours. | — |
| **410 Gone** pour un lien expiré | 410 veut dire « la ressource a existé mais n'est plus disponible », exactement le cas d'un lien expiré. 400 voudrait dire « ta requête est mal formée », ce qui est faux : la requête est correcte. | Coût : une exception (`InvitationExpiredException`) et un mapper (`GoneMapper`), car aucune exception Java standard ne signifie « expiré ». Le service reste indépendant de HTTP. |
| `join` reçoit le pseudo | Les utilisateurs sont identifiés par pseudo dans toute l'API (`/users/{username}`). | Pas d'authentification : le projet n'en a pas, on dit qui rejoint dans le corps. |
| Rejoindre deux fois → 200 sans doublon | `members` est un `Set` : `add` d'un membre déjà présent ne fait rien. | Pas de 409 « déjà membre » : non demandé, et le résultat voulu (être membre) est atteint. |
| `POST` de création **sans corps** + `@Consumes(MediaType.WILDCARD)` | Constaté : la classe déclare `@Consumes(JSON)`, et un `POST` sans `Content-Type` (Swagger UI, `curl -X POST`) recevait 415. L'annotation de la méthode remplace celle de la classe. | — |
| `POST /invitations/{code}/join` (et non `PUT`) | « Rejoindre » est une **action** qui change l'état ; on suit l'énoncé du §0. | — |

**Notion de cours :** modélisation (entité, relation `@ManyToOne` / `@OneToMany` avec cascade),
règles métier dans le domaine, codes HTTP (410 vs 400 vs 404), `ExceptionMapper`.

**Questions d'oral probables :**

1. *Pourquoi 410 et pas 404 ou 400 pour un lien expiré ?* — 404 : la ressource n'existe pas (code
   inconnu). 400 : la requête est mal formée. 410 Gone : la ressource **a existé** mais n'est plus
   utilisable, c'est le cas d'un lien expiré. Le client peut afficher « lien expiré, demandez-en
   un nouveau ».
2. *Où est la règle « expiré » et pourquoi reçoit-elle `now` en paramètre ?* — Dans le record
   `Invitation` (domaine) : `isExpired(Instant now)`. Avec `now` en paramètre, le test vérifie la
   règle avec une date dans 7 jours, sans attendre et sans dépendre de l'horloge.
3. *Que se passe-t-il en base quand un utilisateur rejoint une guilde ?* — Le service ajoute
   l'utilisateur au `Set` `members` de la `GuildEntity` (côté propriétaire, entité managée) ; au
   commit, Hibernate insère une ligne `(guild_id, user_id)` dans `guild_members`.

### 4.13 README du projet (fonctionnalité 9)

`README.md` remplace celui du starter : présentation, architecture, prérequis, installation
(conteneur PostgreSQL), lancement (avec et sans bot), tests, **tableau de tous les endpoints avec
leurs codes**, format des erreurs, et ce qui reste à faire.

| Choix | Pourquoi | Ce que je n'ai pas fait, et pourquoi |
| --- | --- | --- |
| En français | Projet et cours en français. | — |
| Section « Pas encore fait » | Honnêteté sur l'état du projet : pagination, Flyway, Dockerfile multi-étages, `docker-compose` adapté, CI dédiée et LLM sont prévus pour le rendu final (priorité D). | Je n'ai pas documenté `docker compose up` : le `compose.yml` actuel est celui du starter (image `brunoe/product-catalog`), il ne lance pas ce projet. |
| Une commande par bloc | Copier-coller sûr sous zsh. | — |

**Question d'oral probable :** *Comment lance-t-on ton projet de zéro ?* — Démarrer PostgreSQL
(`docker start product-catalog-postgres`), puis `DB_PORT=15432 ./mvnw quarkus:dev` ; Swagger UI
sur `/swagger-ui/`. Les tests : `./mvnw clean verify` avec Docker lancé.
