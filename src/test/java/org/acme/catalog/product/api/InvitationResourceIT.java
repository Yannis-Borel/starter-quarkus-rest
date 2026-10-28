package org.acme.catalog.product.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.UUID;
import org.acme.catalog.product.infrastructure.persistence.GuildRepository;
import org.acme.catalog.product.infrastructure.persistence.InvitationEntity;
import org.acme.catalog.product.infrastructure.persistence.InvitationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test d'intégration des liens d'invitation.
 */
@QuarkusTest
@DisplayName("API invitations")
class InvitationResourceIT {

    @Inject
    GuildRepository guilds;

    @Inject
    InvitationRepository invitations;

    @Test
    @DisplayName("créer un lien (201), rejoindre (200), puis supprimer le membre (204)")
    void createJoinAndDeleteMember() {
        String guildId = GuildResourceIT.createGuild("Invitation guild");
        createUser("invited-user");

        String code = given()
                .when().post("/api/v1/guilds/" + guildId + "/invitations")
                .then()
                .statusCode(201)
                .body("code", notNullValue())
                .body("guildId", is(guildId))
                .extract().path("code");

        join(code, "invited-user").statusCode(200).body("id", is(guildId));

        given().when().get("/api/v1/guilds/" + guildId + "/members")
                .then().statusCode(200).body("username", hasItem("invited-user"));

        // Supprimer un membre doit marcher malgré la table de jointure
        given().when().delete("/api/v1/users/invited-user").then().statusCode(204);
        given().when().get("/api/v1/guilds/" + guildId + "/members")
                .then().statusCode(200).body("size()", is(0));
    }

    @Test
    @DisplayName("code inconnu : 404")
    void unknownCodeReturns404() {
        createUser("lost-user");

        join("inconnu", "lost-user").statusCode(404);
    }

    @Test
    @DisplayName("lien expiré : 410 Gone")
    void expiredInvitationReturns410() {
        String guildId = GuildResourceIT.createGuild("Expired guild");
        createUser("late-user");
        // On enregistre directement en base un lien expiré depuis une heure
        QuarkusTransaction.requiringNew().run(() -> invitations.persist(new InvitationEntity(
                UUID.randomUUID(), "expired1", guilds.findById(UUID.fromString(guildId)),
                Instant.now().minusSeconds(3600))));

        join("expired1", "late-user")
                .statusCode(410)
                .body("status", is(410))
                .body("error", is("Gone"));
    }

    private static io.restassured.response.ValidatableResponse join(String code, String username) {
        return given()
                .contentType(ContentType.JSON)
                .body("{ \"username\": \"" + username + "\" }")
                .when().post("/api/v1/invitations/" + code + "/join")
                .then();
    }

    private static void createUser(String username) {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"username\": \"" + username + "\", \"displayName\": \"X\" }")
                .when().post("/api/v1/users")
                .then()
                .statusCode(201);
    }
}
