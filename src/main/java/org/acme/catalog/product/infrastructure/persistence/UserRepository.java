package org.acme.catalog.product.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

/**
 * Accès à la table {@code users} (patron Repository, avec Panache).
 *
 * <p>
 * {@code PanacheRepositoryBase<UserEntity, UUID>} et non
 * {@code PanacheRepository<UserEntity>} : ce dernier suppose un identifiant de
 * type {@code Long}, alors que notre {@code @Id} est un {@code UUID}. Ainsi
 * {@code findById} et {@code deleteById} attendent bien un {@code UUID}.
 * Les opérations CRUD de base ({@code persist}, {@code listAll},
 * {@code delete}…) sont héritées.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {

    /**
     * Cherche un utilisateur par son pseudo.
     *
     * <p>
     * {@code find("username", …)} est une requête Panache simplifiée : la
     * chaîne est le nom du champ Java de {@link UserEntity}, vérifié seulement
     * à l'exécution.
     *
     * @param username le pseudo cherché
     * @return l'utilisateur, ou un {@code Optional} vide
     */
    public Optional<UserEntity> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }
}
