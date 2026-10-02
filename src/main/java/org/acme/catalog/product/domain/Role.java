package org.acme.catalog.product.domain;

import java.util.UUID;

/**
 * Immutable domain model of a role defined inside one guild.
 *
 * <p>
 * The position orders roles in the guild hierarchy: the higher, the more
 * privileged.
 */
public record Role(UUID id, UUID guildId, String name, int position) {

    /**
     * Compact constructor enforcing the business invariants.
     *
     * @throws IllegalArgumentException if a component is missing or invalid
     */
    public Role {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (guildId == null) {
            throw new IllegalArgumentException("A role must belong to a guild");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        if (position < 0) {
            throw new IllegalArgumentException("Role position cannot be negative");
        }
    }

    /**
     * Creates a new role with a generated identifier.
     *
     * @param guildId  the owning guild
     * @param name     the role name
     * @param position the rank in the guild hierarchy
     * @return a valid role
     */
    public static Role of(UUID guildId, String name, int position) {
        return new Role(UUID.randomUUID(), guildId, name, position);
    }
}
