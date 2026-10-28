package org.acme.catalog.product.api.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Traduit une {@link ConstraintViolationException} (une contrainte Bean
 * Validation a échoué, par exemple {@code @NotBlank} ou
 * {@code @ValidUsername} sur un objet reçu avec {@code @Valid}) en HTTP 400,
 * avec la liste des champs invalides.
 *
 * <p>
 * Sans ce mapper, Quarkus renverrait 400 avec son propre format JSON ; avec
 * lui, toutes les erreurs ont le même format ({@link ErrorResponse}).
 */
@Provider
public class ValidationMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<ErrorResponse.FieldError> fields = new ArrayList<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            // Le chemin ressemble à "create.request.username" : on garde le dernier morceau
            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);
            fields.add(new ErrorResponse.FieldError(field, violation.getMessage()));
        }
        return ErrorResponse.build(Response.Status.BAD_REQUEST, "Validation failed", fields);
    }
}
