package org.acme.catalog.product.application.dto;

import java.time.Instant;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "User representation for public API responses")
public record UserDTO(

                @Schema(description = "User identifier", examples = {
                                "123e4567-e89b-12d3-a456-426614174000" }) UUID id,

                @Schema(description = "Username", examples = { "Mirnis84" }) String username,

                @Schema(description = "Displayname", examples = { "Yannis" }) String displayName,

                @Schema(description = "Date", examples = { "2026-09-30T19:33:00Z" }) Instant joinedAt) {
}
