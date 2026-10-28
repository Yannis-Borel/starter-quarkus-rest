package org.acme.catalog.product.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contrainte Bean Validation personnalisée : le pseudo ne contient pas
 * d'espace.
 *
 * <p>
 * Notion de cours : Bean Validation, contrainte personnalisée (modèle
 * {@code CheckCase} du cours de M. Bruno). Une contrainte, ce sont deux
 * fichiers : cette annotation, et la classe qui fait la vérification
 * ({@link ValidUsernameValidator}), reliées par {@code @Constraint}.
 *
 * <ul>
 * <li>{@code @Target(FIELD)} : on ne la pose que sur un champ (le composant
 * {@code username} d'un record devient un champ).</li>
 * <li>{@code @Retention(RUNTIME)} : le validateur lit l'annotation pendant que
 * l'application tourne, par introspection ; avec {@code SOURCE} ou
 * {@code CLASS} elle serait invisible.</li>
 * <li>{@code @Documented} : l'annotation apparaît dans la Javadoc des champs
 * qui la portent.</li>
 * </ul>
 */
@Constraint(validatedBy = ValidUsernameValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidUsername {

    /**
     * Message renvoyé au client si la contrainte échoue (élément obligatoire
     * pour toute contrainte Bean Validation).
     *
     * @return le message d'erreur
     */
    String message() default "Username must not contain spaces";

    /**
     * Groupes de validation (élément obligatoire, non utilisé ici).
     *
     * @return les groupes
     */
    Class<?>[] groups() default {};

    /**
     * Métadonnées pour les clients de l'API Bean Validation (élément
     * obligatoire, non utilisé ici).
     *
     * @return les payloads
     */
    Class<? extends Payload>[] payload() default {};
}
