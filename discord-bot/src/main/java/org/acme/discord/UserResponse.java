package org.acme.discord;

/**
 * Un utilisateur tel que renvoyé par {@code GET /api/v1/users} de l'API.
 *
 * <p>
 * Le bot n'a besoin que du pseudo : on ne déclare que ce champ. Les autres
 * champs du JSON ({@code id}, {@code displayName}, {@code joinedAt}) sont
 * ignorés par Jackson. C'est une copie volontaire : les deux projets sont
 * indépendants, le bot ne dépend pas du code de l'API, seulement de son
 * contrat JSON.
 *
 * @param username le pseudo
 */
public record UserResponse(String username) {
}
