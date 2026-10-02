package org.acme.catalog.product.infrastructure.mapper;

import org.acme.catalog.product.application.dto.RoleDTO;
import org.acme.catalog.product.application.dto.usecases.CreateRoleRequest;
import org.acme.catalog.product.domain.Role;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;
import org.acme.catalog.product.infrastructure.persistence.RoleEntity;

/**
 * Translates roles between the API, domain and persistence models.
 */
public final class RoleMapper {

    private RoleMapper() {
        // Utility class
    }

    /**
     * Maps a persisted role to its API representation.
     *
     * @param entity the database record
     * @return the public DTO
     */
    public static RoleDTO toDto(RoleEntity entity) {
        return new RoleDTO(
                entity.getId(),
                entity.getGuild().getId(),
                entity.getName(),
                entity.getPosition());
    }

    /**
     * Maps a creation payload to an entity attached to its guild, going through
     * the domain model first.
     *
     * @param request the validated payload
     * @param guild   the owning guild, already loaded
     * @return an entity ready to be persisted
     * @throws IllegalArgumentException if a domain invariant is violated
     */
    public static RoleEntity toEntity(CreateRoleRequest request, GuildEntity guild) {
        var domain = Role.of(guild.getId(), request.name(), request.position());
        return new RoleEntity(domain.id(), guild, domain.name(), domain.position());
    }
}
