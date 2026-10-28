package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Forme du JSON <b>reçu</b> pour modifier un utilisateur ({@code PUT}).
 *
 * <p>
 * Seul le nom affiché est modifiable : le pseudo sert d'identifiant dans
 * l'URL ({@code /users/{username}}), l'id et la date d'arrivée ne changent
 * jamais.
 */
@Schema(description = "Request object to update a user")
public record UserUpdateQuery(

        @Schema(description = "New display name", examples = { "Yannis B." })
        @NotBlank(message = "displayName is required")
        String displayName) {
}
