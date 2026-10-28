package org.acme.catalog.product.infrastructure.discord;

/**
 * Abstraction du bot Discord.
 *
 * <p>
 * Le reste de l'application ne parle qu'à cette interface, jamais à
 * Discord4J directement. Intérêt : dans un test, on la remplace par un mock
 * Mockito et on vérifie qui l'appelle, sans se connecter à Discord (exigence
 * du sujet : « bot derrière une abstraction mockable »).
 *
 * <p>
 * Notion de cours : programmer vers une interface (le D de SOLID, inversion
 * des dépendances), patron Adapter.
 */
public interface DiscordBot {

    /**
     * Connecte le bot à Discord et commence à écouter les messages.
     *
     * @param token le jeton secret du bot (jamais écrit dans le code)
     */
    void start(String token);
}
