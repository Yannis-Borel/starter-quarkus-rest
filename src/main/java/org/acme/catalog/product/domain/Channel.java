package org.acme.catalog.product.domain;

import java.util.UUID;

/**
 * Immutable domain model of a channel belonging to exactly one guild.
 *
 * <p>
 * The guild is referenced by its identifier only: the domain does not know
 * about JPA relations.
 */
@DomainModel
public record Channel(UUID id, UUID guildId, String name, ChannelType type, String topic) {

    /**
     * Compact constructor enforcing the business invariants. The topic is
     * optional.
     *
     * @throws IllegalArgumentException if a mandatory component is missing
     */
    public Channel {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (guildId == null) {
            throw new IllegalArgumentException("A channel must belong to a guild");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Channel name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Channel type cannot be null");
        }
    }

    /**
     * Creates a new channel with a generated identifier.
     *
     * @param guildId the owning guild
     * @param name    the channel name
     * @param type    the channel kind
     * @param topic   an optional topic, may be {@code null}
     * @return a valid channel
     */
    public static Channel of(UUID guildId, String name, ChannelType type, String topic) {
        return new Channel(UUID.randomUUID(), guildId, name, type, topic);
    }

    /**
     * Wither : renvoie un <b>nouveau</b> salon avec un autre nom (le record
     * est immuable). Le constructeur compact revérifie les règles.
     *
     * @param newName le nouveau nom
     * @return une copie modifiée
     */
    public Channel withName(String newName) {
        return new Channel(id, guildId, newName, type, topic);
    }

    /**
     * Wither : renvoie un nouveau salon avec un autre sujet.
     *
     * @param newTopic le nouveau sujet, peut être {@code null}
     * @return une copie modifiée
     */
    public Channel withTopic(String newTopic) {
        return new Channel(id, guildId, name, type, newTopic);
    }
}
