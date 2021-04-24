package nl.tabuu.mclapi.minecraft;

import nl.tabuu.mclapi.profile.IMinecraftProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerInfo {
    private final String _host, _description, _version, _iconData;
    private final int _port, _maxPlayerCount, _playerCount;
    private final Map<String, String> _onlinePlayerSampleList;

    public ServerInfo(String host, String description, String version, String iconData, int port, int maxPlayerCount, int playerCount, Map<String, String> onlinePlayerSampleList) {
        _host = host;
        _description = description;
        _version = version;
        _iconData = iconData;
        _port = port;
        _maxPlayerCount = maxPlayerCount;
        _playerCount = playerCount;
        _onlinePlayerSampleList = new HashMap<>(onlinePlayerSampleList);
    }

    public String getHost() {
        return _host;
    }

    public String getDescription() {
        return _description;
    }

    public String getVersion() {
        return _version;
    }

    public String getIconData() {
        return _iconData;
    }

    public int getPort() {
        return _port;
    }

    public int getMaxPlayerCount() {
        return _maxPlayerCount;
    }

    public int getPlayerCount() {
        return _playerCount;
    }

    public List<IMinecraftProfile> getOnlinePlayerSampleList() {
        return null;
    }
}