# Bot Discord « ping-pong »

Le bot répond `pong!` quand quelqu'un écrit `!ping` dans un salon où il est présent.
Code : `src/main/java/org/acme/catalog/product/infrastructure/discord/`.

Sans jeton, l'application démarre normalement et le log affiche :
`DISCORD_TOKEN is not set: the Discord bot is disabled`. C'est le cas des tests et de la CI.

## 1. Ce que tu dois faire sur le portail développeur de Discord

Ces étapes se font à la main, avec ton compte Discord, sur
<https://discord.com/developers/applications>. Les noms de boutons peuvent changer légèrement
d'une version du portail à l'autre.

1. **Créer l'application** : bouton « New Application », donne-lui un nom (par exemple
   `I311-bot`), accepte les conditions.
2. **Récupérer le jeton du bot** : menu de gauche « Bot », bouton « Reset Token », puis copie le
   jeton affiché. Il n'est montré qu'une fois. **Ne le colle jamais dans le code ni dans git.**
   Attention : c'est le jeton de la section « Bot », pas le « Client Secret » ni la « Public Key »
   de la page « General Information » (Discord4J refuserait le démarrage avec « Invalid token »).
3. **Activer l'intent « Message Content »** : toujours dans « Bot », section
   « Privileged Gateway Intents », active **Message Content Intent** et enregistre.
   Pourquoi : sans lui, Discord envoie au bot des messages dont le contenu est vide, et le bot ne
   voit jamais `!ping`. Le code le demande aussi de son côté (`Intent.MESSAGE_CONTENT`).
4. **Inviter le bot sur un serveur** : menu « OAuth2 », puis « URL Generator ».
   Coche le scope **`bot`**, puis les permissions **View Channels** et **Send Messages**.
   Copie l'URL générée en bas, ouvre-la dans le navigateur et choisis ton serveur de test
   (il faut avoir le droit « Gérer le serveur » sur ce serveur ; le plus simple est d'en créer un).

## 2. Lancer l'application avec le jeton

Le jeton passe par la variable d'environnement `DISCORD_TOKEN`. Quarkus (MicroProfile Config) la
lit comme la propriété `discord.token`.

PostgreSQL d'abord :

```bash
docker start product-catalog-postgres
```

Puis l'application, en remplaçant `ton-jeton` par le jeton copié à l'étape 2 :

```bash
DISCORD_TOKEN=ton-jeton DB_PORT=15432 ./mvnw quarkus:dev
```

Dans le log, tu dois voir `Discord bot started`. Écris `!ping` dans un salon du serveur : le bot
répond `pong!`.

Messages possibles dans le log :

| Message | Signification |
| --- | --- |
| `DISCORD_TOKEN is not set: the Discord bot is disabled` | Pas de jeton : bot désactivé, l'API fonctionne. |
| `Discord bot could not start: Invalid token…` | Le jeton n'a pas le bon format (souvent : Client Secret copié à la place du jeton du bot). |
| `Discord connection failed: … 401 Unauthorized` | Jeton au bon format mais refusé par Discord (jeton régénéré entre-temps ?). |
| `Unable to load io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider` | Sans conséquence sur macOS : Netty utilise le DNS du système. |

## 3. Sécurité du jeton

- Le jeton n'est **jamais** dans le code ni dans `application.properties`.
- `.env.example` (commité) montre la variable sans valeur ; un vrai `.env` est ignoré par git
  (`.gitignore`).
- Si le jeton a fuité (commit, capture d'écran), régénère-le sur le portail (« Reset Token ») :
  l'ancien cesse immédiatement de fonctionner.
