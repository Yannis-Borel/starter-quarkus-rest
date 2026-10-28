package org.acme.catalog.product.api;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test d'intégration de {@code /api/v1/users} : Quarkus démarre pour de vrai,
 * avec un PostgreSQL lancé dans un conteneur Docker (Testcontainers, voir
 * {@code PostgreSqlTestResource}), et on envoie de vraies requêtes HTTP avec
 * RestAssured.
 */
@QuarkusTest
@DisplayName("API /api/v1/users")
class UserResourceIT {

    @Test
    @DisplayName("POST avec un pseudo contenant un espace renvoie 400 (@ValidUsername)")
    void createWithSpaceInUsernameReturns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "username": "mon pseudo", "displayName": "Yannis" }
                        """)
                .when().post("/api/v1/users")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST avec un pseudo valide renvoie 201")
    void createValidUserReturns201() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "username": "valid-user-it", "displayName": "Yannis" }
                        """)
                .when().post("/api/v1/users")
                .then()
                .statusCode(201);
    }
}
