package org.acme.catalog.product.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Panache repository for {@link GuildEntity}.
 *
 * <p>
 * {@code PanacheRepositoryBase<GuildEntity, UUID>} is used instead of
 * {@code PanacheRepository}, which assumes a {@code Long} identifier: this way
 * {@code findById} and {@code deleteById} accept a {@code UUID}.
 */
@ApplicationScoped
public class GuildRepository implements PanacheRepositoryBase<GuildEntity, UUID> {
}
