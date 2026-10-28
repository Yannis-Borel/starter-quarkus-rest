package org.acme.catalog.product.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Utilisateur Discord, modèle du domaine.
 *
 * <p>
 * C'est un {@code record} : la classe est immuable (champs {@code final}, pas
 * de setter) et Java génère le constructeur, les accesseurs,
 * {@code equals}, {@code hashCode} et {@code toString}. Deux {@code User}
 * sont donc égaux s'ils ont les mêmes valeurs dans les quatre composants.
 *
 * <p>
 * Notion de cours : classe immuable + fabrique + contraintes métier (réponse
 * à la question d'oral « créez une classe Personne »). Aucune annotation de
 * framework ici : le domaine ne dépend de rien (vérifié par ArchUnit).
 */
public record User(UUID id, String username, String displayName, Instant joinedAt) {

    /**
     * Constructeur compact : il s'exécute à chaque création d'un {@code User}
     * et refuse les données invalides. C'est le dernier rempart, même si un
     * appelant oublie la validation Bean Validation de l'API.
     *
     * @throws IllegalArgumentException si un composant est absent ou vide
     *                                  (traduite en HTTP 400 par
     *                                  {@code DomainExceptionMapper})
     */
    public User {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("DisplayName cannot be null or empty");
        }
        if (joinedAt == null) {
            throw new IllegalArgumentException("JoinedAt cannot be null");
        }
    }

    /**
     * Fabrique statique : crée un nouvel utilisateur en générant lui-même
     * l'identifiant (UUID) et la date d'arrivée. L'appelant ne fournit que ce
     * qu'il connaît.
     *
     * @param username    le pseudo, unique
     * @param displayName le nom affiché
     * @return un utilisateur valide
     */
    public static User of(String username, String displayName) {
        return new User(UUID.randomUUID(), username, displayName, Instant.now());
    }
}
