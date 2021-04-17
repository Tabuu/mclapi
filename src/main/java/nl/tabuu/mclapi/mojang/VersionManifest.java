package nl.tabuu.mclapi.mojang;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import nl.tabuu.mclapi.util.HttpRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the Minecraft version manifest of the Mojang database.
 */
public class VersionManifest {

    private static final String
            MOJANG_VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private Map<String, IMCVersion> _versions;
    private String _manifestUrl;

    public VersionManifest(String manifestUrl) {
        _manifestUrl = manifestUrl;
    }

    public VersionManifest() {
        this(MOJANG_VERSION_MANIFEST_URL);
    }

    public Map<String, IMCVersion> getVersions() {
        if (Objects.isNull(_versions))
            _versions = Arrays.stream(findVersions()).collect(Collectors.toMap(IMCVersion::getId, v -> v));

        return Collections.unmodifiableMap(_versions);
    }

    private IMCVersion[] findVersions() {
        JsonObject manifest;
        try {
            manifest = HttpRequest.doJsonBodyRequest(_manifestUrl, "GET");
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
        public String getAssetManifestUrl() {
            return url;
        }
    }
}