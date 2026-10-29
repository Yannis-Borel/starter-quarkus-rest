# Bot Discord (projet `discord-bot/`)

Le bot est un **second projet Quarkus**, indépendant de l'API, dans le dossier `discord-bot/`
(son propre `pom.xml` et son propre `./mvnw`). Il parle à l'API par **HTTP**, comme n'importe
quel client.

| Commande | Réponse | Appel à l'API ? |
| --- | --- | --- |
| `!ping` | `pong!` | non |
| `!users` | les pseudos, par exemple `alice, bob` (ou `No users yet`, ou `Cannot reach the API`) | oui : `GET /api/v1/users` |

```
Discord ──(passerelle, Discord4J)──▶ bot (port 8081) ──HTTP GET /api/v1/users──▶ API (port 8080) ──▶ PostgreSQL
```

Sans jeton, l'application du bot démarre normalement et le log affiche :
`DISCORD_TOKEN is not set: the Discord bot is disabled`. C'est le cas des tests et de la CI.

## 1. Ce que tu dois faire sur le portail développeur de Discord

Sur <https://discord.com/developers/applications>. Les noms de boutons peuvent changer légèrement.

1. **Créer l'application** : bouton « New Application », un nom (par exemple `I311-bot`).
2. **Récupérer le jeton** : menu « Bot », bouton « Reset Token », copie le jeton. Il n'est montré
   qu'une fois. **Ne le colle jamais dans le code, dans git, dans une discussion ou une capture.**
   C'est le jeton de la section « Bot », pas le « Client Secret » ni la « Public Key ».
3. **Activer l'intent « Message Content »** : toujours dans « Bot », section
   « Privileged Gateway Intents », active **Message Content Intent** puis **Save Changes**.
   Sans lui, Discord **refuse la connexion** : le bot reste **hors ligne**, même s'il est
   administrateur du serveur (les permissions du serveur et les intents du portail sont deux
   réglages différents).
4. **Inviter le bot** : menu « OAuth2 » → « URL Generator », scope **`bot`**, permissions
   **View Channels** et **Send Messages** (le minimum nécessaire : moindre privilège). Ouvre l'URL
   générée et choisis ton serveur de test.

## 2. Lancer les deux projets

Il faut **deux terminaux**, un par projet.

Terminal 1 — PostgreSQL, puis l'API (port 8080), depuis la racine du dépôt :

```bash
docker start product-catalog-postgres
```

```bash
DB_PORT=15432 ./mvnw quarkus:dev
```

Terminal 2 — le bot (port 8081), en remplaçant `ton-jeton` :

```bash
cd discord-bot
```

```bash
DISCORD_TOKEN=ton-jeton ./mvnw quarkus:dev
```

Par défaut, le bot appelle l'API sur `http://localhost:8080`. Pour une autre adresse, ajoute
`API_URL=http://autre-adresse:port` devant la commande.

Vérifications :

- dans le log du bot : `Discord bot started`, puis le bot passe **en ligne** dans Discord ;
- l'application du bot répond sur <http://localhost:8081/q/health> ;
- dans un salon : `!ping` → `pong!`, `!users` → la liste des pseudos.

Tests du bot :

```bash
cd discord-bot
```

```bash
./mvnw clean verify
```

## 3. Dépannage

| Symptôme / message | Cause probable |
| --- | --- |
| `DISCORD_TOKEN is not set: the Discord bot is disabled` | Variable absente : elle doit être sur la même ligne que `./mvnw`. |
| `Discord bot could not start: Invalid token…` | Mauvais jeton copié (Client Secret au lieu du jeton de la section Bot). |
| `Discord connection failed: … 401 Unauthorized` | Jeton refusé (régénéré entre-temps ?). |
| `Discord bot started` mais le bot reste **hors ligne** | Intent **Message Content** non activé ou non enregistré sur le portail (vécu le 28/10). |
| `Port 8081 seems to be in use` (ou 8080 pour l'API) | Une ancienne instance tourne encore, souvent dans un onglet de terminal fermé. Trouver son numéro avec `lsof -nP -iTCP:8081 -sTCP:LISTEN`, puis `kill -9 <numéro>`. |
| `!users` répond `Cannot reach the API` | L'API n'est pas lancée, ou `API_URL` est faux. |
| `Unable to load io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider` | Sans conséquence sur macOS. |

Pour arrêter Quarkus en mode dev : touche `q`, ou `control + C` (`⌃`, pas `⌘`).

## 4. Sécurité du jeton

- Le jeton n'est **jamais** dans le code ni dans `application.properties` : variable
  d'environnement `DISCORD_TOKEN` seulement.
- `discord-bot/.env.example` (commité) montre les variables sans valeur ; un vrai `.env` est ignoré
  par git.
- Si le jeton a fuité, régénère-le sur le portail (« Reset Token ») : l'ancien cesse
  immédiatement de fonctionner.
