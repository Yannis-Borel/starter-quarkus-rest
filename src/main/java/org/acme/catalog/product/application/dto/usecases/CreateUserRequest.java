package org.acme.catalog.product.application.dto.usecases;


import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


@Schema(description = "Request object to create a new user")
public record CreateUserRequest(

        @Schema(description = "Username", examples = { "Mirnis84" })
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "displayName", examples = { "Yannis" })
        @NotBlank(message = "displayName is required")
        String displayName

) {}
