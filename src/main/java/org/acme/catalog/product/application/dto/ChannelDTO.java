package org.acme.catalog.product.application.dto;

import java.util.UUID;
import org.acme.catalog.product.domain.ChannelType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Channel representation returned by the public API.
 *
 * <p>
 * The guild is exposed by its identifier only, never as a nested entity, to
 * keep the JSON flat and avoid loading the whole guild graph.
 */
@Schema(description = "Channel representation for public API responses")
public record ChannelDTO(

        @Schema(description = "Channel identifier") UUID id,

        @Schema(description = "Identifier of the owning guild") UUID guildId,

        @Schema(description = "Channel name", examples = { "general" }) String name,

        @Schema(description = "Channel kind", examples = { "TEXT" }) ChannelType type,

        @Schema(description = "Optional topic", examples = { "Discussions libres" }) String topic) {
}
