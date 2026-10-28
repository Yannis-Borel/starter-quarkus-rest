package org.acme.catalog.product.api.error;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Vérifie que toutes les erreurs sortent dans le même format JSON
 * ({@link ErrorResponse}) : {@code status}, {@code error}, {@code message},
 * {@code fields}.
 */
@QuarkusTest
@DisplayName("Format unique des erreurs")
class ErrorFormatIT {

    @Test
    @DisplayName("400 de validation : liste des champs invalides (ValidationMapper)")
    void validationError() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"username\": \"mon pseudo\", \"displayName\": \"\" }")
                .when().post("/api/v1/users")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("status", is(400))
                .body("error", is("Bad Request"))
                .body("message", is("Validation failed"))
                .body("fields.field", hasItem("username"))
                .body("fields.field", hasItem("displayName"))
                .body("fields.message", hasItem("Username must not contain spaces"));
    }

    @Test
    @DisplayName("400 : JSON mal formé")
    void malformedJson() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"username\": ")
                .when().post("/api/v1/users")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("status", is(400))
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("400 : JSON bien formé mais du mauvais type (JacksonMapper)")
    void wrongJsonType() {
        given()
                .contentType(ContentType.JSON)
                .body("[1, 2]")
                .when().post("/api/v1/users")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("status", is(400))
                .body("message", is("Invalid JSON structure"));
    }

    @Test
    @DisplayName("404 : ressource absente (NotFoundMapper)")
    void notFound() {
        given()
                .when().get("/api/v1/users/personne")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("status", is(404))
                .body("error", is("Not Found"))
                .body("message", is("User not found"));
    }

    @Test
    @DisplayName("409 : doublon (ConflictMapper)")
    void conflict() {
        String body = "{ \"username\": \"doublon-err\", \"displayName\": \"X\" }";
        given().contentType(ContentType.JSON).body(body).when().post("/api/v1/users").then().statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/v1/users")
                .then()
                .statusCode(409)
                .contentType(ContentType.JSON)
                .body("status", is(409))
                .body("error", is("Conflict"));
    }

    @Test
    @DisplayName("405 : verbe non autorisé (exception JAX-RS, GlobalExceptionMapper)")
    void methodNotAllowed() {
        given()
                .when().delete("/api/v1/users")
                .then()
                .statusCode(405)
                .contentType(ContentType.JSON)
                .body("status", is(405));
    }
}
