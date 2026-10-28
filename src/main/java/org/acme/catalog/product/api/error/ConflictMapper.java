package org.acme.catalog.product.api.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps {@link IllegalStateException} to an HTTP 409 Conflict response.
 * * Pedagogical Point: The Service throws this standard Java exception
 * when a business rule is violated (e.g., trying to create a product with a
 * duplicate SKU).
 */
@Provider
public class ConflictMapper implements ExceptionMapper<IllegalStateException> {
    @Override
    public Response toResponse(IllegalStateException ex) {
        // Send the business rule violation message to the client
        return ErrorResponse.build(Response.Status.CONFLICT, ex.getMessage());
    }
}
