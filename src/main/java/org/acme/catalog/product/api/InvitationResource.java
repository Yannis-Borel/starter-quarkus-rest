package org.acme.catalog.product.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.catalog.product.application.InvitationService;
import org.acme.catalog.product.application.dto.GuildDTO;
import org.acme.catalog.product.application.dto.usecases.JoinGuildRequest;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for using invitation links. Links are created by
 * {@code POST /api/v1/guilds/{id}/invitations} (see {@link GuildResource}).
 */
@Path("/api/v1/invitations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Invitation", description = "Invitation links to join a guild")
@RequestScoped
public class InvitationResource {

    private final InvitationService service;

    @Inject
    public InvitationResource(InvitationService service) {
        this.service = service;
    }

    /**
     * {@code POST /api/v1/invitations/{code}/join} : rejoindre la guilde du lien.
     *
     * @param code    the invitation code
     * @param request who joins
     * @return the joined guild (200 OK)
     */
    @POST
    @Path("/{code}/join")
    @Operation(summary = "Join the guild of an invitation link")
    @APIResponse(responseCode = "200", description = "User is now a member of the guild")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Invitation or user not found")
    @APIResponse(responseCode = "410", description = "Invitation expired")
    public GuildDTO join(@PathParam("code") String code, @Valid JoinGuildRequest request) {
        return service.join(code, request);
    }
}
