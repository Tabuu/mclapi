package nl.tabuu.mclapi.authentication.yggdrasil;

import com.google.gson.JsonObject;
import nl.tabuu.mclapi.authentication.AuthenticationResponse;
import nl.tabuu.mclapi.authentication.IAuthenticator;
import nl.tabuu.mclapi.authentication.Session;
import nl.tabuu.mclapi.util.HttpRequest;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a Minecraft Mojang authenticator.
 */
public class YDAuthenticator implements IAuthenticator<YDAuthenticationRequest> {

    private final static String
        AUTHENTICATE_URL = "https://authserver.mojang.com/authenticate";

    @Override
    public CompletableFuture<AuthenticationResponse> authenticate(YDAuthenticationRequest request) {
        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);

        JsonObject requestBody = new JsonObject();
        requestBody.add("agent", agent);
        requestBody.addProperty("username", request.getUsername());
        requestBody.addProperty("password", request.getPassword());

        return HttpRequest.doJsonBodyRequest(AUTHENTICATE_URL, "GET",
                Map.of("Content-Type", "application/json",
                        "Accept", "application/json"
                ), requestBody)
                .thenApply(response -> response.get("accessToken").getAsString())
                .exceptionally(throwable -> { throwable.printStackTrace(); return ""; } )
                .thenApply(id -> id.isEmpty() ? null : new Session(id))
                .thenCompose(s -> {
                    if (Objects.isNull(s))
                        return CompletableFuture.completedFuture(new AuthenticationResponse(AuthenticationResponse.State.NO_AUTHENTICATION));

                    return s.isValid().thenApply(valid -> {
                        if (!valid)
                            return new AuthenticationResponse(AuthenticationResponse.State.NO_SESSION);
                        return new AuthenticationResponse(AuthenticationResponse.State.SUCCESS, s);
                    });
                });
    }
}