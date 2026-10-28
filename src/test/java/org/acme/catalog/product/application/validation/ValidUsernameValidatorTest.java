package org.acme.catalog.product.application.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire de {@link ValidUsernameValidator} : on appelle
 * {@code isValid} directement, sans démarrer Quarkus.
 */
@DisplayName("ValidUsernameValidator")
class ValidUsernameValidatorTest {

    private final ValidUsernameValidator validator = new ValidUsernameValidator();

    @Test
    @DisplayName("un pseudo sans espace est valide")
    void usernameWithoutSpaceIsValid() {
        assertTrue(validator.isValid("mirnis84", null));
    }

    @Test
    @DisplayName("un pseudo avec un espace est refusé")
    void usernameWithSpaceIsInvalid() {
        assertFalse(validator.isValid("mon pseudo", null));
    }

    @Test
    @DisplayName("null est valide : c'est le travail de @NotBlank")
    void nullIsValid() {
        assertTrue(validator.isValid(null, null));
    }
}
