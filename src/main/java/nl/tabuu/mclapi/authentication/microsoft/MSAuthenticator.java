package nl.tabuu.mclapi.authentication.microsoft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import nl.tabuu.mclapi.authentication.IAuthenticator;
import nl.tabuu.mclapi.authentication.Session;
import nl.tabuu.mclapi.util.HttpRequest;

import java.io.IOException;
import java.util.Map;

public class MSAuthenticator implements IAuthenticator {

    private static final String
            AUTH_TOKEN_URL = "https://login.live.com/oauth20_token.srf",
            XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate",
            XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize",
            MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";

    public Session authenticate(String authenticationCode) {
        try {
            String msAccessToken = getMicrosoftAccessToken(authenticationCode);
            TokenPair xblToken = getXBLToken(msAccessToken);
            TokenPair xstsToken = getXSTSToken(xblToken);
            String mcAccessToken = getMinecraftAccessToken(xstsToken);

            return new Session(mcAccessToken);
        } catch (IOException ignored) { }

        return null;
    }

    /**
     * Returns the Microsoft access token.
     * @param authCode The Authorization token that should be retrieved from the browser.
     *                 Example URL: https://login.live.com/oauth20_authorize.srf?client_id=<client_id>&response_type=code&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&scope=XboxLive.signin%20offline_access
     * @return The Microsoft access token.
     * @throws IOException When something went wrong while getting the token.
     */
    private String getMicrosoftAccessToken(String authCode) throws IOException {
        Map<String, String> data = Map.of(
                "client_id", "00000000402b5328", // key borrowed from MiniDigger TODO: Get own client_id
                "code", authCode,
                "grant_type", "authorization_code",
                "redirect_uri", "https://login.live.com/oauth20_desktop.srf",
                "scope", "service::user.auth.xboxlive.com::MBI_SSL"
        );

        String url = HttpRequest.getParameterizedUri(AUTH_TOKEN_URL, data);

        JsonObject response = HttpRequest.doJsonBodyRequest(url, "POST", null, null);
        return response.get("access_token").getAsString();
    }

    private TokenPair getXBLToken(String accessToken) throws IOException {
        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", String.format("t=%s", accessToken));

        JsonObject json = new JsonObject();
        json.add("Properties", properties);
        json.addProperty("RelyingParty", "http://auth.xboxlive.com");
        json.addProperty("TokenType", "JWT");

        JsonObject response = HttpRequest.doJsonBodyRequest(XBL_AUTH_URL, "POST", Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json",
                "x-xbl-contract-version", "1"
        ),json);

        String token = response.get("Token").getAsString();

        JsonObject displayClaims = (JsonObject) response.get("DisplayClaims");
        JsonArray xui = (JsonArray) displayClaims.get("xui");
        JsonObject container = (JsonObject) xui.get(0);
        String uhs = container.get("uhs").getAsString();

        return new TokenPair(token, uhs);
    }

    private TokenPair getXSTSToken(TokenPair xblToken) throws IOException {
        JsonArray tokens = new JsonArray();
        tokens.add(xblToken.getToken());

        JsonObject properties = new JsonObject();
        properties.addProperty("SandboxId", "RETAIL");
        properties.add("UserTokens", tokens);

        JsonObject json = new JsonObject();
        json.add("Properties", properties);
        json.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        json.addProperty("TokenType", "JWT");

        JsonObject response = HttpRequest.doJsonBodyRequest(XSTS_AUTH_URL, "POST", Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json",
                "x-xbl-contract-version", "1"
        ),json);

        String token = response.get("Token").getAsString();

        JsonObject displayClaims = (JsonObject) response.get("DisplayClaims");
        JsonArray xui = (JsonArray) displayClaims.get("xui");
        JsonObject container = (JsonObject) xui.get(0);
        String uhs = container.get("uhs").getAsString();

        return new TokenPair(token, uhs);
    }

    private String getMinecraftAccessToken(TokenPair xstsToken) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("identityToken", String.format("XBL3.0 x=%s;%s", xstsToken.getHash(), xstsToken.getToken()));

        JsonObject response = HttpRequest.doJsonBodyRequest(MC_LOGIN_URL, "POST", Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json"
        ),json);

        return response.get("access_token").getAsString();
    }

    private class TokenPair {
        private String token, hash;

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