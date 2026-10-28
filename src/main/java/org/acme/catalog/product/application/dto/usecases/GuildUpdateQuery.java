package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Forme du JSON <b>reçu</b> pour modifier une guilde ({@code PUT}) : seul le
 * nom est modifiable (l'id et la date de création ne changent jamais). Mêmes
 * contraintes que {@code CreateGuildRequest}.
 */
@Schema(description = "Request object to update a guild")
public record GuildUpdateQuery(

        @Schema(description = "New guild name", examples = { "Master Info Toulon" })
        @NotBlank(message = "Guild name is required")
        @Size(max = 100, message = "Guild name must not exceed 100 characters")
        String name) {
}
