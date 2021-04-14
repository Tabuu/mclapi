package nl.tabuu.mclapi.util.os;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public enum OperatingSystem {
    WINDOWS("Windows", "windows", "win") {
        @Override
        protected File findMinecraftDirectory() {
            String appDataLocation = System.getenv("appdata");
            if (Objects.isNull(appDataLocation))
                return super.findMinecraftDirectory();

            return new File(appDataLocation, ".minecraft");
        }
    },
    SOLARIS("Solaris/Sun OS", "solaris", "sunos") {
        @Override
        protected File findMinecraftDirectory() {
            String homeLocation = System.getProperty("user.home");
            return new File(homeLocation, "Library/Application Support/minecraft");
        }
    },
    UNIX("Linux/Unix", "linux", "nix", "nux", "aix"),
    MAC("OSX", "osx", "mac");

    private static OperatingSystem CURRENT;

    private final String _name, _minecraftId;
    private final String[] _identifiers;
    private File _minecraftDirectory;

    OperatingSystem(String name, String minecraftId, String... identifiers) {
        _name = name;
        _minecraftId = minecraftId;
        _identifiers = identifiers;
    }

    public boolean isCurrent() {
        return Arrays.stream(_identifiers).anyMatch(id -> System.getProperty("os.name").contains(id));
    }

    public String getDisplayName() {
        return _name;
    }

    public String getMinecraftId() {
        return _minecraftId;
    }

    public File getMinecraftDirectory() {
        if (Objects.isNull(_minecraftDirectory))
            _minecraftDirectory = findMinecraftDirectory();
        return _minecraftDirectory;
    }

    protected File findMinecraftDirectory() {
        String homeLocation = System.getProperty("user.home");
        return new File(homeLocation, ".minecraft");
    }

    public static OperatingSystem getCurrent() {
        if(Objects.isNull(CURRENT)) {
            for (OperatingSystem os : values()) {
                if(!os.isCurrent()) continue;
                CURRENT = os;
                return getCurrent();
            }

            throw new IllegalStateException("The current OS is not supported.");
        }

        return CURRENT;
    }
}