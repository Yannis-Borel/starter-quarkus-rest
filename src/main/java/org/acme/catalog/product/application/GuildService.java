package org.acme.catalog.product.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.acme.catalog.product.application.dto.GuildDTO;
import org.acme.catalog.product.application.dto.usecases.CreateGuildRequest;
import org.acme.catalog.product.infrastructure.mapper.GuildMapper;
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
