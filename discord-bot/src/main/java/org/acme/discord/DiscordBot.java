package org.acme.discord;

/**
 * Abstraction du bot Discord.
 *
 * <p>
 * {@link DiscordBotStartup} ne connaît que cette interface, jamais Discord4J :
 * dans un test, on la remplace par un mock Mockito (exigence du sujet : « bot
 * derrière une abstraction mockable »). Notion de cours : programmer vers une
 * interface (inversion des dépendances), patron Adapter.
 */
public interface DiscordBot {

    /**
     * Connecte le bot à Discord et commence à écouter les messages.
     *
     * @param token le jeton secret du bot (jamais écrit dans le code)
     */
    void start(String token);
}
