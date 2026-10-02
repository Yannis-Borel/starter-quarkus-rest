package org.acme.catalog.product.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Panache repository for {@link RoleEntity}.
 */
@ApplicationScoped
public class RoleRepository implements PanacheRepositoryBase<RoleEntity, UUID> {

    /**
     * Lists the roles of one guild, highest position first.
     *
     * @param guildId the guild identifier
     * @return the roles of that guild
     */
    public List<RoleEntity> findByGuild(UUID guildId) {
        return list("guild.id = ?1 order by position desc", guildId);
    }

    /**
     * Tells whether a guild already has a role with this name.
     *
     * @param guildId the guild identifier
     * @param name    the role name
     * @return {@code true} if the name is already taken in that guild
     */
    public boolean existsByGuildAndName(UUID guildId, String name) {
        return count("guild.id = ?1 and name = ?2", guildId, name) > 0;
    }
}
