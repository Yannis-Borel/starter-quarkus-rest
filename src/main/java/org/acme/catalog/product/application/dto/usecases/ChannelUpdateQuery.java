package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Forme du JSON <b>reçu</b> pour modifier un salon ({@code PUT}).
 *
 * <p>
 * Seuls le nom et le sujet sont modifiables : un salon ne change jamais de
 * guilde, et un salon textuel ne devient pas vocal (on en crée un autre).
 */
@Schema(description = "Request object to update a channel")
public record ChannelUpdateQuery(

        @Schema(description = "New channel name", examples = { "general" })
        @NotBlank(message = "Channel name is required")
        String name,

        @Schema(description = "New optional topic", examples = { "Discussions libres" })
        String topic) {
}
