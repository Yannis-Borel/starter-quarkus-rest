# API Discord — projet I311 (Quarkus)

API REST et bot inspirés de Discord : utilisateurs, guildes (serveurs), salons, rôles et liens
d'invitation. Projet fil rouge du cours I311 « Révision de Java, Docker et Git »
(M2 Informatique, Université de Toulon), construit à partir du
[starter Quarkus REST](https://github.com/ebpro/starter-quarkus-rest) de M. Bruno.

**Technologies :** Java 21, Quarkus 3.31 (REST, Hibernate ORM Panache, Hibernate Validator,
SmallRye OpenAPI), PostgreSQL, Discord4J, JUnit 5, Mockito, Testcontainers, ArchUnit.

## Architecture

Quatre couches, dans `src/main/java/org/acme/catalog/product/` :

```
api/            ressources REST (JAX-RS), mappers d'erreurs (api/error)
application/    services (cas d'usage, transactions), DTO, contrainte @ValidUsername
domain/         records immuables : User, Guild, Channel, Role, Invitation (aucun framework)
infrastructure/ entités JPA, repositories Panache, mappers, bot Discord
```

Les règles de dépendance entre couches sont vérifiées par des tests **ArchUnit**
(`src/test/java/org/acme/architecture/`). Un processeur d'annotations
(`src/main/java/org/acme/processor/`) vérifie à la compilation que les classes `@DomainModel`
sont des records.

Documentation détaillée :

- [docs/GUIDE-ORAL.md](docs/GUIDE-ORAL.md) — rôle de chaque fichier, trajet des requêtes, choix
  de conception ;
- [docs/modele-uml.md](docs/modele-uml.md) — modèle de données (UML) ;
- [docs/architecture-discord.md](docs/architecture-discord.md) — vue d'ensemble des couches ;
- [docs/discord-bot.md](docs/discord-bot.md) — configuration du bot Discord.

## Prérequis

- JDK 21 ou plus récent ;
- Docker (pour PostgreSQL en local et pour les tests d'intégration) ;
- aucun Maven à installer : le projet fournit `./mvnw`.

## Installation

Cloner le dépôt :

```bash
git clone https://github.com/Yannis-Borel/starter-quarkus-rest.git
```

Créer le conteneur PostgreSQL (une seule fois ; port 15432 côté machine) :

```bash
docker run --name product-catalog-postgres -e POSTGRES_DB=products -e POSTGRES_USER=tpuser -e POSTGRES_PASSWORD=Tp@2026 -p 15432:5432 -d postgres:16-alpine
```

Les fois suivantes, il suffit de le redémarrer :

```bash
docker start product-catalog-postgres
```

Le schéma de la base est créé automatiquement par Hibernate au démarrage
(`quarkus.hibernate-orm.schema-management.strategy=update`).

## Lancement

Mode dev (rechargement à chaud) :

```bash
DB_PORT=15432 ./mvnw quarkus:dev
```

| Quoi | URL |
| --- | --- |
| API REST | <http://localhost:8080/api/v1/> |
| Swagger UI | <http://localhost:8080/swagger-ui/> |
| Santé | <http://localhost:8080/q/health> |
| Dev UI | <http://localhost:8080/q/dev-ui> |

### Avec le bot Discord

Le bot répond `pong!` à `!ping`. Il ne démarre que si la variable `DISCORD_TOKEN` est définie ;
sans elle, l'API fonctionne normalement. Étapes sur le portail Discord :
[docs/discord-bot.md](docs/discord-bot.md).

```bash
DISCORD_TOKEN=ton-jeton DB_PORT=15432 ./mvnw quarkus:dev
```

Le jeton ne doit jamais être commité (voir [.env.example](.env.example)).

## Tests

Tous les tests (unitaires, ArchUnit, intégration avec un PostgreSQL lancé par Testcontainers,
scénarios Cucumber du starter). Docker doit tourner.

```bash
./mvnw clean verify
```

Seulement les tests unitaires :

```bash
./mvnw test
```

| Type | Exemples | Lancé par |
| --- | --- | --- |
| Unitaires (JUnit, Mockito) | `UserServiceTest`, `ValidUsernameValidatorTest`, `DiscordBotStartupTest`, `InvitationTest` | `./mvnw test` |
| Processeur d'annotations | `DomainModelProcessorTest` | `./mvnw test` |
| Architecture (ArchUnit) | `src/test/java/org/acme/architecture/` | `./mvnw test` |
| Intégration REST (Quarkus + PostgreSQL) | `UserResourceIT`, `GuildResourceIT`, `RoleResourceIT`, `ChannelResourceIT`, `InvitationResourceIT`, `ErrorFormatIT` | `./mvnw verify` |
| JPA + Bean Validation | `UserEntityValidationIT` | `./mvnw verify` |

## Endpoints

Préfixe : `/api/v1`. Corps en JSON.

### Utilisateurs

| Verbe | Chemin | Rôle | Codes |
| --- | --- | --- | --- |
| GET | `/users` | Liste ; `?search=texte` filtre sur le pseudo ou le nom affiché | 200 |
| GET | `/users/{username}` | Un utilisateur | 200, 404 |
| POST | `/users` | Création `{"username", "displayName"}` (pseudo sans espace) | 201, 400, 409 |
| PUT | `/users/{username}` | Modification `{"displayName"}` | 200, 400, 404 |
| DELETE | `/users/{username}` | Suppression | 204, 404 |

### Guildes

| Verbe | Chemin | Rôle | Codes |
| --- | --- | --- | --- |
| GET | `/guilds` | Liste | 200 |
| GET | `/guilds/{id}` | Une guilde | 200, 404 |
| GET | `/guilds/{id}/stats` | Nombre de salons par type, ex. `{"TEXT": 2, "VOICE": 1}` | 200, 404 |
| GET | `/guilds/{id}/members` | Membres | 200, 404 |
| POST | `/guilds` | Création `{"name"}` | 201, 400 |
| POST | `/guilds/{id}/invitations` | Crée un lien d'invitation valable 7 jours (sans corps) | 201, 404 |
| PUT | `/guilds/{id}` | Modification `{"name"}` | 200, 400, 404 |
| DELETE | `/guilds/{id}` | Suppression, avec ses salons, rôles et invitations | 204, 404 |

### Salons

| Verbe | Chemin | Rôle | Codes |
| --- | --- | --- | --- |
| GET | `/channels` | Liste ; `?guildId=…` filtre par guilde | 200 |
| GET | `/channels/{id}` | Un salon | 200, 404 |
| POST | `/channels` | Création `{"guildId", "name", "type": "TEXT" ou "VOICE", "topic"}` | 201, 400, 404, 409 |
| PUT | `/channels/{id}` | Modification `{"name", "topic"}` | 200, 400, 404, 409 |
| DELETE | `/channels/{id}` | Suppression | 204, 404 |

### Rôles

| Verbe | Chemin | Rôle | Codes |
| --- | --- | --- | --- |
| GET | `/roles` | Liste ; `?guildId=…` filtre par guilde | 200 |
| GET | `/roles/{id}` | Un rôle | 200, 404 |
| POST | `/roles` | Création `{"guildId", "name", "position"}` | 201, 400, 404, 409 |
| PUT | `/roles/{id}` | Modification `{"name", "position"}` | 200, 400, 404, 409 |
| DELETE | `/roles/{id}` | Suppression | 204, 404 |

### Invitations

| Verbe | Chemin | Rôle | Codes |
| --- | --- | --- | --- |
| POST | `/invitations/{code}/join` | Rejoindre la guilde du lien `{"username"}` | 200, 400, 404, 410 (lien expiré) |

### Format des erreurs

Toutes les erreurs ont le même format :

```json
{ "status": 400, "error": "Bad Request", "message": "Validation failed",
  "fields": [ { "field": "username", "message": "Username must not contain spaces" } ] }
```

`fields` n'est rempli que pour les erreurs de validation.

## Pas encore fait (prévu pour le rendu final)

Pagination et filtres sur toutes les listes, migrations Flyway, Dockerfile multi-étages et
`docker-compose` adaptés au projet Discord (le `compose.yml` actuel est celui du starter),
CI GitHub Actions dédiée, extensions LLM.

Les exemples du starter (`/api/v1/products`, `products.http`, scénarios Cucumber) sont conservés
comme référence.
