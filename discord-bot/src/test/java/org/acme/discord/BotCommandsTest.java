package org.acme.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.ProcessingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire de la logique des commandes, avec le Rest Client
 * <b>mocké</b> : ni Discord, ni API, ni Quarkus.
 */
@DisplayName("BotCommands")
class BotCommandsTest {

    private UserApiClient api;
    private BotCommands commands;

    @BeforeEach
    void setUp() {
        api = mock(UserApiClient.class);
        commands = new BotCommands(api);
    }

    @Test
    @DisplayName("!ping répond pong! sans appeler l'API")
    void pingAnswersPongWithoutCallingTheApi() {
        assertEquals("pong!", commands.answer("!ping"));
        verify(api, never()).getAll();
    }

    @Test
    @DisplayName("!users répond la liste des pseudos renvoyée par l'API")
    void usersListsTheUsernames() {
        when(api.getAll()).thenReturn(List.of(new UserResponse("alice"), new UserResponse("bob")));

        assertEquals("alice, bob", commands.answer("!users"));
    }

    @Test
    @DisplayName("!users sans utilisateur : message explicite (Discord refuse un message vide)")
    void usersWithEmptyList() {
        when(api.getAll()).thenReturn(List.of());

        assertEquals("No users yet", commands.answer("!users"));
    }

    @Test
    @DisplayName("!users quand l'API ne répond pas : le bot le dit")
    void usersWhenTheApiIsDown() {
        when(api.getAll()).thenThrow(new ProcessingException("Connection refused"));

        assertEquals("Cannot reach the API", commands.answer("!users"));
    }

    @Test
    @DisplayName("un autre message ne donne aucune réponse")
    void otherMessageGivesNoAnswer() {
        assertNull(commands.answer("bonjour"));
    }
}
