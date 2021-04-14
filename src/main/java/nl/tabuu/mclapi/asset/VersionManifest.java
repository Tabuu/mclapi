package nl.tabuu.mclapi.asset;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import nl.tabuu.mclapi.asset.download.DownloadableAssetPackage;
import nl.tabuu.mclapi.util.HttpRequest;

import java.util.*;
import java.util.stream.Collectors;

public class VersionManifest {

    private static final String
            VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private final String _versionManifestUrl;

    private Map<String, IMCVersion> _versions;

    public VersionManifest(String versionManifestUrl) {
        _versionManifestUrl = versionManifestUrl;
    }

    public VersionManifest() {
        this(VERSION_MANIFEST_URL);
    }

    public Map<String, IMCVersion> getVersions() {
        if (Objects.isNull(_versions))
            _versions = Arrays.stream(findVersions()).collect(Collectors.toMap(IMCVersion::getId, v -> v));

        return Collections.unmodifiableMap(_versions);
    }

    private IMCVersion[] findVersions() {
        JsonObject manifest;
        try {
            manifest = HttpRequest.doJsonBodyRequest(_versionManifestUrl, "GET");
        } catch (Exception exception) { return new IMCVersion[0]; }

        Gson gson = new Gson();
        return gson.fromJson(manifest.get("versions"), MCVersionWrapper[].class);
    }

    public static class MCVersionWrapper implements IMCVersion {
        private String id, type, url;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public DownloadableAssetPackage getDownloadableAssetPackage() {
            return new DownloadableAssetPackage(getId(), url);
        }
    }
}