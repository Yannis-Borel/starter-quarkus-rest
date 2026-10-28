package org.acme.catalog.product.infrastructure.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité JPA : correspondance entre un utilisateur et une ligne de la table
 * {@code users}.
 *
 * <p>
 * Elle ne contient aucune règle métier (elles sont dans le record
 * {@code User}). Elle a un constructeur sans argument et des setters parce que
 * Hibernate en a besoin pour recréer l'objet depuis la base.
 */
@Entity
@Table(name = "users")
public class UserEntity {

    /** Identifiant généré par l'application ({@code User.of}), pas de {@code @GeneratedValue}. */
    @Id
    private UUID id;

    /**
     * Pseudo : unique et obligatoire au niveau de la base (contrainte SQL).
     *
     * <p>
     * {@code @NotBlank} est une contrainte Bean Validation posée sur une
     * entité JPA : Hibernate la vérifie lui-même avant d'écrire en base et
     * refuse l'entité avec une {@code ConstraintViolationException} (notion
     * de cours : intégration JPA / Bean Validation).
     */
    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private Instant joinedAt;

    /** Constructeur sans argument exigé par la spécification JPA. */
    public UserEntity() {
    }

    /**
     * Constructeur pratique utilisé par {@code UserMapper}.
     *
     * @param id          l'identifiant
     * @param username    le pseudo
     * @param displayName le nom affiché
     * @param joinedAt    la date d'arrivée
     */
    public UserEntity(UUID id, String username, String displayName, Instant joinedAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.joinedAt = joinedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
