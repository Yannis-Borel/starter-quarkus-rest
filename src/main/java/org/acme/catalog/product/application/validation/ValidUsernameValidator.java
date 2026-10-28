package org.acme.catalog.product.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Vérification associée à {@link ValidUsername}.
 *
 * <p>
 * Notion de cours : Bean Validation, {@code ConstraintValidator<A, T>}. Le
 * premier paramètre générique est l'annotation traitée, le second le type de
 * la valeur vérifiée (ici un {@code String}). Le validateur Bean Validation
 * (Hibernate Validator) crée cette classe lui-même et appelle
 * {@link #isValid} pour chaque champ annoté.
 */
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    /**
     * Renvoie {@code true} si le pseudo est acceptable.
     *
     * <p>
     * {@code null} est considéré valide, comme dans le cours : vérifier la
     * présence est le travail de {@code @NotNull} / {@code @NotBlank}. Chaque
     * contrainte vérifie une seule chose.
     *
     * @param value   le pseudo à vérifier, peut être {@code null}
     * @param context contexte fourni par le validateur (non utilisé)
     * @return {@code true} si la valeur est {@code null} ou sans espace
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.contains(" ");
    }
}
