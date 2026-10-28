package org.acme.catalog.product.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Montre que JPA (Hibernate) utilise les annotations Bean Validation : une
 * entité dont le {@code username} est vide est refusée au moment d'être
 * écrite en base, sans passer par l'API REST ni par le domaine.
 */
@QuarkusTest
@DisplayName("UserEntity : validation par Hibernate")
class UserEntityValidationIT {

    @Inject
    UserRepository repository;

    @Test
    @TestTransaction
    @DisplayName("un username vide est refusé à l'écriture en base (ConstraintViolationException)")
    void blankUsernameIsRejectedByHibernate() {
        UserEntity invalid = new UserEntity(UUID.randomUUID(), "  ", "Yannis", Instant.now());

        assertThrows(ConstraintViolationException.class, () -> {
            repository.persist(invalid);
            repository.flush();
        });
    }
}
