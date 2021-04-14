package nl.tabuu.mclapi.authentication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.tabuu.mclapi.profile.IProfile;
import nl.tabuu.mclapi.util.HttpRequest;

import java.io.IOException;
import java.util.*;

public class Session {

    private final String _sessionId;
    private IProfile _profile;

    public Session(String sessionId) {
        _sessionId = sessionId;
    }

    IProfile getCachedProfile() {
        return _profile;
    }

    public String getId() {
        return _sessionId;
    }

    public boolean isValid() {
        try {
            return HttpRequest.doPostRequest(
                    IProfile.getProfileEndpoint(),
                    Map.of(
                            "Authorization", String.format("Bearer %s", getId())
                    ),
                    null) != 200;
        } catch (IOException exception) {
            return false;
        }
    }

    public IProfile getProfile() {
        JsonObject response = null;

        try {
            response = HttpRequest.doJsonBodyRequest(
                    IProfile.getProfileEndpoint(),
                    "GET",
                    Map.of(
                            "Authorization", String.format("Bearer %s", getId())),
                    null);
        } catch (Exception ignored) { }

        assert Objects.nonNull(response) : "Cannot get profile";

        List<JsonElement> skins = new ArrayList<>();
        response.getAsJsonArray("skins").iterator().forEachRemaining(skins::add);

        final UUID userId = UUID.fromString(response.get("id").getAsString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5")); // Mojang does not like hyphens.
        final String name = response.get("name").getAsString();

        final String skinUrl = skins.stream()
                .map(JsonElement::getAsJsonObject)
                .filter(object -> "ACTIVE".equals(object.get("state").getAsString()))
                .findFirst()
                .map(object -> object.get("url").getAsString())
                .orElse(null);

        IProfile profile = new IProfile() {
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

        _profile = profile;

        return _profile;
    }
}