package nl.tabuu.mclapi.authentication.yggdrasil;

import com.google.gson.JsonObject;
import nl.tabuu.mclapi.authentication.Session;
import nl.tabuu.mclapi.util.HttpRequest;

import java.io.IOException;
import java.util.Map;

public class YDAuthenticator {

    private final static String
        AUTHENTICATE_URL = "https://authserver.mojang.com/authenticate";

    public Session authenticate(String username, String password) {
        try {
            return new Session(getMojangAccessToken(username, password));
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public String getMojangAccessToken(String username, String password) throws IOException {
        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);

        JsonObject request = new JsonObject();
        request.add("agent", agent);
        request.addProperty("username", username);
        request.addProperty("password", password);

        JsonObject response = HttpRequest.doJsonBodyRequest(AUTHENTICATE_URL, "GET",
                Map.of("Content-Type", "application/json",
                        "Accept", "application/json"
                ), request);

        return response.get("accessToken").getAsString();
    }

}
