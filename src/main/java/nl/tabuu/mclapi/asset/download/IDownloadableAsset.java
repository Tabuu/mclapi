package nl.tabuu.mclapi.asset.download;

import nl.tabuu.mclapi.util.FileUtil;

import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public interface IDownloadableAsset {
    String getUrl();

    String getHash();

    int getSize();

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
