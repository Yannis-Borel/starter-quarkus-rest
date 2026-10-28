package org.acme.catalog.product.infrastructure.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test de la logique de réponse du bot, sans connexion à Discord.
 */
@DisplayName("Discord4jBot.answer")
class Discord4jBotTest {

    @Test
    @DisplayName("!ping donne pong!")
    void pingGivesPong() {
        assertEquals("pong!", Discord4jBot.answer("!ping"));
    }

    @Test
    @DisplayName("un autre message ne donne aucune réponse")
    void otherMessageGivesNoAnswer() {
        assertNull(Discord4jBot.answer("bonjour"));
    }
}
