package nl.tabuu.mclapi.asset.download;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import nl.tabuu.mclapi.util.FileUtil;
import nl.tabuu.mclapi.util.HttpRequest;
import nl.tabuu.mclapi.util.os.OperatingSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DownloadableAssetPackage {

    private static final String
            RESOURCE_URL = "http://resources.download.minecraft.net/%.2s/%s";

    private final String _id;
    private final String _name;
    private final DownloadableAssetWrapper _client;
    private final DownloadableAssetWrapper _assetIndex;
    private final DownloadableLibraryWrapper[] _libraries;
    private final DownloadableLibraryWrapper[] _classifiers;

    public DownloadableAssetPackage(String id, String manifestUrl) {
        _id = id;
        JsonObject manifest;

        try {
            manifest = HttpRequest.doJsonBodyRequest(manifestUrl, "GET");
        } catch (Exception exception) {
            throw new IllegalStateException("Could not obtain asset package.");
        }

        _name = manifest.get("assets").getAsString();

        Gson gson = new Gson();
        _client = gson.fromJson(manifest.getAsJsonObject("downloads").get("client"), DownloadableAssetWrapper.class);
        _assetIndex = gson.fromJson(manifest.get("assetIndex"), DownloadableAssetWrapper.class);

        List<JsonElement> manifestEntries = new LinkedList<>();
        manifest.getAsJsonArray("libraries").forEach(manifestEntries::add);

        List<JsonObject> downloads = manifestEntries.stream()
                .map(JsonElement::getAsJsonObject)
                .map(element -> element.getAsJsonObject("downloads"))
                .collect(Collectors.toList());

        _libraries = downloads.stream()
                .map(element -> element.getAsJsonObject("artifact"))
                .map(element -> gson.fromJson(element, DownloadableLibraryWrapper.class))
                .toArray(DownloadableLibraryWrapper[]::new);

        String nativeId = String.format("natives-%s", OperatingSystem.getCurrent().getMinecraftId());
        _classifiers = downloads.stream()
                .filter(element -> element.has("classifiers"))
                .map(element -> element.getAsJsonObject("classifiers"))
                .filter(element -> element.has(nativeId))
                .map(element -> element.getAsJsonObject(nativeId))
                .map(element -> gson.fromJson(element, DownloadableLibraryWrapper.class))
                .toArray(DownloadableLibraryWrapper[]::new);
    }

    public DownloadableAssetWrapper getClient() {
        return _client;
    }

    public DownloadableLibraryWrapper[] getLibraries() {
        return _libraries;
    }

    public DownloadableLibraryWrapper[] getClassifiers() {
        return _classifiers;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public boolean download(File target) {
        if (!_client.download(new File(new File(target, String.format("/versions/%s/", getId())), String.format("/%s.jar", getId()))))
            return false;

        for (DownloadableLibraryWrapper library : getLibraries())
            if (!library.download(new File(target, String.format("/libraries/%s", library.getPath()))))
                return false;

        for (DownloadableLibraryWrapper library : getClassifiers())
            if (!library.download(new File(target, String.format("/libraries/%s", library.getPath()))))
                return false;

        File indexFile = new File(target, String.format("/assets/indexes/%s.json", getId()));
        if (!_assetIndex.download(indexFile))
            return false;

        FileReader indexReader;
        try {
            indexReader = new FileReader(indexFile);
        } catch (FileNotFoundException e) {
            return false;
        }

        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonReader reader = new JsonReader(indexReader);
        JsonObject object = parser.parse(reader).getAsJsonObject().getAsJsonObject("objects");

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            DownloadableGameAssetWrapper gameAsset = gson.fromJson(entry.getValue(), DownloadableGameAssetWrapper.class);
            if(!gameAsset.download(new File(target, String.format("/assets/objects/%.2s/%s", gameAsset.getHash(), gameAsset.getHash()))))
                return false;
        }

        System.out.println(getClassifiers().length);

        // Extracting
        for (DownloadableLibraryWrapper classifier : getClassifiers()) {

            File file = new File(target, String.format("/libraries/%s", classifier.getPath()));
            File destination = new File(target, String.format("/versions/%s/natives/", getId()));
            try {
                FileUtil.unzip(file, destination);
            } catch (IOException exception) {
                exception.printStackTrace();
                return false;
            }

        }

        return true;
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
