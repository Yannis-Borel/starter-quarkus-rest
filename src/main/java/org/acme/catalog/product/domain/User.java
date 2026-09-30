package org.acme.catalog.product.domain;

import java.time.Instant;
import java.util.UUID;

public record User(UUID id, String username, String displayName, Instant joinedAt) {

    public User {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (displayName == null ) {
            throw new IllegalArgumentException("DisplayName cannot be null or empty");
        }
        if (joinedAt == null) {
            throw new IllegalArgumentException("JoinedAt cannot be null");
        }
    }

    public static User of ( String username, String displayName ) {
        return new User(UUID.randomUUID(), username, displayName, Instant.now());
    }
}