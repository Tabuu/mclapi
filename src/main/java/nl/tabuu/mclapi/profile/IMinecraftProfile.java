package nl.tabuu.mclapi.profile;

import java.util.UUID;

/**
 * Represents a Minecraft profile
 */
public interface IMinecraftProfile {

    /**
     * Returns the uuid of the profile.
     *
     * @return The uuid of the profile.
     */
    UUID getUserId();

    /**
     * Returns the url to the skin currently in use by this profile.
     *
     * @return The url to the skin currently in use by this profile.
     */
    String getSkinUrl(); // TODO: Skin data?

    /**
     * Returns the username of the profile.
     *
     * @return The username of the profile.
     */
    String getUserName();
}