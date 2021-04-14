package nl.tabuu.mclapi.asset;

import nl.tabuu.mclapi.asset.download.DownloadableAssetPackage;

public interface IMCVersion {
    String getId();

    String getType();

    DownloadableAssetPackage getDownloadableAssetPackage();
}
