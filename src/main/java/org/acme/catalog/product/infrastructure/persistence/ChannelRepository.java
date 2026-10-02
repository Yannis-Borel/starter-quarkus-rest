package org.acme.catalog.product.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Panache repository for {@link ChannelEntity}.
 */
@ApplicationScoped
public class ChannelRepository implements PanacheRepositoryBase<ChannelEntity, UUID> {

    /**
     * Lists the channels of one guild.
     *
     * @param guildId the guild identifier
     * @return the channels of that guild
     */
    public List<ChannelEntity> findByGuild(UUID guildId) {
        return list("guild.id", guildId);
    }

    /**
     * Tells whether a guild already has a channel with this name.
     *
     * @param guildId the guild identifier
     * @param name    the channel name
     * @return {@code true} if the name is already taken in that guild
     */
    public boolean existsByGuildAndName(UUID guildId, String name) {
        return count("guild.id = ?1 and name = ?2", guildId, name) > 0;
    }
}
