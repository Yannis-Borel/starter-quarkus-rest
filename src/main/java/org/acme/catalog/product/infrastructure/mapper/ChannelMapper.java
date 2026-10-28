package org.acme.catalog.product.infrastructure.mapper;

import org.acme.catalog.product.application.dto.ChannelDTO;
import org.acme.catalog.product.application.dto.usecases.CreateChannelRequest;
import org.acme.catalog.product.domain.Channel;
import org.acme.catalog.product.infrastructure.persistence.ChannelEntity;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;

/**
 * Translates channels between the API, domain and persistence models.
 */
public final class ChannelMapper {

    private ChannelMapper() {
        // Utility class
    }

    /**
     * Maps a persisted channel to its API representation.
     *
     * @param entity the database record
     * @return the public DTO
     */
    public static ChannelDTO toDto(ChannelEntity entity) {
        return new ChannelDTO(
                entity.getId(),
                entity.getGuild().getId(),
                entity.getName(),
                entity.getType(),
                entity.getTopic());
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
    public static ChannelEntity toEntity(CreateChannelRequest request, GuildEntity guild) {
        var domain = Channel.of(guild.getId(), request.name(), request.type(), request.topic());
        return new ChannelEntity(domain.id(), guild, domain.name(), domain.type(), domain.topic());
    }

    /**
     * Entité → domaine : recrée le record à partir de la ligne lue en base.
     *
     * @param entity la ligne lue en base
     * @return le salon du domaine
     */
    public static Channel toDomain(ChannelEntity entity) {
        return new Channel(entity.getId(), entity.getGuild().getId(), entity.getName(), entity.getType(),
                entity.getTopic());
    }

    /**
     * Recopie dans l'entité les champs modifiables. L'entité est « managée » :
     * Hibernate fera l'{@code UPDATE} au commit, sans {@code persist}.
     *
     * @param entity  l'entité à mettre à jour
     * @param channel le salon modifié et validé
     */
    public static void updateEntity(ChannelEntity entity, Channel channel) {
        entity.setName(channel.name());
        entity.setTopic(channel.topic());
    }
}
