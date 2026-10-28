package org.acme.catalog.product.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire de la règle métier d'expiration, en Java pur.
 */
@DisplayName("Invitation")
class InvitationTest {

    @Test
    @DisplayName("of : code de 8 caractères, valable 7 jours")
    void ofCreatesAValidInvitation() {
        Invitation invitation = Invitation.of(UUID.randomUUID());

        assertEquals(8, invitation.code().length());
        assertFalse(invitation.isExpired(Instant.now()));
        assertTrue(invitation.isExpired(Instant.now().plus(Invitation.VALIDITY).plusSeconds(1)));
    }
}
