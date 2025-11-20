package com.chat.hellbound.multiplayer;

import com.badlogic.gdx.Gdx;
import com.chat.hellbound.entities.Player;

import java.io.*;
import java.net.Socket;

public class ClientSession implements Runnable {

    private final String host;
    private final int port;
    private final String localName;
    private volatile boolean running = true;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private volatile PlayerSnapshot remoteSnapshot;
    private volatile String remoteName;
    private volatile boolean mapApplied = false;

    private volatile int selectedLevel = 0;
    private volatile int[][] receivedMap = null;
    private volatile boolean mapReady = false;

    public ClientSession(String host, int port, String localName) {
        this.host = host;
        this.port = port;
        this.localName = localName;
    }

    public boolean hasMapReady() { return mapReady; }
    public int[][] getReceivedMap() { return receivedMap; }
    public int getSelectedLevel() { return selectedLevel; }

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
            socket = new Socket(host, port);

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // Handshake
            out.writeUTF(localName);
            out.flush();
            remoteName = in.readUTF();

            Gdx.app.log("NET", "Cliente conectado. Host: " + remoteName);

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

                else if (type == 1) {
                    selectedLevel = in.readInt();
                }

                else if (type == 2) {
                    int h = in.readInt();
                    int w = in.readInt();

                    int[][] map = new int[h][w];

                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            map[y][x] = in.readInt();
                        }
                    }

                    receivedMap = map;
                    mapReady = true;

                    Gdx.app.log("NET", "Mapa recibido por cliente (" + w + "x" + h + ")");
                }
            }

        } catch (Exception e) {
            Gdx.app.log("NET", "Client error", e);
        } finally {
            close();
        }
    }

    public void sendLocalSnapshot(Player p) {
        if (out == null) return;

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
            Gdx.app.log("NET", "Client snapshot error", e);
        }
    }

    private void close() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }

    public PlayerSnapshot getRemoteSnapshot() { return remoteSnapshot; }
    public String getRemoteName() { return remoteName; }
    public boolean isMapApplied() { return mapApplied; }
    public void setMapApplied() { this.mapApplied = true; }
}
