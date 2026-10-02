package org.acme.catalog.product.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.acme.catalog.product.application.ChannelService;
import org.acme.catalog.product.application.dto.ChannelDTO;
import org.acme.catalog.product.application.dto.usecases.CreateChannelRequest;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource exposing the CRUD operations on channels.
 */
@Path("/api/v1/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Channel", description = "Text and voice channels of a guild")
@RequestScoped
public class ChannelResource {

    private final ChannelService service;

    @Inject
    public ChannelResource(ChannelService service) {
        this.service = service;
    }

    /**
     * @param guildId optional filter on the owning guild (query parameter)
     * @return the matching channels
     */
    @GET
    @Operation(summary = "Get channels, optionally filtered by guild")
    public List<ChannelDTO> getAll(
            @Parameter(description = "Only return the channels of this guild") @QueryParam("guildId") UUID guildId) {
        return service.getAll(guildId);
    }

    /**
     * @param id the channel identifier
     * @return the matching channel
     * @throws NoSuchElementException if not found (mapped to 404)
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get a channel by id")
    @APIResponse(responseCode = "200", description = "Channel found")
    @APIResponse(responseCode = "404", description = "Channel not found")
    public ChannelDTO getById(@PathParam("id") UUID id) {
        return service.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Channel not found"));
    }

    /**
     * @param request validated creation payload
     * @return a 201 Created response with the created channel
     */
    @POST
    @Operation(summary = "Create a channel in a guild")
    @APIResponse(responseCode = "201", description = "Channel created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Guild not found")
    @APIResponse(responseCode = "409", description = "Channel name already used in this guild")
    public Response create(@Valid CreateChannelRequest request) {
        ChannelDTO created = service.create(request);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    /**
     * @param id the channel identifier
     * @return a 204 No Content response
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a channel")
    @APIResponse(responseCode = "204", description = "Channel deleted")
    @APIResponse(responseCode = "404", description = "Channel not found")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
