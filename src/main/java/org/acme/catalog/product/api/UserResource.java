package org.acme.catalog.product.api;

import org.acme.catalog.product.application.UserService;
import org.acme.catalog.product.application.dto.UserDTO;
import org.acme.catalog.product.application.dto.usecases.CreateUserRequest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.NoSuchElementException;


@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User", description = "Validated and documented production-ready API")
@RequestScoped
public class UserResource {

    private final UserService service;

    @Inject
    public UserResource(UserService service) {
        this.service = service;
    }

    
    @GET
    @Operation(summary = "Get all Users", description = "Returns a list of all Users in the catalog")
    @APIResponse(responseCode = "200", description = "List of Users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class, type = SchemaType.ARRAY)))
    public List<UserDTO> getAll() {
        return service.getAll();
    }

 
    @GET
    @Path("/{username}")
    @Operation(summary = "Get user by username", description = "Finds a specific user using its unique username")
    @APIResponse(responseCode = "200", description = "User found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @APIResponse(responseCode = "404", description = "User not found")
    public UserDTO getByUsername(
            @Parameter(description = "The Username of the User", required = true, examples = @ExampleObject(value = "Username123")) @PathParam("username") String username) {
        return service.getByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }


    @POST
    @Operation(summary = "Create a new user", description = "Validates input and persists a new user")
    @APIResponse(responseCode = "201", description = "User created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @APIResponse(responseCode = "400", description = "Invalid input or domain validation failure")
    @APIResponse(responseCode = "409", description = "Username already exists")
    public Response create(@Valid CreateUserRequest request) {
        UserDTO created = service.create(request);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }


    @DELETE
    @Path("/{username}")
    @Operation(summary = "Delete a user", description = "Removes a user from the database by its username")
    @APIResponse(responseCode = "204", description = "User deleted")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response delete(
            @Parameter(description = "The Username of the User to delete", required = true) @PathParam("username") String username) {
        service.delete(username);
        return Response.noContent().build();
    }
}
