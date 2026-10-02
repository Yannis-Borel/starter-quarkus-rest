package org.acme.catalog.product.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.acme.catalog.product.application.dto.RoleDTO;
import org.acme.catalog.product.application.dto.usecases.CreateRoleRequest;
import org.acme.catalog.product.infrastructure.mapper.RoleMapper;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;
import org.acme.catalog.product.infrastructure.persistence.GuildRepository;
import org.acme.catalog.product.infrastructure.persistence.RoleEntity;
import org.acme.catalog.product.infrastructure.persistence.RoleRepository;

/**
 * Use cases on roles. A role is always defined inside an existing guild.
 */
@ApplicationScoped
public class RoleService {

    private final RoleRepository roles;
    private final GuildRepository guilds;

    public RoleService(RoleRepository roles, GuildRepository guilds) {
        this.roles = roles;
        this.guilds = guilds;
    }

    /**
     * Lists roles, optionally restricted to one guild.
     *
     * @param guildId the guild to filter on, or {@code null} for all roles
     * @return the matching roles
     */
    public List<RoleDTO> getAll(UUID guildId) {
        List<RoleEntity> result = guildId == null ? roles.listAll() : roles.findByGuild(guildId);
        return result.stream()
                .map(RoleMapper::toDto)
                .toList();
    }

    /**
     * Finds a role by its identifier.
     *
     * @param id the role identifier
     * @return the role, or an empty {@code Optional}
     */
    public Optional<RoleDTO> getById(UUID id) {
        return roles.findByIdOptional(id)
                .map(RoleMapper::toDto);
    }

    /**
     * Creates a role in an existing guild.
     *
     * @param request the validated payload
     * @return the created role
     * @throws NoSuchElementException if the guild does not exist
     * @throws IllegalStateException  if the name is already used in that guild
     */
    @Transactional
    public RoleDTO create(CreateRoleRequest request) {
        GuildEntity guild = guilds.findByIdOptional(request.guildId())
                .orElseThrow(() -> new NoSuchElementException("Guild not found: " + request.guildId()));

        if (roles.existsByGuildAndName(guild.getId(), request.name())) {
            throw new IllegalStateException("Role name already used in this guild: " + request.name());
        }

        RoleEntity entity = RoleMapper.toEntity(request, guild);
        roles.persist(entity);
        return RoleMapper.toDto(entity);
    }

    /**
     * Deletes a role.
     *
     * @param id the role identifier
     * @throws NoSuchElementException if the role does not exist
     */
    @Transactional
    public void delete(UUID id) {
        RoleEntity role = roles.findByIdOptional(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found: " + id));
        roles.delete(role);
    }
}
