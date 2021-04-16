package nl.tabuu.mclapi.launcher;

import nl.tabuu.mclapi.mojang.IMCVersion;

import java.io.File;

public interface ILauncherProfile {
    IMCVersion getVersion();

    File getGameDirectory();

    File getAssetDirectory();

    String getAssetIndexName();
}
