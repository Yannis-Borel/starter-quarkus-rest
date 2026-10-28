package org.acme.catalog.product.api.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Le format <b>unique</b> de toutes les erreurs renvoyées par l'API, par
 * exemple :
 *
 * <pre>
 * { "status": 404, "error": "Not Found", "message": "User not found", "fields": [] }
 * </pre>
 *
 * <p>
 * Tous les mappers de ce paquetage passent par {@link #build}, pour ne pas
 * répéter la construction de la réponse. {@code fields} n'est rempli que
 * pour un 400 de validation (un élément par champ invalide).
 *
 * @param status  le code HTTP (répété dans le corps pour le client)
 * @param error   le nom standard du code, par exemple {@code "Bad Request"}
 * @param message l'explication, sans détail technique
 * @param fields  les champs invalides, vide si ce n'est pas une erreur de
 *                validation
 */
public record ErrorResponse(int status, String error, String message, List<FieldError> fields) {

    /**
     * Un champ invalide et le message de sa contrainte.
     *
     * @param field   le nom du champ, par exemple {@code "username"}
     * @param message le message de la contrainte qui a échoué
     */
    public record FieldError(String field, String message) {
    }

    /**
     * Construit la réponse HTTP d'une erreur sans liste de champs.
     *
     * @param status  le code HTTP
     * @param message l'explication
     * @return la réponse JSON
     */
    public static Response build(Response.StatusType status, String message) {
        return build(status, message, List.of());
    }

    /**
     * Construit la réponse HTTP d'une erreur avec la liste des champs
     * invalides.
     *
     * @param status  le code HTTP
     * @param message l'explication
     * @param fields  les champs invalides
     * @return la réponse JSON
     */
    public static Response build(Response.StatusType status, String message, List<FieldError> fields) {
        ErrorResponse body = new ErrorResponse(status.getStatusCode(), status.getReasonPhrase(), message, fields);
        return Response.status(status.getStatusCode())
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
