package nl.tabuu.mclapi.launcher;

import nl.tabuu.mclapi.mojang.IMCVersion;
import nl.tabuu.mclapi.mojang.MCAssetPackage;
import nl.tabuu.mclapi.authentication.Session;
import nl.tabuu.mclapi.profile.IMinecraftProfile;
import nl.tabuu.mclapi.util.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MCLauncher {

    private final static String LAUNCHER_NAME = "Another Generic Minecraft Launcher";
    private final static UUID LAUNCHER_ID = UUID.fromString("5c9a9384-6c2d-432f-a2b5-d662b22cfee9");

    private final Map<String, IMinecraftProfile> _minecraftProfiles;
    private final OperatingSystem _operatingSystem;
    private final File _workDirectory;

    public MCLauncher(OperatingSystem operatingSystem, File workingDirection) {
        _minecraftProfiles = new HashMap<>();
        _operatingSystem = operatingSystem;
        _workDirectory = workingDirection;
    }

    public MCLauncher() {
        this(OperatingSystem.getCurrent(), OperatingSystem.getCurrent().getMinecraftDirectory());
    }

    public CompletableFuture<ProcessBuilder> createMinecraftProcess(LauncherProfile profile, Session session) {
        return getLaunchCommand(session, profile.getVersion())
                .thenApply(ProcessBuilder::new);
    }

    public CompletableFuture<Boolean> launch(LauncherProfile profile, Session session) {
        return createMinecraftProcess(profile, session)
                .thenApply(process -> {
                    try {
                        process.start();
                        return true;
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        return false;
                    }
                });
    }

    public CompletableFuture<List<String>> getLaunchCommand(Session session, IMCVersion version) {
        File libraries = new File(_workDirectory, "/libraries/");
        File jar = new File(_workDirectory, String.format("/versions/%s/%s.jar", version.getId(), version.getId()));

        return session.getProfile().thenCombine(getLibraryString(libraries, version), (profile, string) ->
            Arrays.asList(
                    "java",
                    "-Xms256M",
                    "-Xmx1G",
                    String.format("-Djava.library.path=%s", new File(_workDirectory, String.format("/versions/%s/natives/", version.getId()))),
                    "-cp", jar.getPath() + _operatingSystem.getPathSeparator() + string,
                    "-Dminecraft.launcher.brand=" + LAUNCHER_NAME,
                    "-Dminecraft.launcher.version=boii",
                    "net.minecraft.client.main.Main",
                    "--username", profile.getUserName(),
                    "--version", version.getId(),
                    "--gameDir", _workDirectory.getPath(),
                    "--assetsDir", new File(_workDirectory, "/assets/").getPath(),
                    "--assetIndex", version.getId(),
                    "--uuid", profile.getUserId().toString().replaceAll("-", ""),
                    "--accessToken", session.getId(),
                    "--userType", "legacy",
                    "--versionType", "release"
            )
        );
    }

    private CompletableFuture<String> getLibraryString(File library, IMCVersion version) {
        return version.getAssetPackage()
                .thenApply(pack -> {
                    List<MCAssetPackage.DownloadableLibraryWrapper> libraries = new LinkedList<>();
                    libraries.addAll(Arrays.asList(pack.getClassifiers()));
                    libraries.addAll(Arrays.asList(pack.getLibraries()));

                    return libraries.stream()
                            .map(MCAssetPackage.DownloadableLibraryWrapper::getPath)
                            .map(s -> new File(library, s).getPath())
                            .collect(Collectors.joining(_operatingSystem.getPathSeparator()));
                });
    }

    public Optional<IMinecraftProfile> getMinecraftProfile(String username) {
        return Optional.ofNullable(_minecraftProfiles.get(username));
    }

    public Set<IMinecraftProfile> getMinecraftProfiles() {
        return new HashSet<>(_minecraftProfiles.values());
    }

    public void saveMinecraftProfile(IMinecraftProfile profile) {
        _minecraftProfiles.put(profile.getUserName(), profile);
    }

    public static String getLauncherName() {
        return LAUNCHER_NAME;
    }

    public static UUID getLauncherId() {
        return LAUNCHER_ID;
    }
}