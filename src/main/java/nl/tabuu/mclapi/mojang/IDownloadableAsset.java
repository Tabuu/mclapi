package nl.tabuu.mclapi.mojang;

import nl.tabuu.mclapi.util.FileUtil;

import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a downloadable asset from the Mojang database.
 */
public interface IDownloadableAsset {

    /**
     * Returns the url of the asset.
     * @return the url of the asset.
     */
    String getUrl();

    /**
     * Returns the sha1-hash of the file, according to the database.
     * @return the sha1-hash of the file, according to the database.
     */
    String getHash();

    /**
     * Returns the byte-size of the asset, according to the database.
     * @return the byte-size of the asset, according to the database.
     */
    int getSize();

    /**
     * Downloads the asset to the target directory.
     * @param target The directory to download the asset to.
     * @return True if the asset downloaded successfully, false otherwise.
     */
    default boolean download(File target) {
        if (target.exists()) {
            String sha1;
            try {
                sha1 = FileUtil.getSha1(target);
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
                return false;
            }

            if (getHash().equals(sha1))
                return true;
        }

        target.getParentFile().mkdirs();
        try (BufferedInputStream in = new BufferedInputStream(new URL(getUrl()).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(target)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
