package org.acme.catalog.product.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.acme.catalog.product.application.dto.UserDTO;
import org.acme.catalog.product.application.dto.usecases.CreateUserRequest;
import org.acme.catalog.product.application.dto.usecases.UserUpdateQuery;
import org.acme.catalog.product.infrastructure.persistence.UserEntity;
import org.acme.catalog.product.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire de {@link UserService} avec JUnit et Mockito.
 *
 * <p>
 * Le repository est remplacé par un <b>mock</b> : pas de base de données, pas
 * de Quarkus. On décide ce qu'il renvoie ({@code when(...).thenReturn(...)})
 * et on vérifie comment le service l'a appelé ({@code verify(...)}). On teste
 * ainsi la logique du service seule.
 */
@DisplayName("UserService (Mockito)")
class UserServiceTest {

    private UserRepository repository;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new UserService(repository);
    }

    private static UserEntity entity(String username, String displayName) {
        return new UserEntity(UUID.randomUUID(), username, displayName, Instant.now());
    }

    @Test
    @DisplayName("create : pseudo libre → l'utilisateur est enregistré")
    void createPersistsWhenUsernameIsFree() {
        when(repository.findByUsername("yannis")).thenReturn(Optional.empty());

        UserDTO created = service.create(new CreateUserRequest("yannis", "Yannis"));

        assertEquals("yannis", created.username());
        verify(repository).persist(any(UserEntity.class));
    }

    @Test
    @DisplayName("create : pseudo déjà pris → IllegalStateException (409) et rien n'est enregistré")
    void createRejectsDuplicateUsername() {
        when(repository.findByUsername("yannis")).thenReturn(Optional.of(entity("yannis", "Yannis")));

        assertThrows(IllegalStateException.class, () -> service.create(new CreateUserRequest("yannis", "Autre")));
        verify(repository, never()).persist(any(UserEntity.class));
    }

    @Test
    @DisplayName("getAll : la recherche garde seulement les utilisateurs correspondants")
    void getAllFiltersWithSearch() {
        when(repository.listAll()).thenReturn(List.of(entity("alice", "Alice"), entity("bob", "Bob")));

        List<UserDTO> result = service.getAll("ALI");

        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).username());
    }

    @Test
    @DisplayName("update : modifie le nom affiché de l'entité")
    void updateChangesDisplayName() {
        UserEntity existing = entity("yannis", "Avant");
        when(repository.findByUsername("yannis")).thenReturn(Optional.of(existing));

        UserDTO updated = service.update("yannis", new UserUpdateQuery("Apres"));

        assertEquals("Apres", updated.displayName());
        assertEquals("Apres", existing.getDisplayName());
    }

    @Test
    @DisplayName("update : utilisateur inconnu → NoSuchElementException (404)")
    void updateUnknownUserThrows() {
        when(repository.findByUsername("inconnu")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.update("inconnu", new UserUpdateQuery("X")));
    }
}
