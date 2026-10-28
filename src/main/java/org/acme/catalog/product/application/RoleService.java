package org.acme.catalog.product.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.acme.catalog.product.application.dto.RoleDTO;
import org.acme.catalog.product.application.dto.usecases.CreateRoleRequest;
import org.acme.catalog.product.application.dto.usecases.RoleUpdateQuery;
import org.acme.catalog.product.domain.Role;
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
     * Modifie le nom et la position d'un rôle.
     *
     * <p>
     * Trajet du schéma du tableau : {@code RoleUpdateQuery} → {@code Role}
     * (domaine, via les withers qui revérifient les règles) →
     * {@code RoleEntity} → base ; puis {@code RoleEntity} → {@code RoleDTO}.
     *
     * @param id    l'identifiant du rôle
     * @param query les nouvelles valeurs, validées par Bean Validation
     * @return le rôle modifié
     * @throws NoSuchElementException   si le rôle n'existe pas (404)
     * @throws IllegalStateException    si le nom est déjà pris dans la guilde (409)
     * @throws IllegalArgumentException si une règle du domaine est violée (400)
     */
    @Transactional
    public RoleDTO update(UUID id, RoleUpdateQuery query) {
        RoleEntity entity = roles.findByIdOptional(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found: " + id));

        boolean nameChanged = !entity.getName().equals(query.name());
        if (nameChanged && roles.existsByGuildAndName(entity.getGuild().getId(), query.name())) {
            throw new IllegalStateException("Role name already used in this guild: " + query.name());
        }

        Role updated = RoleMapper.toDomain(entity)
                .withName(query.name())
                .withPosition(query.position());
        RoleMapper.updateEntity(entity, updated);
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
