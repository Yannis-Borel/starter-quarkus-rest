package org.acme.catalog.product.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test d'intégration du {@code PUT /api/v1/roles/{id}} : les 4 codes
 * possibles (200, 400, 404, 409).
 */
@QuarkusTest
@DisplayName("API PUT /api/v1/roles/{id}")
class RoleResourceIT {

    @Test
    @DisplayName("200 : le nom et la position sont modifiés")
    void updateReturns200() {
        String guildId = GuildResourceIT.createGuild("Role guild 200");
        String roleId = createRole(guildId, "Modo", 5);

        update(roleId, "Moderator", 10)
                .statusCode(200)
                .body("name", is("Moderator"))
                .body("position", is(10))
                .body("guildId", is(guildId));
    }

    @Test
    @DisplayName("400 : position négative refusée par Bean Validation")
    void negativePositionReturns400() {
        String guildId = GuildResourceIT.createGuild("Role guild 400");
        String roleId = createRole(guildId, "Modo", 5);

        update(roleId, "Modo", -1).statusCode(400);
    }

    @Test
    @DisplayName("404 : rôle inconnu")
    void unknownRoleReturns404() {
        update("00000000-0000-0000-0000-000000000000", "Modo", 1).statusCode(404);
    }

    @Test
    @DisplayName("409 : nom déjà pris par un autre rôle de la guilde")
    void duplicateNameReturns409() {
        String guildId = GuildResourceIT.createGuild("Role guild 409");
        createRole(guildId, "Admin", 20);
        String roleId = createRole(guildId, "Modo", 5);

        update(roleId, "Admin", 5).statusCode(409);
    }

    private static io.restassured.response.ValidatableResponse update(String roleId, String name, int position) {
        return given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"" + name + "\", \"position\": " + position + " }")
                .when().put("/api/v1/roles/" + roleId)
                .then();
    }

    private static String createRole(String guildId, String name, int position) {
        return given()
                .contentType(ContentType.JSON)
                .body("{ \"guildId\": \"" + guildId + "\", \"name\": \"" + name + "\", \"position\": " + position + " }")
                .when().post("/api/v1/roles")
                .then()
                .statusCode(201)
                .extract().path("id");
    }
}
