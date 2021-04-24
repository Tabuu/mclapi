package nl.tabuu.mclapi.mojang;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import nl.tabuu.mclapi.util.FileUtil;
import nl.tabuu.mclapi.util.HttpRequest;
import nl.tabuu.mclapi.util.os.OperatingSystem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MCAssetPackage {

    private static final String
            RESOURCE_URL = "http://resources.download.minecraft.net/%.2s/%s";

    private final IMCVersion _version;
    private final DownloadableAssetWrapper _client;
    private final DownloadableAssetWrapper _assetIndex;
    private final DownloadableLibraryWrapper[] _libraries;
    private final DownloadableLibraryWrapper[] _classifiers;

    protected MCAssetPackage(IMCVersion version, DownloadableAssetWrapper client, DownloadableAssetWrapper assetIndex, DownloadableLibraryWrapper[] libraries, DownloadableLibraryWrapper[] classifiers) {
        _version = version;
        _client = client;
        _assetIndex = assetIndex;
        _libraries = libraries;
        _classifiers = classifiers;
    }

    public DownloadableAssetWrapper getClient() {
        return _client;
    }

    public IMCVersion getVersion() {
        return _version;
    }

    public DownloadableLibraryWrapper[] getLibraries() {
        return _libraries;
    }

    public DownloadableLibraryWrapper[] getClassifiers() {
        return _classifiers;
    }

    public CompletableFuture<Boolean> download(File target) {
        File client = new File(target, String.format("/versions/%s/%s.jar", getVersion().getId(), getVersion().getId()));
        File index = new File(target, String.format("/assets/indexes/%s.json", getVersion().getId()));

        CompletableFuture<Boolean> download = _client.download(client);

        // Download libraries
        for (DownloadableLibraryWrapper library : getLibraries()) {
            download.thenComposeAsync(downloaded -> {
                if(!downloaded) return CompletableFuture.completedFuture(false);
                return library.download(new File(target, String.format("/libraries/%s", library.getPath())));
            });
        }

        // Download native libraries/classifiers
        for (DownloadableLibraryWrapper library : getClassifiers()) {
            download.thenComposeAsync(downloaded -> {
                if(!downloaded) return CompletableFuture.completedFuture(false);
                return library.download(new File(target, String.format("/libraries/%s", library.getPath())));
            });
        }

        // Download asset index
        download.thenComposeAsync(downloaded -> {
            if(!downloaded) return CompletableFuture.completedFuture(false);
            return _assetIndex.download(index);
        });

        FileReader indexReader;
        try {
            indexReader = new FileReader(index);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }

        Gson gson = new Gson();
        JsonReader reader = new JsonReader(indexReader);
        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("objects");

        // Downloading all assets from the asset index
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            DownloadableGameAssetWrapper gameAsset = gson.fromJson(entry.getValue(), DownloadableGameAssetWrapper.class);
            File asset = new File(target, String.format("/assets/objects/%.2s/%s", gameAsset.getHash(), gameAsset.getHash()));

            download.thenComposeAsync(downloaded -> {
                if(!downloaded) return CompletableFuture.completedFuture(false);
                return gameAsset.download(asset);
            });
        }

        // Extracting native files from the classifier libraries
        for (DownloadableLibraryWrapper classifier : getClassifiers()) {
            File file = new File(target, String.format("/libraries/%s", classifier.getPath()));
            File destination = new File(target, String.format("/versions/%s/natives/", getVersion().getId()));
            try {
                FileUtil.unzip(file, destination);
            } catch (IOException exception) {
                exception.printStackTrace();
                return CompletableFuture.completedFuture(false);
            }
        }

        return download;
    }

    public static CompletableFuture<MCAssetPackage> fromVersion(IMCVersion version) {
        return HttpRequest.doJsonBodyRequest(version.getAssetManifestUrl(), "GET").thenApply(manifest -> {
            Gson gson = new Gson();
            DownloadableAssetWrapper client = gson.fromJson(manifest.getAsJsonObject("downloads").get("client"), DownloadableAssetWrapper.class);
            DownloadableAssetWrapper assetIndex = gson.fromJson(manifest.get("assetIndex"), DownloadableAssetWrapper.class);

            List<JsonElement> manifestEntries = new LinkedList<>();
            manifest.getAsJsonArray("libraries").forEach(manifestEntries::add);

            List<JsonObject> downloads = manifestEntries.stream()
                    .map(JsonElement::getAsJsonObject)
                    .map(element -> element.getAsJsonObject("downloads"))
                    .collect(Collectors.toList());

            DownloadableLibraryWrapper[] libraries = downloads.stream()
                    .map(element -> element.getAsJsonObject("artifact"))
                    .map(element -> gson.fromJson(element, DownloadableLibraryWrapper.class))
                    .toArray(DownloadableLibraryWrapper[]::new);

            String nativeId = String.format("natives-%s", OperatingSystem.getCurrent().getMinecraftId());
            DownloadableLibraryWrapper[] classifiers = downloads.stream()
                    .filter(element -> element.has("classifiers"))
                    .map(element -> element.getAsJsonObject("classifiers"))
                    .filter(element -> element.has(nativeId))
                    .map(element -> element.getAsJsonObject(nativeId))
                    .map(element -> gson.fromJson(element, DownloadableLibraryWrapper.class))
                    .toArray(DownloadableLibraryWrapper[]::new);

            return new MCAssetPackage(version, client, assetIndex, libraries, classifiers);
        });
    }

    public static class DownloadableGameAssetWrapper implements IDownloadableAsset {
        private String hash;
        private int size;

        @Override
        public String getUrl() {
            return String.format(RESOURCE_URL, getHash(), getHash());
        }

        @Override
        public String getHash() {
            return hash;
        }

        @Override
        public int getSize() {
            return size;
        }
    }

    public static class DownloadableAssetWrapper implements IDownloadableAsset {
        private String sha1, url;
        private int size;

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getHash() {
            return sha1;
        }

        @Override
        public int getSize() {
            return size;
        }
    }

    public static class DownloadableLibraryWrapper extends DownloadableAssetWrapper {
        private String path;
        private Map<String, DownloadableLibraryWrapper> classifiers;

        public String getPath() {
            return path;
        }
    }
}