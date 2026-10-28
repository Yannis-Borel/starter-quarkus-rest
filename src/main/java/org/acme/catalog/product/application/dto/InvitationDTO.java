package org.acme.catalog.product.application.dto;

import java.time.Instant;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Invitation representation returned by the public API.
 */
@Schema(description = "Invitation link representation for public API responses")
public record InvitationDTO(

        @Schema(description = "Code to share", examples = { "3f2a9c1b" }) String code,

        @Schema(description = "Identifier of the guild to join") UUID guildId,

        @Schema(description = "Expiration date", examples = { "2026-11-04T08:00:00Z" }) Instant expiresAt) {
}
