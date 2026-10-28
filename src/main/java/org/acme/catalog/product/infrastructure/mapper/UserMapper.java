package org.acme.catalog.product.infrastructure.mapper;

import org.acme.catalog.product.application.dto.UserDTO;
import org.acme.catalog.product.application.dto.usecases.CreateUserRequest;
import org.acme.catalog.product.domain.User;
import org.acme.catalog.product.infrastructure.persistence.UserEntity;

/**
 * Traduit un utilisateur d'un modèle à l'autre : requête → domaine → entité,
 * et entité → DTO.
 *
 * <p>
 * Classe utilitaire : {@code final}, constructeur privé, méthodes
 * {@code static}. On ne crée jamais d'objet {@code UserMapper}.
 */
public final class UserMapper {

    private UserMapper() {
        // Classe utilitaire : pas d'instance
    }

    /**
     * Entité → DTO, pour les réponses envoyées au client.
     *
     * @param entity la ligne lue en base
     * @return le DTO public
     */
    public static UserDTO toDto(UserEntity entity) {
        return new UserDTO(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getJoinedAt());
    }

    /**
     * Requête de création → entité, en passant par le domaine : c'est
     * {@code User.of} qui génère l'UUID et la date, et le constructeur du
     * record qui vérifie les règles métier.
     *
     * @param request la requête déjà validée par Bean Validation
     * @return une entité prête à être enregistrée
     * @throws IllegalArgumentException si une règle du domaine est violée
     */
    public static UserEntity toEntity(CreateUserRequest request) {
        var domain = User.of(
                request.username(),
                request.displayName());

        return new UserEntity(
                domain.id(),
                domain.username(),
                domain.displayName(),
                domain.joinedAt());
    }
}
