package org.acme.discord;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Client HTTP de l'API, sur le modèle de {@code ProductRestClient} du starter
 * (MicroProfile Rest Client).
 *
 * <p>
 * On écrit seulement une interface avec les annotations JAX-RS
 * ({@code @Path}, {@code @GET}) : Quarkus génère l'implémentation qui envoie
 * la requête HTTP et transforme le JSON reçu en objets Java.
 * {@code configKey = "api"} relie ce client à la propriété
 * {@code quarkus.rest-client.api.url} (variable {@code API_URL}).
 */
@RegisterRestClient(configKey = "api")
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
public interface UserApiClient {

    /**
     * {@code GET /api/v1/users} sur l'API.
     *
     * @return tous les utilisateurs
     */
    @GET
    List<UserResponse> getAll();
}
