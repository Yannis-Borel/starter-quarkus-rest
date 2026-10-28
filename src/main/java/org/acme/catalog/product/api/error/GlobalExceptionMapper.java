package org.acme.catalog.product.api.error;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "Final Safety Net" for the API.
 * * This mapper catches any Throwable that wasn't caught by more specific
 * mappers.
 * It fulfills Non-Functional Requirement (NFR-SEC-03): Prevention of technical
 * data leakage.
 */
@Provider
@Priority(Priorities.USER + 100) // Low priority: only runs if no specific mapper is found
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {

        // 1. JAX-RS exceptions below 500 (like 405 Method Not Allowed or 415 Unsupported
        // Media Type) keep their status code, but in our single JSON error format.
        if (exception instanceof WebApplicationException webEx) {
            Response originalResponse = webEx.getResponse();
            if (originalResponse.getStatus() < 500) {
                return ErrorResponse.build(originalResponse.getStatusInfo(), webEx.getMessage());
            }
        }

        // 2. Catch-all for unexpected errors (NullPointer, SQL issues, etc.)
        // We log the real error for developers but hide details from the client.
        LOG.error("Unhandled error caught by Global Mapper:", exception);

        return ErrorResponse.build(Response.Status.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.");
    }
}
