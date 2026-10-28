package org.acme.catalog.product.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Lien d'invitation vers une guilde : celui qui connaît le code peut rejoindre
 * la guilde tant que le lien n'a pas expiré.
 *
 * <p>
 * Record immuable du domaine, comme les autres : fabrique {@code of} et
 * règles vérifiées dans le constructeur compact.
 */
@DomainModel
public record Invitation(UUID id, String code, UUID guildId, Instant expiresAt) {

    /** Durée de validité d'un lien (7 jours, comme les invitations Discord par défaut). */
    public static final Duration VALIDITY = Duration.ofDays(7);

    /**
     * Constructeur compact : refuse les données invalides.
     *
     * @throws IllegalArgumentException si un composant est absent ou vide
     */
    public Invitation {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
        if (guildId == null) {
            throw new IllegalArgumentException("An invitation must belong to a guild");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("ExpiresAt cannot be null");
        }
    }

    /**
     * Crée une invitation pour une guilde : identifiant et code générés,
     * expiration dans {@link #VALIDITY}.
     *
     * <p>
     * Le code est le début d'un UUID aléatoire (8 caractères) : court à
     * partager, et l'unicité est garantie en base ({@code unique = true}).
     *
     * @param guildId la guilde concernée
     * @return une invitation valide
     */
    public static Invitation of(UUID guildId) {
        String code = UUID.randomUUID().toString().substring(0, 8);
        return new Invitation(UUID.randomUUID(), code, guildId, Instant.now().plus(VALIDITY));
    }

    /**
     * Règle métier : le lien est-il expiré à l'instant donné ?
     *
     * @param now l'instant de référence (en paramètre pour pouvoir le tester)
     * @return {@code true} si {@code now} est après la date d'expiration
     */
    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
