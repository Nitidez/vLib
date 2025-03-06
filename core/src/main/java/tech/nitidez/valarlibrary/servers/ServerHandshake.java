package tech.nitidez.valarlibrary.servers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerHandshake {
    
    public static ServerInfo getServerInfo(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(3000);
            socket.connect(new InetSocketAddress(ip, port), 3000);
            
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            
            ByteArrayOutputStream handshake = new ByteArrayOutputStream();
            DataOutputStream handshakeStream = new DataOutputStream(handshake);
            handshakeStream.writeByte(0x00);
            writeVarInt(handshakeStream, 47);
            writeString(handshakeStream, ip);
            handshakeStream.writeShort(port);
            writeVarInt(handshakeStream, 1);
            
            writeVarInt(dataOutputStream, handshake.size());
            dataOutputStream.write(handshake.toByteArray());
            
            dataOutputStream.writeByte(0x01);
            dataOutputStream.writeByte(0x00);
            
            readVarInt(dataInputStream);
            int packetId = readVarInt(dataInputStream);
            if (packetId != 0x00) {
                throw new IOException("Resposta inesperada do servidor");
            }
            
            int length = readVarInt(dataInputStream);
            byte[] responseData = new byte[length];
            dataInputStream.readFully(responseData);
            
            String jsonString = new String(responseData, StandardCharsets.UTF_8);
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonString).getAsJsonObject();
            
            String motd = json.get("description").getAsString();
            String version = json.getAsJsonObject("version").get("name").getAsString();
            int onlinePlayers = json.getAsJsonObject("players").get("online").getAsInt();
            int maxPlayers = json.getAsJsonObject("players").get("max").getAsInt();
            
            return new ServerInfo(ip, port, true, onlinePlayers, maxPlayers, version, motd);
            
        } catch (Exception e) {
            return new ServerInfo(ip, port, false, 0, 0, "", "");
        }
    }
    
    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;
        do {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << position;
            position += 7;
            if (position >= 32) throw new IOException("VarInt muito grande");
        } while ((currentByte & 0x80) == 0x80);
        return value;
    }

    private static void writeString(DataOutputStream out, String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static class ServerInfo {
        public String ip;
        public int port;
        public boolean online;
        public int onlinePlayers;
        public int maxPlayers;
        public String version;
        public String motd;

        public ServerInfo(String ip, int port, boolean online, int onlinePlayers, int maxPlayers, String version, String motd) {
            this.ip = ip;
            this.port = port;
            this.online = online;
            this.onlinePlayers = onlinePlayers;
            this.maxPlayers = maxPlayers;
            this.version = version;
            this.motd = motd;
        }

        @Override
        public String toString() {
            return "IP: " + ip + ":" + port + "\nOnline: " + online + "\nJogadores: " + onlinePlayers + "/" + maxPlayers + "\nVersão: " + version + "\nMOTD: " + motd;
        }
    }
}