package org.acme.discord;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Démarre le bot au lancement de l'application, <b>seulement si un jeton est
 * configuré</b>.
 *
 * <p>
 * Le jeton est lu dans la propriété {@code discord.token}, que MicroProfile
 * Config remplit automatiquement depuis la variable d'environnement
 * {@code DISCORD_TOKEN}. Sans jeton (tests, CI), l'application démarre et un
 * avertissement est écrit dans les logs. Cette classe ne dépend que de
 * l'interface {@link DiscordBot} : c'est ce qui permet de la tester avec un
 * mock.
 */
@ApplicationScoped
public class DiscordBotStartup {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordBotStartup.class);

    private final DiscordBot bot;
    private final Optional<String> token;

    /**
     * @param bot   le bot à démarrer (Discord4J en vrai, un mock dans les tests)
     * @param token le jeton, vide si {@code DISCORD_TOKEN} n'est pas défini
     */
    @Inject
    public DiscordBotStartup(DiscordBot bot, @ConfigProperty(name = "discord.token") Optional<String> token) {
        this.bot = bot;
        this.token = token;
    }

    /**
     * Appelée par Quarkus au démarrage ({@code @Observes StartupEvent}).
     *
     * @param event l'événement de démarrage (non utilisé)
     */
    void onStart(@Observes StartupEvent event) {
        startIfConfigured();
    }

    /**
     * Démarre le bot si le jeton est présent, sinon écrit un avertissement. Un
     * jeton mal formé fait lancer une exception à Discord4J : on l'attrape pour
     * que l'application ne plante pas.
     */
    public void startIfConfigured() {
        if (token.isEmpty()) {
            LOG.warn("DISCORD_TOKEN is not set: the Discord bot is disabled");
            return;
        }
        try {
            bot.start(token.get());
            LOG.info("Discord bot started");
        } catch (RuntimeException e) {
            LOG.error("Discord bot could not start: {}", e.getMessage());
        }
    }
}
