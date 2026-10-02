package org.acme.catalog.product.application.dto;

import java.time.Instant;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Guild representation returned by the public API.
 */
@Schema(description = "Guild representation for public API responses")
public record GuildDTO(

        @Schema(description = "Guild identifier", examples = { "7c9e6679-7425-40de-944b-e07fc1f90ae7" }) UUID id,

        @Schema(description = "Guild name", examples = { "Master Info Toulon" }) String name,

        @Schema(description = "Creation date", examples = { "2026-10-01T08:00:00Z" }) Instant createdAt) {
}
