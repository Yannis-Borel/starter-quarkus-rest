package org.acme.catalog.product.application.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import org.acme.catalog.product.application.dto.usecases.CreateUserRequest;
import org.acme.catalog.product.domain.DomainModel;
import org.acme.catalog.product.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Lecture d'annotations <b>à l'exécution</b>, par introspection (réflexion) :
 * c'est ce que fait le validateur Bean Validation. À comparer avec le
 * processeur {@code DomainModelProcessor}, qui lit les annotations à la
 * compilation.
 */
@DisplayName("Introspection des annotations")
class IntrospectionTest {

    @Test
    @DisplayName("@ValidUsername (RUNTIME) se lit sur le champ username")
    void validUsernameIsVisibleOnTheField() throws NoSuchFieldException {
        Field field = CreateUserRequest.class.getDeclaredField("username");

        ValidUsername annotation = field.getAnnotation(ValidUsername.class);

        assertNotNull(annotation);
        assertEquals("Username must not contain spaces", annotation.message());
    }

    @Test
    @DisplayName("@Target(FIELD) : l'annotation n'est pas recopiée sur le composant du record")
    void validUsernameIsNotOnTheRecordComponent() {
        RecordComponent username = CreateUserRequest.class.getRecordComponents()[0];

        assertEquals("username", username.getName());
        assertNull(username.getAnnotation(ValidUsername.class));
    }

    @Test
    @DisplayName("@DomainModel (SOURCE) est invisible à l'exécution")
    void domainModelIsNotVisibleAtRuntime() {
        assertNull(User.class.getAnnotation(DomainModel.class));
    }
}
