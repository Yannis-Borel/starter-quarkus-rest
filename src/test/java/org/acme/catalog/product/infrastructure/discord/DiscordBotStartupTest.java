package org.acme.catalog.product.infrastructure.discord;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire du démarrage du bot, avec un <b>mock</b> de {@link DiscordBot} :
 * aucune connexion à Discord, on vérifie seulement si {@code start} est
 * appelé.
 */
@DisplayName("DiscordBotStartup")
class DiscordBotStartupTest {

    @Test
    @DisplayName("sans jeton, le bot n'est pas démarré")
    void withoutTokenTheBotIsNotStarted() {
        DiscordBot bot = mock(DiscordBot.class);

        new DiscordBotStartup(bot, Optional.empty()).startIfConfigured();

        verify(bot, never()).start(anyString());
    }

    @Test
    @DisplayName("avec un jeton, le bot est démarré avec ce jeton")
    void withTokenTheBotIsStarted() {
        DiscordBot bot = mock(DiscordBot.class);

        new DiscordBotStartup(bot, Optional.of("secret")).startIfConfigured();

        verify(bot).start("secret");
    }

    @Test
    @DisplayName("si le bot échoue au démarrage, l'application continue")
    void botFailureDoesNotStopTheApplication() {
        DiscordBot bot = mock(DiscordBot.class);
        doThrow(new IllegalArgumentException("Invalid token")).when(bot).start("mauvais");

        assertDoesNotThrow(() -> new DiscordBotStartup(bot, Optional.of("mauvais")).startIfConfigured());
    }
}
