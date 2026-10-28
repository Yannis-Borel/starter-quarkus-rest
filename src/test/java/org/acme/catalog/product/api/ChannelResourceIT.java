package org.acme.catalog.product.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test d'intégration du {@code PUT /api/v1/channels/{id}}.
 */
@QuarkusTest
@DisplayName("API PUT /api/v1/channels/{id}")
class ChannelResourceIT {

    @Test
    @DisplayName("200 : nom et sujet modifiés, le type ne change pas")
    void updateReturns200() {
        String guildId = GuildResourceIT.createGuild("Channel guild 200");
        String channelId = createChannel(guildId, "general");

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"accueil\", \"topic\": \"Bienvenue\" }")
                .when().put("/api/v1/channels/" + channelId)
                .then()
                .statusCode(200)
                .body("name", is("accueil"))
                .body("topic", is("Bienvenue"))
                .body("type", is("TEXT"));
    }

    @Test
    @DisplayName("409 : nom déjà pris par un autre salon de la guilde")
    void duplicateNameReturns409() {
        String guildId = GuildResourceIT.createGuild("Channel guild 409");
        createChannel(guildId, "general");
        String channelId = createChannel(guildId, "memes");

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"general\" }")
                .when().put("/api/v1/channels/" + channelId)
                .then()
                .statusCode(409);
    }

    private static String createChannel(String guildId, String name) {
        return given()
                .contentType(ContentType.JSON)
                .body("{ \"guildId\": \"" + guildId + "\", \"name\": \"" + name + "\", \"type\": \"TEXT\" }")
                .when().post("/api/v1/channels")
                .then()
                .statusCode(201)
                .extract().path("id");
    }
}
