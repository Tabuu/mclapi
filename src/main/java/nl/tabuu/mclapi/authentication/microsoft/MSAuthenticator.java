package nl.tabuu.mclapi.authentication.microsoft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import nl.tabuu.mclapi.authentication.AuthenticationResponse;
import nl.tabuu.mclapi.authentication.IAuthenticator;
import nl.tabuu.mclapi.authentication.Session;
import nl.tabuu.mclapi.util.HttpRequest;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a Minecraft Microsoft authenticator.
 */
public class MSAuthenticator implements IAuthenticator<MSAuthenticationRequest> {

    private static final String
            AUTH_TOKEN_URL = "https://login.live.com/oauth20_token.srf",
            XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate",
            XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize",
            MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";

    @Override
    public CompletableFuture<AuthenticationResponse> authenticate(MSAuthenticationRequest request) {
        return getMicrosoftAccessToken(request.getAuthorisationToken())
                .thenCompose(this::getXBLToken)
                .thenCompose(this::getXSTSToken)
                .thenCompose(this::getMinecraftSessionId)
                .exceptionally(throwable -> "")
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

    /**
     * Returns the Microsoft access token.
     *
     * @param authCode The Authorization token that should be retrieved from the browser.
     *                 Example URL: https://login.live.com/oauth20_authorize.srf?client_id=<client_id>&response_type=code&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&scope=XboxLive.signin%20offline_access
     * @return The Microsoft access token.
     */
    private CompletableFuture<String> getMicrosoftAccessToken(String authCode) {
        Map<String, String> data = Map.of(
                "client_id", "00000000402b5328", // key borrowed from MiniDigger TODO: Get own client_id
                "code", authCode,
                "grant_type", "authorization_code",
                "redirect_uri", "https://login.live.com/oauth20_desktop.srf",
                "scope", "service::user.auth.xboxlive.com::MBI_SSL"
        );

        String url = HttpRequest.getParameterizedUri(AUTH_TOKEN_URL, data);

        return HttpRequest.doJsonBodyRequest(url, "GET",
                Map.of("Accept", "application/json"), null)
                .thenApply(response -> response.get("access_token").getAsString());
    }

    private CompletableFuture<TokenPair> getXBLToken(String accessToken) {
        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", String.format("t=%s", accessToken));

        JsonObject json = new JsonObject();
        json.add("Properties", properties);
        json.addProperty("RelyingParty", "http://auth.xboxlive.com");
        json.addProperty("TokenType", "JWT");

        return HttpRequest.doJsonBodyRequest(XBL_AUTH_URL, "POST", Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json",
                "x-xbl-contract-version", "1"
        ), json).thenApply(response -> {
            String token = response.get("Token").getAsString();
            String userHash = response
                    .getAsJsonObject("DisplayClaims")
                    .getAsJsonArray("xui")
                    .get(0)
                    .getAsJsonObject()
                    .get("uhs")
                    .getAsString();

            return new TokenPair(token, userHash);
        });
    }

    private CompletableFuture<TokenPair> getXSTSToken(TokenPair xblToken) {
        JsonArray tokens = new JsonArray();
        tokens.add(xblToken.getToken());

        JsonObject properties = new JsonObject();
        properties.addProperty("SandboxId", "RETAIL");
        properties.add("UserTokens", tokens);

        JsonObject json = new JsonObject();
        json.add("Properties", properties);
        json.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        json.addProperty("TokenType", "JWT");

        return HttpRequest.doJsonBodyRequest(XSTS_AUTH_URL, "POST", Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json",
                "x-xbl-contract-version", "1"
        ), json).thenApply(response -> {
            String token = response.get("Token").getAsString();
            String userHash = response
                    .getAsJsonObject("DisplayClaims")
                    .getAsJsonArray("xui")
                    .get(0)
                    .getAsJsonObject()
                    .get("uhs")
                    .getAsString();

            return new TokenPair(token, userHash);
        });
    }

    private CompletableFuture<String> getMinecraftSessionId(TokenPair xstsToken) {
        JsonObject json = new JsonObject();
        json.addProperty("identityToken", String.format("XBL3.0 x=%s;%s", xstsToken.getHash(), xstsToken.getToken()));

        return HttpRequest.doJsonBodyRequest(MC_LOGIN_URL, "POST", Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json"
        ), json).thenApply(response -> response.get("access_token").getAsString());
    }

    private static class TokenPair {
        private final String token, hash;

        public TokenPair(String token, String hash) {
            this.token = token;
            this.hash = hash;
        }

        public String getToken() {
            return token;
        }

        public String getHash() {
            return hash;
        }

        @Override
        public String toString() {
            return "TokenPair{" +
                    "token='" + token + '\'' +
                    ", hash='" + hash + '\'' +
                    '}';
        }
    }
}