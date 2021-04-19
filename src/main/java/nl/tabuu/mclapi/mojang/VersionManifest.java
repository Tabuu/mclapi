package nl.tabuu.mclapi.mojang;

import com.google.gson.Gson;
import nl.tabuu.mclapi.util.HttpRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Represents the Minecraft version manifest of the Mojang database.
 */
public class VersionManifest {

    private static final String
            MOJANG_VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private final Map<String, IMCVersion> _versions;

    protected VersionManifest(Map<String, MCVersionWrapper> versions) {
        _versions = new HashMap<>(versions);
    }

    public Map<String, IMCVersion> getVersions() {
        return Collections.unmodifiableMap(_versions);
    }

    public static CompletableFuture<VersionManifest> get(String manifestUrl) {
        return HttpRequest.doJsonBodyRequest(manifestUrl, "GET")
                .thenApply(response -> {
                    Gson gson = new Gson();
                    return gson.fromJson(response.get("versions"), MCVersionWrapper[].class);
                })
                .thenApply(versions -> Arrays.stream(versions).collect(Collectors.toMap(IMCVersion::getId, v -> v)))
                .thenApply(VersionManifest::new);
    }

    public static CompletableFuture<VersionManifest> get() {
        return get(MOJANG_VERSION_MANIFEST_URL);
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