package org.acme.catalog.product.infrastructure.discord;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/**
 * Implémentation de {@link DiscordBot} avec la bibliothèque Discord4J : le bot
 * répond {@code pong!} quand quelqu'un écrit {@code !ping}.
 *
 * <p>
 * Discord4J est <b>réactive</b> (bibliothèque Reactor) : les méthodes ne
 * renvoient pas un résultat mais un {@code Mono} (0 ou 1 valeur à venir) ou un
 * {@code Flux} (plusieurs valeurs à venir). Rien ne se passe tant qu'on
 * n'appelle pas {@code subscribe()}.
 *
 * <p>
 * Notion de cours : patron Adapter (cette classe adapte Discord4J à notre
 * interface), programmation asynchrone avec Reactor.
 */
@ApplicationScoped
public class Discord4jBot implements DiscordBot {

    private static final Logger LOG = LoggerFactory.getLogger(Discord4jBot.class);

    /** La connexion en cours, gardée pour pouvoir la fermer à l'arrêt. */
    private Disposable connection;

    /**
     * Ouvre la connexion avec Discord, sans bloquer le démarrage de Quarkus
     * ({@code subscribe()} au lieu de {@code block()}).
     *
     * <p>
     * {@code MESSAGE_CONTENT} est un intent « privilégié » : sans lui, Discord
     * envoie des messages au contenu vide et le bot ne verrait jamais
     * {@code !ping}. Il faut aussi l'activer sur le portail développeur.
     *
     * <p>
     * {@code subscribe(null, erreur -> ...)} : le premier argument (que faire
     * de chaque valeur) est inutile ici ; le second est appelé si la connexion
     * échoue plus tard (jeton refusé par Discord, réseau coupé).
     *
     * @param token le jeton du bot
     */
    @Override
    public void start(String token) {
        connection = DiscordClient.create(token)
                .gateway()
                .setEnabledIntents(IntentSet.nonPrivileged().or(IntentSet.of(Intent.MESSAGE_CONTENT)))
                .withGateway(gateway -> gateway.on(MessageCreateEvent.class, this::onMessage))
                .subscribe(null, error -> LOG.error("Discord connection failed: {}", error.getMessage()));
    }

    /**
     * Appelée par Discord4J pour chaque message reçu.
     *
     * @param event l'événement « un message a été posté »
     * @return l'envoi de la réponse, ou un {@code Mono} vide s'il n'y a rien à
     *         répondre
     */
    private Mono<Message> onMessage(MessageCreateEvent event) {
        String answer = answer(event.getMessage().getContent());
        if (answer == null) {
            return Mono.empty();
        }
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(answer));
    }

    /**
     * La logique du bot, en Java pur : testable sans Discord.
     *
     * @param content le texte du message reçu
     * @return {@code "pong!"} pour {@code "!ping"}, sinon {@code null}
     */
    static String answer(String content) {
        if ("!ping".equals(content)) {
            return "pong!";
        }
        return null;
    }

    /**
     * Ferme la connexion quand Quarkus détruit ce bean (arrêt de
     * l'application ou rechargement à chaud en mode dev), pour ne pas laisser
     * un ancien bot connecté qui répondrait en double.
     */
    @PreDestroy
    void stop() {
        if (connection != null) {
            connection.dispose();
        }
    }
}
