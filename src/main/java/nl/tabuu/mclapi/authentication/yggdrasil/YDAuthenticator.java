package nl.tabuu.mclapi.authentication.yggdrasil;

import com.google.gson.JsonObject;
import nl.tabuu.mclapi.authentication.AuthenticationResponse;
import nl.tabuu.mclapi.authentication.IAuthenticator;
import nl.tabuu.mclapi.authentication.Session;
import nl.tabuu.mclapi.util.HttpRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a Minecraft Mojang authenticator.
 */
public class YDAuthenticator implements IAuthenticator<YDAuthenticationRequest> {

    private final static String
        AUTHENTICATE_URL = "https://authserver.mojang.com/authenticate";

    @Override
    public AuthenticationResponse authenticate(YDAuthenticationRequest request) {

        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);

        JsonObject requestBody = new JsonObject();
        requestBody.add("agent", agent);
        requestBody.addProperty("username", request.getUsername());
        requestBody.addProperty("password", request.getPassword());

        String sessionId;

        try {
            JsonObject response = HttpRequest.doJsonBodyRequest(AUTHENTICATE_URL, "GET",
                    Map.of("Content-Type", "application/json",
                            "Accept", "application/json"
                    ), requestBody);

            sessionId = response.get("accessToken").getAsString();
        } catch (Exception exception) {
            return new AuthenticationResponse(AuthenticationResponse.State.NO_AUTHENTICATION);
        }

        if(Objects.isNull(sessionId))
            return new AuthenticationResponse(AuthenticationResponse.State.NO_SESSION);

        return new AuthenticationResponse(AuthenticationResponse.State.SUCCESS, new Session(sessionId));
    }
}