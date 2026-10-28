package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Forme du JSON <b>reçu</b> pour rejoindre une guilde avec un lien
 * d'invitation : qui veut rejoindre. Le pseudo identifie l'utilisateur, comme
 * dans {@code /api/v1/users/{username}}.
 */
@Schema(description = "Request object to join a guild with an invitation code")
public record JoinGuildRequest(

        @Schema(description = "Username of the user who joins", examples = { "Mirnis84" })
        @NotBlank(message = "Username is required")
        String username) {
}
