package nl.tabuu.mclapi.util.os;

import java.io.File;
import java.util.Objects;

public class OperatingSystem {

    private static OperatingSystem CURRENT;
    public static final OperatingSystem
            WINDOWS = new OperatingSystem("Windows", "windows", getMinecraftDirectoryWindows()),
            SOLARIS = new OperatingSystem("Solaris/Sun OS", "solaris", getMinecraftDirectorySolaris()),
            UNIX = new OperatingSystem("Linux/Unix", "linux", getMinecraftDirectoryUnix()),
            MAC = new OperatingSystem("OSX", "osx", getMinecraftDirectoryUnix());

    private final String _name, _version, _architecture, _variableDelimiter, _minecraftId;
    private final File _minecraftDirectory;

    public OperatingSystem(String name, String version, String architecture, String variableDelimiter, String minecraftId, File minecraftDirectory) {
        _name = name;
        _version = version;
        _architecture = architecture;
        _variableDelimiter = variableDelimiter;
        _minecraftId = minecraftId;
        _minecraftDirectory = minecraftDirectory;
    }

    public OperatingSystem(String name, String minecraftId, File minecraftDirectory) {
        this(name, System.getProperty("os.version"), System.getProperty("os.arch"), System.getProperty("path.separator"), minecraftId, minecraftDirectory);
    }

    public String getName() {
        return _name;
    }

    public String getArchitecture() {
        return _architecture;
    }

    public String getPathSeparator() {
        return _variableDelimiter;
    }

    public String getVersion() {
        return _version;
    }

    public String getMinecraftId() {
        return _minecraftId;
    }

    public File getMinecraftDirectory() {
        return _minecraftDirectory;
    }

    private static File getMinecraftDirectoryWindows() {
        String appDataLocation = System.getenv("appdata");
        if (Objects.isNull(appDataLocation))
            return getMinecraftDirectoryUnix();

        return new File(appDataLocation, ".minecraft");
    }

    private static File getMinecraftDirectorySolaris() {
        String homeLocation = System.getProperty("user.home");
        return new File(homeLocation, ".minecraft");
    }

    private static File getMinecraftDirectoryUnix() {
        String homeLocation = System.getProperty("user.home");
        return new File(homeLocation, ".minecraft");
    }

    public static OperatingSystem getCurrent() {
        if (Objects.isNull(CURRENT)) {
            String name = System.getProperty("os.name");

            if (name.contains("win"))
                CURRENT = WINDOWS;

            else if (name.contains("sunos"))
                CURRENT = SOLARIS;

            else if (name.contains("mac"))
                CURRENT = MAC;

            else if (name.contains("nix") || name.contains("nux") || name.contains("aix"))
                CURRENT = UNIX;

            else
                throw new IllegalStateException("Could not find the current operating system.");
        }

        return CURRENT;
    }
}