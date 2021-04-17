package nl.tabuu.mclapi.launcher;

import nl.tabuu.mclapi.mojang.IMCVersion;
import nl.tabuu.mclapi.util.os.OperatingSystem;

import java.io.File;

public class LauncherProfile {

    private IMCVersion _version;
    private File _gameDirectory, _assetDirectory;

    public LauncherProfile(IMCVersion version, File gameDirectory, File assetDirectory) {
        _version = version;
        _gameDirectory = gameDirectory;
        _assetDirectory = assetDirectory;
    }

    public LauncherProfile(IMCVersion version, OperatingSystem operatingSystem) {
        this(version, operatingSystem.getMinecraftDirectory(), new File(operatingSystem.getMinecraftDirectory(), "/assets/"));
    }

    public LauncherProfile(IMCVersion version) {
        this(version, OperatingSystem.getCurrent());
    }

    public IMCVersion getVersion() {
        return _version;
    }

    public File getGameDirectory() {
        return _gameDirectory;
    }

    public File getAssetDirectory() {
        return _assetDirectory;
    }

    public String getAssetIndexName() {
        return getVersion().getId();
    }
}
