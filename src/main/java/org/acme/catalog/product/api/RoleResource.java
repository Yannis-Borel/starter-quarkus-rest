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
import org.acme.catalog.product.application.RoleService;
import org.acme.catalog.product.application.dto.RoleDTO;
import org.acme.catalog.product.application.dto.usecases.CreateRoleRequest;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource exposing the CRUD operations on roles.
 */
@Path("/api/v1/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Role", description = "Roles defined inside a guild")
@RequestScoped
public class RoleResource {

    private final RoleService service;

    @Inject
    public RoleResource(RoleService service) {
        this.service = service;
    }

    /**
     * @param guildId optional filter on the owning guild (query parameter)
     * @return the matching roles
     */
    @GET
    @Operation(summary = "Get roles, optionally filtered by guild")
    public List<RoleDTO> getAll(
            @Parameter(description = "Only return the roles of this guild") @QueryParam("guildId") UUID guildId) {
        return service.getAll(guildId);
    }

    /**
     * @param id the role identifier
     * @return the matching role
     * @throws NoSuchElementException if not found (mapped to 404)
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get a role by id")
    @APIResponse(responseCode = "200", description = "Role found")
    @APIResponse(responseCode = "404", description = "Role not found")
    public RoleDTO getById(@PathParam("id") UUID id) {
        return service.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found"));
    }

    /**
     * @param request validated creation payload
     * @return a 201 Created response with the created role
     */
    @POST
    @Operation(summary = "Create a role in a guild")
    @APIResponse(responseCode = "201", description = "Role created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Guild not found")
    @APIResponse(responseCode = "409", description = "Role name already used in this guild")
    public Response create(@Valid CreateRoleRequest request) {
        RoleDTO created = service.create(request);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    /**
     * @param id the role identifier
     * @return a 204 No Content response
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a role")
    @APIResponse(responseCode = "204", description = "Role deleted")
    @APIResponse(responseCode = "404", description = "Role not found")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
