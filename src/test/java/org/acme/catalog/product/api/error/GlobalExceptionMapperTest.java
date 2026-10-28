package org.acme.catalog.product.api.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire du filet de sécurité : une erreur imprévue donne un 500 au
 * format commun, sans le message technique de l'exception.
 */
@DisplayName("GlobalExceptionMapper")
class GlobalExceptionMapperTest {

    @Test
    @DisplayName("une exception imprévue donne 500 sans détail technique")
    void unexpectedErrorGives500() {
        Response response = new GlobalExceptionMapper().toResponse(new RuntimeException("SQL: table users is locked"));

        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertEquals(500, response.getStatus());
        assertEquals(500, body.status());
        assertEquals("Internal Server Error", body.error());
        assertEquals("An unexpected error occurred. Please contact support.", body.message());
    }
}
