package org.acme.catalog.product.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.acme.catalog.product.application.dto.ChannelDTO;
import org.acme.catalog.product.application.dto.usecases.ChannelUpdateQuery;
import org.acme.catalog.product.application.dto.usecases.CreateChannelRequest;
import org.acme.catalog.product.domain.Channel;
import org.acme.catalog.product.infrastructure.mapper.ChannelMapper;
import org.acme.catalog.product.infrastructure.persistence.ChannelEntity;
import org.acme.catalog.product.infrastructure.persistence.ChannelRepository;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;
import org.acme.catalog.product.infrastructure.persistence.GuildRepository;

/**
 * Use cases on channels. A channel is always created inside an existing guild.
 */
@ApplicationScoped
public class ChannelService {

    private final ChannelRepository channels;
    private final GuildRepository guilds;

    public ChannelService(ChannelRepository channels, GuildRepository guilds) {
        this.channels = channels;
        this.guilds = guilds;
    }

    /**
     * Lists channels, optionally restricted to one guild.
     *
     * @param guildId the guild to filter on, or {@code null} for all channels
     * @return the matching channels
     */
    public List<ChannelDTO> getAll(UUID guildId) {
        List<ChannelEntity> result = guildId == null ? channels.listAll() : channels.findByGuild(guildId);
        return result.stream()
                .map(ChannelMapper::toDto)
                .toList();
    }

    /**
     * Finds a channel by its identifier.
     *
     * @param id the channel identifier
     * @return the channel, or an empty {@code Optional}
     */
    public Optional<ChannelDTO> getById(UUID id) {
        return channels.findByIdOptional(id)
                .map(ChannelMapper::toDto);
    }

    /**
     * Creates a channel in an existing guild.
     *
     * @param request the validated payload
     * @return the created channel
     * @throws NoSuchElementException if the guild does not exist
     * @throws IllegalStateException  if the name is already used in that guild
     */
    @Transactional
    public ChannelDTO create(CreateChannelRequest request) {
        GuildEntity guild = guilds.findByIdOptional(request.guildId())
                .orElseThrow(() -> new NoSuchElementException("Guild not found: " + request.guildId()));

        if (channels.existsByGuildAndName(guild.getId(), request.name())) {
            throw new IllegalStateException("Channel name already used in this guild: " + request.name());
        }

        ChannelEntity entity = ChannelMapper.toEntity(request, guild);
        channels.persist(entity);
        return ChannelMapper.toDto(entity);
    }

    /**
     * Modifie le nom et le sujet d'un salon (même schéma que
     * {@code RoleService.update}).
     *
     * @param id    l'identifiant du salon
     * @param query les nouvelles valeurs, validées par Bean Validation
     * @return le salon modifié
     * @throws NoSuchElementException   si le salon n'existe pas (404)
     * @throws IllegalStateException    si le nom est déjà pris dans la guilde (409)
     * @throws IllegalArgumentException si une règle du domaine est violée (400)
     */
    @Transactional
    public ChannelDTO update(UUID id, ChannelUpdateQuery query) {
        ChannelEntity entity = channels.findByIdOptional(id)
                .orElseThrow(() -> new NoSuchElementException("Channel not found: " + id));

        boolean nameChanged = !entity.getName().equals(query.name());
        if (nameChanged && channels.existsByGuildAndName(entity.getGuild().getId(), query.name())) {
            throw new IllegalStateException("Channel name already used in this guild: " + query.name());
        }

        Channel updated = ChannelMapper.toDomain(entity)
                .withName(query.name())
                .withTopic(query.topic());
        ChannelMapper.updateEntity(entity, updated);
        return ChannelMapper.toDto(entity);
    }

    /**
     * Deletes a channel.
     *
     * @param id the channel identifier
     * @throws NoSuchElementException if the channel does not exist
     */
    @Transactional
    public void delete(UUID id) {
        ChannelEntity channel = channels.findByIdOptional(id)
                .orElseThrow(() -> new NoSuchElementException("Channel not found: " + id));
        channels.delete(channel);
    }
}
