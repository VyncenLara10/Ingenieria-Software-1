package com.chat.hellbound.multiplayer;

import com.badlogic.gdx.Gdx;
import com.chat.hellbound.entities.Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HostSession implements Runnable {

    private final int port;
    private final String localName;
    private volatile boolean running = true;

    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private volatile PlayerSnapshot remoteSnapshot;
    private volatile String remoteName;

    // 🔥 Mapa que GameScreen enviará al cliente
    private volatile int[][] levelDataToSend = null;

    public HostSession(int port, String localName) {
        this.port = port;
        this.localName = localName;
    }

    // GameScreen HOST llama esto cuando ya generó el mapa
    public void setLevelData(int[][] map) {
        this.levelDataToSend = map;
        sendFullMap(map);
    }

    public void stop() {
        running = false;
        close();
    }
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            Gdx.app.log("NET", "Host esperando cliente...");

            socket = serverSocket.accept();
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // Handshake
            out.writeUTF(localName);
            out.flush();
            remoteName = in.readUTF();

            // Si el host ya tenía mapa listo → enviarlo
            if (levelDataToSend != null)
                sendFullMap(levelDataToSend);

            while (running) {
                byte type = in.readByte();

                if (type == 0) {
                    float x = in.readFloat();
                    float y = in.readFloat();
                    int hp = in.readInt();

                    PlayerSnapshot snap = new PlayerSnapshot();
                    snap.x = x;
                    snap.y = y;
                    snap.hp = hp;
                    remoteSnapshot = snap;
                }
            }

        } catch (Exception e) {
            Gdx.app.log("NET", "Host error", e);
        } finally {
            close();
        }
    }

    public void sendLocalSnapshot(Player p) {
        if (!isConnected() || out == null) return;

        PlayerSnapshot s = new PlayerSnapshot(p);

        try {
            synchronized (out) {
                out.writeByte(0);
                out.writeFloat(s.x);
                out.writeFloat(s.y);
                out.writeInt(s.hp);
                out.flush();
            }
        } catch (Exception e) {
            Gdx.app.log("NET", "Host error snapshot", e);
        }
    }


    public void sendLevelSelection(int level) {
        if (out == null) return;

        try {
            synchronized (out) {
                out.writeByte(1);
                out.writeInt(level);
                out.flush();
            }
        } catch (Exception e) {
            Gdx.app.log("NET", "Host error level send", e);
        }
    }

    // Enviar mapa completo
    public void sendFullMap(int[][] map) {
        if (out == null || map == null) return;

        try {
            synchronized (out) {
                out.writeByte(2);

                int h = map.length;
                int w = map[0].length;

                out.writeInt(h);
                out.writeInt(w);

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        out.writeInt(map[y][x]);
                    }
                }

                out.flush();
                Gdx.app.log("NET", "Mapa enviado (" + w + "x" + h + ")");
            }

        } catch (Exception e) {
            Gdx.app.log("NET", "Host error sending map", e);
        }
    }

    private void close() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
    }

    public PlayerSnapshot getRemoteSnapshot() { return remoteSnapshot; }
    public String getRemoteName() { return remoteName; }
}
