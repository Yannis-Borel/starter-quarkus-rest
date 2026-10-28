package org.acme.catalog.product.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.acme.catalog.product.application.GuildService;
import org.acme.catalog.product.application.InvitationService;
import org.acme.catalog.product.application.dto.GuildDTO;
import org.acme.catalog.product.application.dto.InvitationDTO;
import org.acme.catalog.product.application.dto.UserDTO;
import org.acme.catalog.product.application.dto.usecases.CreateGuildRequest;
import org.acme.catalog.product.application.dto.usecases.GuildUpdateQuery;
import org.acme.catalog.product.domain.ChannelType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource exposing the CRUD operations on guilds.
 */
@Path("/api/v1/guilds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Guild", description = "Discord servers")
@RequestScoped
public class GuildResource {

    private final GuildService service;
    private final InvitationService invitationService;

    @Inject
    public GuildResource(GuildService service, InvitationService invitationService) {
        this.service = service;
        this.invitationService = invitationService;
    }

    /**
     * @return all guilds
     */
    @GET
    @Operation(summary = "Get all guilds")
    public List<GuildDTO> getAll() {
        return service.getAll();
    }

    /**
     * @param id the guild identifier
     * @return the matching guild
     * @throws NoSuchElementException if not found (mapped to 404)
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get a guild by id")
    @APIResponse(responseCode = "200", description = "Guild found")
    @APIResponse(responseCode = "404", description = "Guild not found")
    public GuildDTO getById(@PathParam("id") UUID id) {
        return service.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Guild not found"));
    }

    /**
     * {@code GET /api/v1/guilds/{id}/stats} : nombre de salons par type.
     *
     * @param id the guild identifier
     * @return for example {@code {"TEXT": 2, "VOICE": 1}}
     */
    @GET
    @Path("/{id}/stats")
    @Operation(summary = "Count the channels of a guild by type")
    @APIResponse(responseCode = "200", description = "Number of channels per type")
    @APIResponse(responseCode = "404", description = "Guild not found")
    public Map<ChannelType, Long> getChannelStats(@PathParam("id") UUID id) {
        return service.getChannelStats(id);
    }

    /**
     * {@code GET /api/v1/guilds/{id}/members} : les membres de la guilde.
     *
     * @param id the guild identifier
     * @return the members
     */
    @GET
    @Path("/{id}/members")
    @Operation(summary = "Get the members of a guild")
    @APIResponse(responseCode = "200", description = "Members of the guild")
    @APIResponse(responseCode = "404", description = "Guild not found")
    public List<UserDTO> getMembers(@PathParam("id") UUID id) {
        return service.getMembers(id);
    }

    /**
     * {@code POST /api/v1/guilds/{id}/invitations} : crée un lien d'invitation
     * valable 7 jours. Pas de corps de requête : {@code @Consumes(WILDCARD)}
     * remplace le {@code @Consumes(JSON)} de la classe, sinon un {@code POST}
     * sans {@code Content-Type} serait refusé (415).
     *
     * @param id the guild identifier
     * @return 201 Created with the invitation (its code)
     */
    @POST
    @Path("/{id}/invitations")
    @Consumes(MediaType.WILDCARD)
    @Operation(summary = "Create an invitation link to the guild (valid 7 days)")
    @APIResponse(responseCode = "201", description = "Invitation created")
    @APIResponse(responseCode = "404", description = "Guild not found")
    public Response createInvitation(@PathParam("id") UUID id) {
        InvitationDTO created = invitationService.create(id);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    /**
     * @param request validated creation payload
     * @return a 201 Created response with the created guild
     */
    @POST
    @Operation(summary = "Create a guild")
    @APIResponse(responseCode = "201", description = "Guild created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    public Response create(@Valid CreateGuildRequest request) {
        GuildDTO created = service.create(request);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    /**
     * {@code PUT /api/v1/guilds/{id}} : modifie le nom.
     *
     * @param id    the guild identifier
     * @param query validated update payload
     * @return the updated guild (200 OK)
     */
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update the name of a guild")
    @APIResponse(responseCode = "200", description = "Guild updated")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Guild not found")
    public GuildDTO update(@PathParam("id") UUID id, @Valid GuildUpdateQuery query) {
        return service.update(id, query);
    }

    /**
     * @param id the guild identifier
     * @return a 204 No Content response
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a guild with its channels and roles")
    @APIResponse(responseCode = "204", description = "Guild deleted")
    @APIResponse(responseCode = "404", description = "Guild not found")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
