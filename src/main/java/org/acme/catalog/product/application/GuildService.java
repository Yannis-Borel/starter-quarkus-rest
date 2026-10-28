package org.acme.catalog.product.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.acme.catalog.product.application.dto.GuildDTO;
import org.acme.catalog.product.application.dto.usecases.CreateGuildRequest;
import org.acme.catalog.product.application.dto.usecases.GuildUpdateQuery;
import org.acme.catalog.product.domain.Guild;
import org.acme.catalog.product.domain.ChannelType;
import org.acme.catalog.product.infrastructure.mapper.GuildMapper;
import org.acme.catalog.product.infrastructure.persistence.ChannelEntity;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;
import org.acme.catalog.product.infrastructure.persistence.GuildRepository;

/**
 * Use cases on guilds. Independent from HTTP: it returns DTOs and
 * {@code Optional}s and throws standard Java exceptions.
 */
@ApplicationScoped
public class GuildService {

    private final GuildRepository repository;

    public GuildService(GuildRepository repository) {
        this.repository = repository;
    }

    /**
     * Lists all guilds.
     *
     * @return every guild as a DTO
     */
    public List<GuildDTO> getAll() {
        return repository.listAll().stream()
                .map(GuildMapper::toDto)
                .toList();
    }

    /**
     * Finds a guild by its identifier.
     *
     * @param id the guild identifier
     * @return the guild, or an empty {@code Optional}
     */
    public Optional<GuildDTO> getById(UUID id) {
        return repository.findByIdOptional(id)
                .map(GuildMapper::toDto);
    }

    /**
     * Compte les salons d'une guilde par type, par exemple
     * {@code {"TEXT": 2, "VOICE": 1}}.
     *
     * <p>
     * Notion de cours : {@code Collectors}. {@code groupingBy} range les salons
     * dans des groupes selon la clé donnée ({@code ChannelEntity::getType}, une
     * {@code Function}) et {@code counting()} remplace chaque groupe par sa
     * taille. Un type sans aucun salon n'apparaît pas dans le résultat.
     *
     * @param id l'identifiant de la guilde
     * @return le nombre de salons pour chaque type présent
     * @throws NoSuchElementException si la guilde n'existe pas
     */
    public Map<ChannelType, Long> getChannelStats(UUID id) {
        GuildEntity guild = repository.findByIdOptional(id)
                .orElseThrow(() -> new NoSuchElementException("Guild not found: " + id));
        return guild.getChannels().stream()
                .collect(Collectors.groupingBy(ChannelEntity::getType, Collectors.counting()));
    }

    /**
     * Creates a guild.
     *
     * @param request the validated payload
     * @return the created guild
     */
    @Transactional
    public GuildDTO create(CreateGuildRequest request) {
        GuildEntity entity = GuildMapper.toEntity(request);
        repository.persist(entity);
        return GuildMapper.toDto(entity);
    }

    /**
     * Modifie le nom d'une guilde (même schéma que {@code RoleService.update}).
     *
     * @param id    l'identifiant de la guilde
     * @param query les nouvelles valeurs, validées par Bean Validation
     * @return la guilde modifiée
     * @throws NoSuchElementException   si la guilde n'existe pas (404)
     * @throws IllegalArgumentException si une règle du domaine est violée (400)
     */
    @Transactional
    public GuildDTO update(UUID id, GuildUpdateQuery query) {
        GuildEntity entity = repository.findByIdOptional(id)
                .orElseThrow(() -> new NoSuchElementException("Guild not found: " + id));

        Guild updated = GuildMapper.toDomain(entity).withName(query.name());
        GuildMapper.updateEntity(entity, updated);
        return GuildMapper.toDto(entity);
    }

    /**
     * Deletes a guild together with its channels and roles (cascade).
     *
     * @param id the guild identifier
     * @throws NoSuchElementException if the guild does not exist
     */
    @Transactional
    public void delete(UUID id) {
        GuildEntity guild = repository.findByIdOptional(id)
                .orElseThrow(() -> new NoSuchElementException("Guild not found: " + id));
        repository.delete(guild);
    }
}
