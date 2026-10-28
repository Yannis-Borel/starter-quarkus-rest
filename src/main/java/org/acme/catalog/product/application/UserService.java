package org.acme.catalog.product.application;

import org.acme.catalog.product.application.dto.UserDTO;
import org.acme.catalog.product.application.dto.usecases.CreateUserRequest;
import org.acme.catalog.product.application.dto.usecases.UserUpdateQuery;
import org.acme.catalog.product.infrastructure.mapper.UserMapper;
import org.acme.catalog.product.infrastructure.persistence.GuildEntity;
import org.acme.catalog.product.infrastructure.persistence.UserEntity;
import org.acme.catalog.product.infrastructure.persistence.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import org.acme.catalog.product.domain.User;

/**
 * Cas d'usage sur les utilisateurs (couche application).
 *
 * <p>
 * Le service ne connaît pas HTTP : il renvoie des DTO ou des {@code Optional}
 * et lance des exceptions Java standard ; ce sont les mappers de
 * {@code api/error} qui les traduisent en codes HTTP.
 * {@code @ApplicationScoped} : une seule instance (bean CDI) pour toute
 * l'application, injectée dans {@code UserResource}.
 */
@ApplicationScoped
public class UserService {

    private final UserRepository repository;

    /**
     * Injection par constructeur : Quarkus fournit le repository.
     *
     * @param repository l'accès à la table {@code users}
     */
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Liste les utilisateurs, éventuellement filtrés par une recherche.
     *
     * <p>
     * Notion de cours : programmation fonctionnelle. {@code matchesSearch} est
     * un {@code Predicate<User>} : une fonction qui reçoit un {@code User} et
     * renvoie {@code true} ou {@code false}. Le stream enchaîne trois étapes :
     * {@code map} (une {@code Function} entité → domaine), {@code filter} (garde
     * ceux pour qui le prédicat est vrai), {@code map} (domaine → DTO).
     *
     * @param search texte cherché dans le pseudo ou le nom affiché, sans tenir
     *               compte des majuscules ; {@code null} pour tout renvoyer
     * @return les utilisateurs correspondants, sous forme de DTO
     */
    public List<UserDTO> getAll(String search) {
        Predicate<User> matchesSearch = user -> search == null
                || user.username().toLowerCase().contains(search.toLowerCase())
                || user.displayName().toLowerCase().contains(search.toLowerCase());

        return repository.listAll().stream()
                .map(UserMapper::toDomain)
                .filter(matchesSearch)
                .map(UserMapper::toDto)
                .toList();
    }

    /**
     * Cherche un utilisateur par pseudo. Renvoie un {@code Optional} : c'est
     * la ressource REST qui décide quoi faire s'il est vide (404).
     *
     * @param username le pseudo
     * @return l'utilisateur, ou un {@code Optional} vide
     */
    public Optional<UserDTO> getByUsername(String username) {
        return repository.findByUsername(username)
                .map(UserMapper::toDto);
    }

    /**
     * Crée un utilisateur. {@code @Transactional} ouvre une transaction : la
     * vérification du doublon et l'insertion réussissent ou échouent ensemble.
     *
     * @param request la requête validée
     * @return l'utilisateur créé
     * @throws IllegalStateException si le pseudo est déjà pris (HTTP 409)
     */
    @Transactional
    public UserDTO create(CreateUserRequest request) {
        UserEntity entity = UserMapper.toEntity(request);

        if (repository.findByUsername(entity.getUsername()).isPresent()) {
            throw new IllegalStateException("User already exists: " + entity.getUsername());
        }

        repository.persist(entity);
        return UserMapper.toDto(entity);
    }

    /**
     * Modifie le nom affiché d'un utilisateur (même schéma que
     * {@code RoleService.update}).
     *
     * @param username le pseudo de l'utilisateur à modifier
     * @param query    les nouvelles valeurs, validées par Bean Validation
     * @return l'utilisateur modifié
     * @throws NoSuchElementException   si l'utilisateur n'existe pas (404)
     * @throws IllegalArgumentException si une règle du domaine est violée (400)
     */
    @Transactional
    public UserDTO update(String username, UserUpdateQuery query) {
        UserEntity entity = repository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));

        User updated = UserMapper.toDomain(entity).withDisplayName(query.displayName());
        UserMapper.updateEntity(entity, updated);
        return UserMapper.toDto(entity);
    }

    /**
     * Supprime un utilisateur, après l'avoir retiré de ses guildes.
     *
     * @param username le pseudo
     * @throws NoSuchElementException si l'utilisateur n'existe pas (HTTP 404)
     */
    @Transactional
    public void delete(String username) {
        UserEntity p = repository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        // UserEntity est le côté inverse de la relation avec les guildes : on retire
        // d'abord l'utilisateur de chaque guilde (côté propriétaire), sinon ses lignes
        // dans guild_members bloqueraient la suppression (clé étrangère).
        for (GuildEntity guild : p.getGuilds()) {
            guild.getMembers().remove(p);
        }
        repository.delete(p);
    }

    /**
     * Supprime tous les utilisateurs (repris de {@code ProductService}, utile
     * seulement pour les tests).
     */
    @Transactional
    public void clearAll() {
        repository.deleteAll();
    }
}
