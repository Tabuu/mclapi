package nl.tabuu.mclapi.minecraft.packet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.tabuu.mclapi.minecraft.ServerInfo;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ServerPingPacket {

    private static final int PROTOCOL_VERSION = -1, TIME_OUT = 1_000, HANDSHAKE_ID = 1;

    private final Socket _socket;
    private final InetSocketAddress _address;

    private DataInputStream _dataInputStream;
    private DataOutputStream _dataOutputStream;

    public ServerPingPacket(String host, int port) {
        _address = new InetSocketAddress(host, port);
        _socket = new Socket();
    }

    private void connect() throws IOException {
        _socket.setSoTimeout(TIME_OUT);
        _socket.connect(_address, TIME_OUT);

        _dataOutputStream = new DataOutputStream(_socket.getOutputStream());
        _dataInputStream = new DataInputStream(_socket.getInputStream());
    }

    private void disconnect() throws IOException {
        _socket.close();
    }

    private String handshake() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(b);
        handshake.writeByte(0x00);

        writeVarInt(PROTOCOL_VERSION, handshake);
        writeVarInt(_address.getHostString().length(), handshake);
        handshake.writeBytes(_address.getHostString());
        handshake.writeShort(_address.getPort());
        writeVarInt(HANDSHAKE_ID, handshake);
        writeVarInt(b.size(), _dataOutputStream);
        _dataOutputStream.write(b.toByteArray());

        _dataOutputStream.writeByte(0x01);
        _dataOutputStream.writeByte(0x00);

        int size = readVarInt(_dataInputStream);
        int id = readVarInt(_dataInputStream);
        if (id == -1) throw new IOException("Premature end of stream");
        if (id != 0) throw new IOException("Invalid packet id");
        int length = readVarInt(_dataInputStream);
        if (length == -1) throw new IOException("Premature end of stream");
        if (length == 0) throw new IOException("Invalid string length");

        byte[] in = new byte[length];
        _dataInputStream.readFully(in);
        return new String(in);
    }

    public CompletableFuture<ServerInfo> ping() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connect();
                JsonObject json = JsonParser.parseString(handshake()).getAsJsonObject();
                disconnect();
                return fromJson(json);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            return null;
        });
    }

    private ServerInfo fromJson(JsonObject json) {
        String version = json.getAsJsonObject("version").get("name").getAsString();
        String description = json.getAsJsonObject("description").get("text").getAsString();
        String iconData;

        if(json.has("favicon"))
            iconData = json.get("favicon").getAsString().replaceFirst("data:image/png;base64,", "");
        else iconData = "";

        JsonObject players = json.getAsJsonObject("players");
        int maxPlayerCount = players.get("max").getAsInt();
        int playerCount = players.get("online").getAsInt();

        Map<String, String> profiles = new HashMap<>();
        if(players.has("sample")) {
            JsonArray playerSample = players.getAsJsonArray("sample");
            for (JsonElement playerElement : playerSample) {
                JsonObject player = playerElement.getAsJsonObject();
                String playerName = player.get("name").getAsString();
                String playerId = player.get("id").getAsString();
                profiles.put(playerId, playerName);
            }
        }

        return new ServerInfo(_address.getHostString(), description, version, iconData, _address.getPort(), maxPlayerCount, playerCount, profiles);
    }

    public static int readVarInt(InputStream in) throws IOException {
        int numRead = 0;
        int result = 0;
        int read;
        do {
            read = in.read();
            if (read < 0) {
                throw new RuntimeException("Unexpected end of stream");
            }
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writeVarInt(int value, OutputStream out) throws IOException {
        do {
            byte temp = (byte)(value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            out.write(temp);
        } while (value != 0);
    }
}
