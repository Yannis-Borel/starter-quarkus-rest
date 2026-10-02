package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.acme.catalog.product.domain.ChannelType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Payload for creating a channel inside an existing guild.
 */
@Schema(description = "Request object to create a new channel")
public record CreateChannelRequest(

        @Schema(description = "Identifier of the owning guild")
        @NotNull(message = "Guild id is required")
        UUID guildId,

        @Schema(description = "Channel name", examples = { "general" })
        @NotBlank(message = "Channel name is required")
        String name,

        @Schema(description = "Channel kind", examples = { "TEXT" })
        @NotNull(message = "Channel type is required")
        ChannelType type,

        @Schema(description = "Optional topic", examples = { "Discussions libres" })
        String topic) {
}
