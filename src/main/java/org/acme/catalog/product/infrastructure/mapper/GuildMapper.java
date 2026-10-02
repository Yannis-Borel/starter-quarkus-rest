package org.acme.catalog.product.infrastructure.mapper;

import org.acme.catalog.product.application.dto.GuildDTO;
import org.acme.catalog.product.application.dto.usecases.CreateGuildRequest;
import org.acme.catalog.product.domain.Guild;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;

/**
 * Translates guilds between the API, domain and persistence models.
 */
public final class GuildMapper {

    private GuildMapper() {
        // Utility class
    }

    /**
     * Maps a persisted guild to its API representation.
     *
     * @param entity the database record
     * @return the public DTO
     */
    public static GuildDTO toDto(GuildEntity entity) {
        return new GuildDTO(entity.getId(), entity.getName(), entity.getCreatedAt());
    }

    /**
     * Maps a creation payload to an entity, going through the domain model so
     * that business rules are checked and the identifier is generated.
     *
     * @param request the validated payload
     * @return an entity ready to be persisted
     * @throws IllegalArgumentException if a domain invariant is violated
     */
    public static GuildEntity toEntity(CreateGuildRequest request) {
        var domain = Guild.of(request.name());
        return new GuildEntity(domain.id(), domain.name(), domain.createdAt());
    }
}
