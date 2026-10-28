package org.acme.catalog.product.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test d'intégration de {@code /api/v1/guilds} (Quarkus + PostgreSQL dans
 * Docker).
 */
@QuarkusTest
@DisplayName("API /api/v1/guilds")
class GuildResourceIT {

    @Test
    @DisplayName("GET /{id}/stats compte les salons par type (Collectors.groupingBy)")
    void statsCountChannelsByType() {
        String guildId = createGuild("Stats guild");
        createChannel(guildId, "general", "TEXT");
        createChannel(guildId, "memes", "TEXT");
        createChannel(guildId, "vocal", "VOICE");

        given()
                .when().get("/api/v1/guilds/" + guildId + "/stats")
                .then()
                .statusCode(200)
                .body("TEXT", is(2))
                .body("VOICE", is(1));
    }

    @Test
    @DisplayName("GET /{id}/stats d'une guilde inconnue renvoie 404")
    void statsOfUnknownGuildReturns404() {
        given()
                .when().get("/api/v1/guilds/00000000-0000-0000-0000-000000000000/stats")
                .then()
                .statusCode(404);
    }

    /** Crée une guilde et renvoie son identifiant. */
    static String createGuild(String name) {
        return given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"" + name + "\" }")
                .when().post("/api/v1/guilds")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    private static void createChannel(String guildId, String name, String type) {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"guildId\": \"" + guildId + "\", \"name\": \"" + name + "\", \"type\": \"" + type + "\" }")
                .when().post("/api/v1/channels")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("PUT modifie le nom (200), nom vide refusé (400)")
    void updateName() {
        String guildId = createGuild("Ancien nom");

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Nouveau nom\" }")
                .when().put("/api/v1/guilds/" + guildId)
                .then()
                .statusCode(200)
                .body("name", is("Nouveau nom"));

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"\" }")
                .when().put("/api/v1/guilds/" + guildId)
                .then()
                .statusCode(400);
    }
}
