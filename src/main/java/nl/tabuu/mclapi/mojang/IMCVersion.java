package nl.tabuu.mclapi.mojang;

/**
 * Represents a Minecraft version.
 */
public interface IMCVersion {
    /**
     * Returns the ID of the version, e.g.: "1.16.5", "21w03a", etc.
     * @return the ID of the version, e.g.: "1.16.5", "21w03a", etc.
     */
    String getId();

    /**
     * Returns version type, e.g.: "snapshot" & "release".
     * @return version type, e.g.: "snapshot" & "release".
     */
    String getType();

    /**
     * Returns the url of the asset manifest.
     * @return the url of the asset manifest.
     */
    String getAssetManifestUrl();

    /**
     * Returns a MCAssetPackage based on this version.
     * @return A MCAssetPackage based on this version.
     */
    default MCAssetPackage getAssetPackage() {
        return new MCAssetPackage(this);
    }
}