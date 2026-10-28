package org.acme.catalog.product.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité JPA : un lien d'invitation, table {@code invitations}.
 *
 * <p>
 * Côté propriétaire de la relation avec la guilde (clé étrangère
 * {@code guild_id}), comme {@link ChannelEntity}. Le code est unique dans
 * toute la base.
 */
@Entity
@Table(name = "invitations")
public class InvitationEntity {

    /** Identifiant généré par l'application ({@code Invitation.of}). */
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "guild_id", nullable = false)
    private GuildEntity guild;

    @Column(nullable = false)
    private Instant expiresAt;

    /** Constructeur sans argument exigé par la spécification JPA. */
    public InvitationEntity() {
    }

    /**
     * Constructeur pratique utilisé par le mapper.
     *
     * @param id        l'identifiant
     * @param code      le code du lien
     * @param guild     la guilde concernée
     * @param expiresAt la date d'expiration
     */
    public InvitationEntity(UUID id, String code, GuildEntity guild, Instant expiresAt) {
        this.id = id;
        this.code = code;
        this.guild = guild;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public GuildEntity getGuild() {
        return guild;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
