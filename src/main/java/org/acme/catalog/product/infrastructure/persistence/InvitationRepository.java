package org.acme.catalog.product.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

/**
 * Panache repository for {@link InvitationEntity}.
 */
@ApplicationScoped
public class InvitationRepository implements PanacheRepositoryBase<InvitationEntity, UUID> {

    /**
     * Cherche une invitation par son code.
     *
     * @param code le code du lien
     * @return l'invitation, ou un {@code Optional} vide
     */
    public Optional<InvitationEntity> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }
}
