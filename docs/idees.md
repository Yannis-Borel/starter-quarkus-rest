# Idées pour personnaliser le projet

Pistes recueillies le 2 octobre 2026. Le bot Discord est une **extension bonus** du sujet
(10 % de la note) : les demandes du professeur (annotations, processeur d'annotations,
programmation fonctionnelle) passent avant.

Pour chaque idée : **originalité** (existe-t-il déjà quelque chose de connu ?) et
**difficulté**. Les points qui dépendent d'une fonction précise de l'API Discord sont
marqués *à vérifier* dans la documentation officielle avant de coder.

## Idée retenue

### Bot « temps de parole » ⭐

Le bot note qui rejoint ou quitte un salon vocal, et à quelle heure. Il stocke ces sessions
en base, puis calcule des statistiques : temps passé en vocal par personne, salon le plus
fréquenté, heures de pointe. Accès par une commande `/stats` ou un endpoint REST.

- **Originalité :** peu courant sous cette forme.
- **Difficulté :** moyenne.
- **Ce que ça apporte au sujet :** une nouvelle entité JPA (session vocale) liée à `User` et
  `Channel` ; de la programmation fonctionnelle naturelle (`Collectors.groupingBy`, sommes de
  durées) ; un endpoint de statistiques.



### Annuaire de compétences 🧠

`/skill add docker`, puis `/ask docker` : le bot mentionne les membres qui s'y connaissent.
Leur niveau monte à chaque aide apportée.

- **Originalité :** rare.
- **Difficulté :** facile.
- **Ce que ça apporte au sujet :** une vraie relation `@ManyToMany` User ↔ Skill.

### Tireur de binômes équitable 👥

`/binomes TP3` : le bot forme des binômes sans jamais remettre ensemble des personnes qui ont
déjà travaillé ensemble, en équilibrant les compétences.

- **Originalité :** aucune version connue.
- **Difficulté :** moyenne.
- **Ce que ça apporte au sujet :** un vrai petit algorithme, idéal pour des tests JUnit.

### Révision d'oral 🎓

`/quiz java` pose des questions d'oral et repose plus souvent celles qui ont été ratées.

- **Originalité :** les bots de quiz existent ; la révision adaptée à un cours précis, beaucoup
  moins.
- **Difficulté :** facile sans IA, moyenne avec.

## Le pont avec le développement

### Surveillance des dépôts GitHub 🔔

Quand quelqu'un pousse du code ou que la CI échoue, le bot l'annonce dans un salon. GitHub
envoie lui-même une notification (*webhook*) à l'API REST du projet.

- **Originalité :** des intégrations existent, mais rarement faites maison dans un projet étudiant.
- **Difficulté :** moyenne.
- **Ce que ça apporte au sujet :** réutilise l'API REST, la CI et Docker.

## La vie du serveur

### Capsule temporelle ⏳

`/capsule 6mois "Je vais avoir mon M2 ?"` : le bot garde le message et le republie à l'échéance.

- **Originalité :** rare.
- **Difficulté :** facile (une tâche planifiée et une table).

### Salons qui vieillissent 🍂

Un salon inutilisé depuis 30 jours est proposé à l'archivage ; les membres votent pour le garder.

- **Originalité :** peu courant.
- **Difficulté :** facile à moyenne.

### Le « merci » qui compte 🙏

`/merci @quelqu'un` donne un point. Classement mensuel des membres les plus serviables, avec des
règles anti-triche : pas de merci à soi-même, au maximum 3 par jour.

- **Originalité :** les systèmes de points existent.
- **Difficulté :** facile. Les règles anti-triche font de bonnes règles métier à tester.

## Avec une IA (extension LLM du sujet)

### Résumé pour les absents 📝

`/rattrapage` : le bot résume ce qui s'est dit dans un salon depuis la dernière visite.

- **Originalité :** Discord a déjà testé une fonction de résumé, donc moins original.
- **Difficulté :** moyenne.

## Idées écartées

### Moteur de permissions façon Discord

Permissions portées par les rôles (`EnumSet<Permission>`), calcul des permissions effectives
d'un membre, respect de la hiérarchie des rôles.

- **Pourquoi écartée :** ne plaît pas, trop proche d'une fonction que Discord fait déjà.

## Idées pour le reste du projet (hors bot)

Ces pistes répondent directement aux demandes du sujet et du professeur.

| Idée | Exigence couverte |
| --- | --- |
| Entité `Membership` (utilisateur, guilde, date d'arrivée, surnom) au lieu d'un simple `@ManyToMany` | Relations JPA |
| Contrainte `@ValidChannelName` : nom de salon en minuscules, sans espace | Annotation Bean Validation |
| Processeur `@DomainModel` : refuse de compiler une classe du domaine qui n'est pas un record ou n'a pas de fabrique `of(...)` | Processeur d'annotations ; question d'oral « classe `Personne` » |
| Endpoint `GET /api/v1/guilds/{id}/stats` (salons par type, rôles triés) | Programmation fonctionnelle, `Collectors` |
| Pagination et recherche : `GET /api/v1/users?page=0&size=20&search=yan` | Pagination et filtres du sujet |
| Renommer le paquetage `org.acme.catalog.product` en `org.acme.discord` | Architecture, lisibilité |
