package org.acme.catalog.product.application.dto;

import java.time.Instant;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Forme du JSON <b>renvoyé</b> au client pour un utilisateur (DTO = Data
 * Transfer Object).
 *
 * <p>
 * Séparé de l'entité JPA : on peut changer la base sans changer le contrat de
 * l'API, et on ne renvoie que ce qu'on choisit. {@code @Schema} ne sert qu'à
 * la documentation OpenAPI (Swagger UI).
 */
@Schema(description = "User representation for public API responses")
public record UserDTO(

        @Schema(description = "User identifier", examples = {
                "123e4567-e89b-12d3-a456-426614174000" }) UUID id,

        @Schema(description = "Username", examples = { "Mirnis84" }) String username,

        @Schema(description = "Display name", examples = { "Yannis" }) String displayName,

        @Schema(description = "Date the user joined", examples = { "2026-09-30T19:33:00Z" }) Instant joinedAt) {
}
