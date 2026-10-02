package org.acme.catalog.product.application.dto.usecases;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Payload for creating a guild. The identifier and creation date are generated
 * by the application.
 */
@Schema(description = "Request object to create a new guild")
public record CreateGuildRequest(

        @Schema(description = "Guild name", examples = { "Master Info Toulon" })
        @NotBlank(message = "Guild name is required")
        @Size(max = 100, message = "Guild name must not exceed 100 characters")
        String name) {
}
