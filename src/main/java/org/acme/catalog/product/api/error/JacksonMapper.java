package org.acme.catalog.product.api.error;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Handles a JSON body that is well-formed but does not match the expected
 * object (for example an array instead of an object, or {@code "type": "PHOTO"}
 * for a {@code ChannelType}). Prevents the client from seeing raw Jackson
 * details.
 *
 * <p>
 * Le type traité est {@code MismatchedInputException} et non sa classe mère
 * {@code JsonProcessingException} : Quarkus fournit déjà un mapper pour
 * {@code MismatchedInputException} (400 sans corps), et JAX-RS choisit le
 * mapper du type le plus proche de l'exception. En déclarant le même type, le
 * nôtre le remplace. Un JSON <b>mal formé</b> (accolade manquante) n'arrive
 * pas ici : Quarkus le transforme en {@code BadRequestException}, traitée par
 * {@link GlobalExceptionMapper}.
 */
@Provider
public class JacksonMapper implements ExceptionMapper<MismatchedInputException> {
    @Override
    public Response toResponse(MismatchedInputException ex) {
        return ErrorResponse.build(Response.Status.BAD_REQUEST, "Invalid JSON structure");
    }
}
