package org.acme.catalog.product.application;

/**
 * Lancée quand on utilise un lien d'invitation expiré. Traduite en HTTP
 * 410 Gone par {@code GoneMapper}.
 *
 * <p>
 * Une exception dédiée, car aucune exception standard de Java ne veut dire
 * « existait, mais n'est plus utilisable » ; le service reste ainsi
 * indépendant de HTTP.
 */
public class InvitationExpiredException extends RuntimeException {

    /**
     * @param code le code du lien expiré
     */
    public InvitationExpiredException(String code) {
        super("Invitation expired: " + code);
    }
}
