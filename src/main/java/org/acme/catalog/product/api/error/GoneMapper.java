package org.acme.catalog.product.api.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.catalog.product.application.InvitationExpiredException;

/**
 * Maps {@link InvitationExpiredException} to an HTTP 410 Gone: the invitation
 * existed but can no longer be used.
 */
@Provider
public class GoneMapper implements ExceptionMapper<InvitationExpiredException> {
    @Override
    public Response toResponse(InvitationExpiredException exception) {
        return ErrorResponse.build(Response.Status.GONE, exception.getMessage());
    }
}
