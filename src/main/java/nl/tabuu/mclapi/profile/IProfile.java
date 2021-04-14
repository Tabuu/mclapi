package nl.tabuu.mclapi.profile;

import java.util.UUID;

public interface IProfile {
    UUID getUserId();
    String getSkinUrl();
    String getUserName();

    static String getProfileEndpoint() {
        return "https://api.minecraftservices.com/minecraft/profile";
    }
}