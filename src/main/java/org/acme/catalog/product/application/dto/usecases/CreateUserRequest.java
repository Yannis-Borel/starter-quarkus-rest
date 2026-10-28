package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Forme du JSON <b>reçu</b> pour créer un utilisateur.
 *
 * <p>
 * Notion de cours : Bean Validation. Les annotations comme {@code @NotBlank}
 * sont lues par le validateur quand la ressource REST reçoit l'objet avec
 * {@code @Valid} : si une contrainte échoue, la méthode n'est même pas
 * appelée et le client reçoit un 400. Pas d'identifiant ni de date : c'est
 * l'application qui les génère.
 */
@Schema(description = "Request object to create a new user")
public record CreateUserRequest(

        @Schema(description = "Username", examples = { "Mirnis84" })
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Display name", examples = { "Yannis" })
        @NotBlank(message = "displayName is required")
        String displayName) {
}
