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

/**
 * Ressource REST (JAX-RS) des utilisateurs : associe chaque URL et verbe HTTP
 * à une méthode Java.
 *
 * <p>
 * {@code @Path} donne l'URL de base, {@code @Produces}/{@code @Consumes}
 * disent que l'on parle JSON. La ressource ne fait que déléguer au
 * {@link UserService} et choisir le code HTTP de succès ; les erreurs sont
 * traduites par les mappers de {@code api/error}.
 */
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User", description = "Discord users")
@RequestScoped
public class UserResource {

    private final UserService service;

    /**
     * @param service le service injecté par CDI
     */
    @Inject
    public UserResource(UserService service) {
        this.service = service;
    }

    /**
     * {@code GET /api/v1/users}
     *
     * @return tous les utilisateurs
     */
    @GET
    @Operation(summary = "Get all users", description = "Returns the list of all users")
    @APIResponse(responseCode = "200", description = "List of users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class, type = SchemaType.ARRAY)))
    public List<UserDTO> getAll() {
        return service.getAll();
    }

    /**
     * {@code GET /api/v1/users/{username}}
     *
     * @param username le pseudo, lu dans l'URL ({@code @PathParam})
     * @return l'utilisateur trouvé
     * @throws NoSuchElementException s'il n'existe pas (HTTP 404)
     */
    @GET
    @Path("/{username}")
    @Operation(summary = "Get user by username", description = "Finds a specific user using its unique username")
    @APIResponse(responseCode = "200", description = "User found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @APIResponse(responseCode = "404", description = "User not found")
    public UserDTO getByUsername(
            @Parameter(description = "The username of the user", required = true, examples = @ExampleObject(value = "Mirnis84")) @PathParam("username") String username) {
        return service.getByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    /**
     * {@code POST /api/v1/users}. {@code @Valid} déclenche Bean Validation sur
     * la requête avant d'entrer dans la méthode.
     *
     * @param request le JSON reçu
     * @return 201 Created avec l'utilisateur créé
     */
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

    /**
     * {@code DELETE /api/v1/users/{username}}
     *
     * @param username le pseudo
     * @return 204 No Content
     */
    @DELETE
    @Path("/{username}")
    @Operation(summary = "Delete a user", description = "Removes a user from the database by its username")
    @APIResponse(responseCode = "204", description = "User deleted")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response delete(
            @Parameter(description = "The username of the user to delete", required = true) @PathParam("username") String username) {
        service.delete(username);
        return Response.noContent().build();
    }
}
