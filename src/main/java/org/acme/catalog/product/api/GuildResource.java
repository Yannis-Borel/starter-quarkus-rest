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
import org.acme.catalog.product.application.GuildService;
import org.acme.catalog.product.application.dto.GuildDTO;
import org.acme.catalog.product.application.dto.usecases.CreateGuildRequest;
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

    @Inject
    public GuildResource(GuildService service) {
        this.service = service;
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
