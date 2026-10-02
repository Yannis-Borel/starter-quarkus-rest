package org.acme.catalog.product.application.dto;

import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Role representation returned by the public API.
 */
@Schema(description = "Role representation for public API responses")
public record RoleDTO(

        @Schema(description = "Role identifier") UUID id,

        @Schema(description = "Identifier of the owning guild") UUID guildId,

        @Schema(description = "Role name", examples = { "Moderator" }) String name,

        @Schema(description = "Rank in the guild hierarchy", examples = { "10" }) int position) {
}
