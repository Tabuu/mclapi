package nl.tabuu.mclapi.launcher;

import nl.tabuu.mclapi.asset.IMCVersion;
import nl.tabuu.mclapi.asset.download.DownloadableAssetPackage;
import nl.tabuu.mclapi.authentication.Session;
import nl.tabuu.mclapi.util.os.OperatingSystem;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MCLauncher {

    private final static String LAUNCHER_NAME = "Another Generic Minecraft Launcher";
    private final static UUID LAUNCHER_ID = UUID.fromString("5c9a9384-6c2d-432f-a2b5-d662b22cfee9");

    private File _workDirectory;

    public MCLauncher(File workingDirection) {
        _workDirectory = workingDirection;
    }

    public ProcessBuilder createMinecraftProcess(Session session, IMCVersion version) {
        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = getLaunchCommand(session, version);
        System.out.println(String.join(" ", command));

        processBuilder.command(command);
        return processBuilder;
    }

    public List<String> getLaunchCommand(Session session, IMCVersion version) {
        File dir = OperatingSystem.getCurrent().getMinecraftDirectory();
        System.out.println(getLibraryString(new File(dir, "/libraries/"), version));

        File libraries = new File(dir, "/libraries/");
        File jar = new File(dir, String.format("/versions/%s/%s.jar", version.getId(), version.getId()));
        System.out.println(jar.exists());

        return Arrays.asList(
                "java",
                "-Xms256M",
                "-Xmx1G",
                String.format("-Djava.library.path=%s", new File(dir, String.format("/versions/%s/natives/", version.getId()))),
                "-cp", jar.getPath() + ":" + getLibraryString(libraries, version),
//                "-Dminecraft.launcher.brand=Ye",
//                "-Dminecraft.launcher.version=boii",
                "net.minecraft.client.main.Main",
                "--username", session.getProfile().getUserName(),
                "--version", version.getId(),
                "--gameDir", dir.getPath(),
                "--assetsDir", new File(dir, "/assets/").getPath(),
                "--assetIndex", version.getId(),
                "--uuid", session.getProfile().getUserId().toString().replaceAll("-", ""),
                "--accessToken", session.getId(),
                "--userType", "legacy",
                "--versionType", "release"
        );
    }

    private String getLibraryString(File library, IMCVersion version) {
        List<DownloadableAssetPackage.DownloadableLibraryWrapper> libraries = new LinkedList<>();
        libraries.addAll(Arrays.asList(version.getDownloadableAssetPackage().getLibraries()));
        libraries.addAll(Arrays.asList(version.getDownloadableAssetPackage().getClassifiers()));

        return libraries.stream()
                .map(DownloadableAssetPackage.DownloadableLibraryWrapper::getPath)
                .map(s -> new File(library, s).getPath())
                .collect(Collectors.joining(":"));
    }
}
