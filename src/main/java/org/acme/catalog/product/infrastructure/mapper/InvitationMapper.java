package org.acme.catalog.product.infrastructure.mapper;

import org.acme.catalog.product.application.dto.InvitationDTO;
import org.acme.catalog.product.domain.Invitation;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;
import org.acme.catalog.product.infrastructure.persistence.InvitationEntity;

/**
 * Translates invitations between the API, domain and persistence models.
 */
public final class InvitationMapper {

    private InvitationMapper() {
        // Utility class
    }

    /**
     * Entité → DTO.
     *
     * @param entity la ligne lue en base
     * @return le DTO public
     */
    public static InvitationDTO toDto(InvitationEntity entity) {
        return new InvitationDTO(entity.getCode(), entity.getGuild().getId(), entity.getExpiresAt());
    }

    /**
     * Domaine → entité rattachée à sa guilde.
     *
     * @param invitation l'invitation du domaine
     * @param guild      la guilde, déjà chargée
     * @return une entité prête à être enregistrée
     */
    public static InvitationEntity toEntity(Invitation invitation, GuildEntity guild) {
        return new InvitationEntity(invitation.id(), invitation.code(), guild, invitation.expiresAt());
    }

    /**
     * Entité → domaine.
     *
     * @param entity la ligne lue en base
     * @return l'invitation du domaine
     */
    public static Invitation toDomain(InvitationEntity entity) {
        return new Invitation(entity.getId(), entity.getCode(), entity.getGuild().getId(), entity.getExpiresAt());
    }
}
