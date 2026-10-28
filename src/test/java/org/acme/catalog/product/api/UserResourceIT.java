package org.acme.catalog.product.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

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

    @Test
    @DisplayName("GET ?search= filtre sur le pseudo ou le nom affiché, sans tenir compte des majuscules")
    void searchFiltersUsers() {
        createUser("search-alice", "Alice");
        createUser("search-bob", "Bob");

        given()
                .queryParam("search", "ALICE")
                .when().get("/api/v1/users")
                .then()
                .statusCode(200)
                .body("username", hasItem("search-alice"))
                .body("username", not(hasItem("search-bob")));
    }

    private void createUser(String username, String displayName) {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"username\": \"" + username + "\", \"displayName\": \"" + displayName + "\" }")
                .when().post("/api/v1/users")
                .then()
                .statusCode(201);
    }
}
