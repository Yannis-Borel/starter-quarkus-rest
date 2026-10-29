package org.acme.discord;

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
import reactor.core.scheduler.Schedulers;

/**
 * Implémentation de {@link DiscordBot} avec Discord4J : reçoit les messages et
 * envoie la réponse calculée par {@link BotCommands}.
 *
 * <p>
 * Discord4J est <b>réactive</b> (bibliothèque Reactor) : les méthodes renvoient
 * un {@code Mono} (0 ou 1 valeur à venir) et rien ne se passe avant
 * {@code subscribe()}. Notion de cours : patron Adapter, programmation
 * asynchrone avec Reactor.
 */
@ApplicationScoped
public class Discord4jBot implements DiscordBot {

    private static final Logger LOG = LoggerFactory.getLogger(Discord4jBot.class);

    private final BotCommands commands;

    /** La connexion en cours, gardée pour pouvoir la fermer à l'arrêt. */
    private Disposable connection;

    /**
     * @param commands la logique des commandes
     */
    public Discord4jBot(BotCommands commands) {
        this.commands = commands;
    }

    /**
     * Ouvre la connexion avec Discord, sans bloquer le démarrage de Quarkus
     * ({@code subscribe()} au lieu de {@code block()}).
     *
     * <p>
     * {@code MESSAGE_CONTENT} est un intent « privilégié » : il faut aussi
     * l'activer sur le portail développeur, sinon Discord refuse la connexion
     * et le bot reste hors ligne. {@code subscribe(null, error -> ...)} : le
     * second argument est appelé si la connexion échoue (jeton refusé, intent
     * non activé, réseau coupé).
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
     * <p>
     * {@code !users} fait un appel HTTP <b>bloquant</b> à l'API. On ne doit
     * pas bloquer les threads de Discord4J : {@code Mono.fromCallable(...)}
     * emballe l'appel, et {@code subscribeOn(Schedulers.boundedElastic())}
     * l'exécute sur un pool de threads prévu pour les tâches bloquantes. Si
     * {@code answer} renvoie {@code null}, le {@code Mono} est vide et rien
     * n'est envoyé.
     *
     * @param event l'événement « un message a été posté »
     * @return l'envoi de la réponse, ou un {@code Mono} vide
     */
    private Mono<Message> onMessage(MessageCreateEvent event) {
        Message message = event.getMessage();
        return Mono.fromCallable(() -> commands.answer(message.getContent()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(answer -> message.getChannel()
                        .flatMap(channel -> channel.createMessage(answer)));
    }

    /**
     * Ferme la connexion quand Quarkus détruit ce bean (arrêt ou rechargement
     * à chaud), pour ne pas laisser un ancien bot connecté.
     */
    @PreDestroy
    void stop() {
        if (connection != null) {
            connection.dispose();
        }
    }
}
