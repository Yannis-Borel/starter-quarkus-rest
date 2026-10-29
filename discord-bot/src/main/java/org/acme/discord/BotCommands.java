package org.acme.discord;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * La logique des commandes du bot, en Java pur : aucune dépendance à Discord4J,
 * donc testable avec un {@link UserApiClient} mocké.
 *
 * <ul>
 * <li>{@code !ping} → {@code pong!} (sans appel à l'API) ;</li>
 * <li>{@code !users} → les pseudos renvoyés par {@code GET /api/v1/users}.</li>
 * </ul>
 */
@ApplicationScoped
public class BotCommands {

    private final UserApiClient api;

    /**
     * @param api le client HTTP de l'API ({@code @RestClient} : Quarkus injecte
     *            le client généré)
     */
    public BotCommands(@RestClient UserApiClient api) {
        this.api = api;
    }

    /**
     * Calcule la réponse du bot à un message.
     *
     * @param content le texte du message reçu
     * @return la réponse à envoyer, ou {@code null} si le message n'est pas une
     *         commande du bot
     */
    public String answer(String content) {
        if ("!ping".equals(content)) {
            return "pong!";
        }
        if ("!users".equals(content)) {
            return listUsers();
        }
        return null;
    }

    /**
     * Appelle l'API et met les pseudos sur une ligne. Discord refuse un message
     * vide, d'où le texte pour une liste vide ; si l'API ne répond pas, le bot
     * le dit au lieu de rester muet.
     */
    private String listUsers() {
        try {
            List<String> usernames = api.getAll().stream()
                    .map(UserResponse::username)
                    .toList();
            if (usernames.isEmpty()) {
                return "No users yet";
            }
            return String.join(", ", usernames);
        } catch (RuntimeException e) {
            return "Cannot reach the API";
        }
    }
}
