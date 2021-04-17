package nl.tabuu.mclapi.authentication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.tabuu.mclapi.launcher.MCLauncher;
import nl.tabuu.mclapi.profile.IMinecraftProfile;
import nl.tabuu.mclapi.util.HttpRequest;

import java.io.IOException;
import java.util.*;

public class Session {

    public static final String
            MOJANG_AUTH_SERVER_URL = "https://authserver.mojang.com/%s",
            MOJANG_AUTH_SERVER_VALIDATE_ENPOINT = "validate",
            MOJANG_AUTH_SERVER_INVALIDATE_ENPOINT = "validate",
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
    public boolean isValid() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("accessToken", getId());
        requestBody.addProperty("clientToken", MCLauncher.getLauncherId().toString());

        try {
            return HttpRequest.doPostRequest(
                    String.format(MOJANG_AUTH_SERVER_URL, MOJANG_AUTH_SERVER_VALIDATE_ENPOINT),
                    null,
                    requestBody
            ) == 204;
        } catch (IOException ignored) {
        }

        return false;
    }

    /**
     * Invalidates this session.
     *
     * @return True if this session was successfully invalidated, otherwise false.
     */
    public boolean invalidate() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("accessToken", getId());
        requestBody.addProperty("clientToken", MCLauncher.getLauncherId().toString());

        try {
            return HttpRequest.doPostRequest(
                    String.format(MOJANG_AUTH_SERVER_URL, MOJANG_AUTH_SERVER_INVALIDATE_ENPOINT),
                    null,
                    requestBody
            ) == 204;
        } catch (IOException ignored) {
        }

        return false;
    }

    /**
     * Makes a request to the Mojang servers to fetch the profile, and caches this.
     *
     * @return The cached profile for this session.
     * @throws IllegalStateException If the profile could not be obtained from the Mojang servers.
     */
    public IMinecraftProfile getProfile() {
        if (Objects.isNull(_profile)) {
            try {
                _profile = fetchProfile();
            } catch (IOException exception) {
                throw new IllegalStateException("The profile could not be obtained from the Mojang servers.", exception);
            }
        }

        return _profile;
    }

    private IMinecraftProfile fetchProfile() throws IOException {
        JsonObject response = HttpRequest.doJsonBodyRequest(
                MINECRAFT_PROFILE_URL,
                "GET",
                Map.of(
                        "Authorization", String.format("Bearer %s", getId())),
                null);

        List<JsonElement> skins = new ArrayList<>();
        response.getAsJsonArray("skins").iterator().forEachRemaining(skins::add);

        UUID userId = UUID.fromString(response.get("id").getAsString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5")); // Mojang does not like hyphens.

        String name = response.get("name").getAsString();

        String skinUrl = skins.stream()
                .map(JsonElement::getAsJsonObject)
                .filter(object -> "ACTIVE".equals(object.get("state").getAsString()))
                .findFirst()
                .map(object -> object.get("url").getAsString())
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