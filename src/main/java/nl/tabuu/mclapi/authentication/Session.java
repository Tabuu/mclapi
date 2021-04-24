package nl.tabuu.mclapi.authentication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.tabuu.mclapi.profile.IMinecraftProfile;
import nl.tabuu.mclapi.util.HttpRequest;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Session {
    public static final String
            MOJANG_AUTH_SERVER_URL = "https://authserver.mojang.com/%s",
            MOJANG_AUTH_SERVER_VALIDATE_ENPOINT = "validate",
            MOJANG_AUTH_SERVER_INVALIDATE_ENPOINT = "invalidate",
            MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private final String _sessionId;
    private IMinecraftProfile _profile;

    /**
     * Creates a session based on the provided session id.
     *
     * @param sessionId The session id to base this session on.
     */
    public Session(String sessionId) {
        _sessionId = sessionId;
    }


    /**
     * Returns the session id.
     *
     * @return The session id.
     */
    public String getId() {
        return _sessionId;
    }

    /**
     * Returns true if this session is valid, otherwise false.
     *
     * @return True if this session is valid, otherwise false.
     */
    public CompletableFuture<Boolean> isValid() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("accessToken", getId());
        // requestBody.addProperty("clientToken", MCLauncher.getLauncherId().toString()); TODO: fix 403

        return HttpRequest.doPostRequest(
                String.format(MOJANG_AUTH_SERVER_URL, MOJANG_AUTH_SERVER_VALIDATE_ENPOINT),
                Map.of("Content-Type", "application/json"),
                requestBody
        ).exceptionally(t -> { t.printStackTrace(); return 0;})
                .thenApply((code) -> code == 204);
    }

    /**
     * Invalidates this session.
     *
     * @return True if this session was successfully invalidated, otherwise false.
     */
    public CompletableFuture<Boolean> invalidate() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("accessToken", getId());
        // requestBody.addProperty("clientToken", MCLauncher.getLauncherId().toString()); TODO: fix 403

        return HttpRequest.doPostRequest(
                String.format(MOJANG_AUTH_SERVER_URL, MOJANG_AUTH_SERVER_INVALIDATE_ENPOINT),
                Map.of("Content-Type", "application/json"),
                requestBody
        ).exceptionally(t -> 0)
                .thenApply(code -> code == 204);
    }

    /**
     * Makes a request to the Mojang servers to fetch the profile, and caches this.
     *
     * @return The cached profile for this session.
     */
    public CompletableFuture<IMinecraftProfile> getProfile() {
        if(Objects.isNull(_profile)) {
            CompletableFuture<IMinecraftProfile> future = HttpRequest.doJsonBodyRequest(
                    MINECRAFT_PROFILE_URL,
                    "GET",
                    Map.of(
                            "Authorization", String.format("Bearer %s", getId())),
                    null)
                    .thenApply(this::getProfileFromJson);

            future.thenApply(profile -> _profile = profile);

            return future;
        }

        return CompletableFuture.completedFuture(_profile);
    }

    private IMinecraftProfile getProfileFromJson(JsonObject object) {
        List<JsonElement> skins = new ArrayList<>();
        object.getAsJsonArray("skins").iterator().forEachRemaining(skins::add);

        UUID userId = UUID.fromString(object.get("id").getAsString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5")); // Mojang does not like hyphens.

        String name = object.get("name").getAsString();

        String skinUrl = skins.stream()
                .map(JsonElement::getAsJsonObject)
                .filter(json -> "ACTIVE".equals(json.get("state").getAsString()))
                .findFirst()
                .map(json -> json.get("url").getAsString())
                .orElse(null);

        return new IMinecraftProfile() {
            @Override
            public UUID getUserId() {
                return userId;
            }

            @Override
            public String getSkinUrl() {
                return skinUrl;
            }

            @Override
            public String getUserName() {
                return name;
            }
        };
    }
}