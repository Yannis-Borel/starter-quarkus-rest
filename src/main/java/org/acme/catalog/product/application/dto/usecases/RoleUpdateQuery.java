package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Forme du JSON <b>reçu</b> pour modifier un rôle ({@code PUT}).
 *
 * <p>
 * Différent de {@code CreateRoleRequest} : il ne contient que les champs
 * modifiables. Pas d'identifiant (il est dans l'URL) et pas de guilde (un
 * rôle ne change jamais de guilde). C'est l'objet « UpdateQuery » du schéma
 * du tableau : il arrive de l'extérieur de la frontière.
 */
@Schema(description = "Request object to update a role")
public record RoleUpdateQuery(

        @Schema(description = "New role name", examples = { "Moderator" })
        @NotBlank(message = "Role name is required")
        String name,

        @Schema(description = "New rank in the guild hierarchy", examples = { "10" })
        @PositiveOrZero(message = "Role position cannot be negative")
        int position) {
}
