package org.acme.catalog.product.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.acme.catalog.product.application.dto.GuildDTO;
import org.acme.catalog.product.application.dto.InvitationDTO;
import org.acme.catalog.product.application.dto.usecases.JoinGuildRequest;
import org.acme.catalog.product.domain.Invitation;
import org.acme.catalog.product.infrastructure.mapper.GuildMapper;
import org.acme.catalog.product.infrastructure.mapper.InvitationMapper;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;
import org.acme.catalog.product.infrastructure.persistence.GuildRepository;
import org.acme.catalog.product.infrastructure.persistence.InvitationEntity;
import org.acme.catalog.product.infrastructure.persistence.InvitationRepository;
import org.acme.catalog.product.infrastructure.persistence.UserEntity;
import org.acme.catalog.product.infrastructure.persistence.UserRepository;

/**
 * Cas d'usage des liens d'invitation : créer un lien pour une guilde, et
 * rejoindre la guilde avec ce lien.
 */
@ApplicationScoped
public class InvitationService {

    private final InvitationRepository invitations;
    private final GuildRepository guilds;
    private final UserRepository users;

    /**
     * @param invitations l'accès aux invitations
     * @param guilds      l'accès aux guildes
     * @param users       l'accès aux utilisateurs
     */
    public InvitationService(InvitationRepository invitations, GuildRepository guilds, UserRepository users) {
        this.invitations = invitations;
        this.guilds = guilds;
        this.users = users;
    }

    /**
     * Crée un lien d'invitation valable 7 jours.
     *
     * @param guildId la guilde à rejoindre
     * @return le lien créé
     * @throws NoSuchElementException si la guilde n'existe pas (404)
     */
    @Transactional
    public InvitationDTO create(UUID guildId) {
        GuildEntity guild = guilds.findByIdOptional(guildId)
                .orElseThrow(() -> new NoSuchElementException("Guild not found: " + guildId));

        InvitationEntity entity = InvitationMapper.toEntity(Invitation.of(guildId), guild);
        invitations.persist(entity);
        return InvitationMapper.toDto(entity);
    }

    /**
     * Fait rejoindre la guilde du lien à un utilisateur. Rejoindre une guilde
     * dont on est déjà membre ne change rien (le {@code Set} ignore le
     * doublon).
     *
     * @param code    le code du lien
     * @param request qui rejoint
     * @return la guilde rejointe
     * @throws NoSuchElementException     si le lien ou l'utilisateur n'existe pas (404)
     * @throws InvitationExpiredException si le lien a expiré (410)
     */
    @Transactional
    public GuildDTO join(String code, JoinGuildRequest request) {
        InvitationEntity invitation = invitations.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Invitation not found: " + code));

        if (InvitationMapper.toDomain(invitation).isExpired(Instant.now())) {
            throw new InvitationExpiredException(code);
        }

        UserEntity user = users.findByUsername(request.username())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + request.username()));

        GuildEntity guild = invitation.getGuild();
        guild.getMembers().add(user);
        return GuildMapper.toDto(guild);
    }
}
