package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Payload for creating a role inside an existing guild.
 */
@Schema(description = "Request object to create a new role")
public record CreateRoleRequest(

        @Schema(description = "Identifier of the owning guild")
        @NotNull(message = "Guild id is required")
        UUID guildId,

        @Schema(description = "Role name", examples = { "Moderator" })
        @NotBlank(message = "Role name is required")
        String name,

        @Schema(description = "Rank in the guild hierarchy", examples = { "10" })
        @PositiveOrZero(message = "Role position cannot be negative")
        int position) {
}
