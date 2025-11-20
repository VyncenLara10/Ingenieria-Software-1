package com.chat.hellbound.multiplayer;

import com.badlogic.gdx.Gdx;

import java.io.*;
import java.net.Socket;

public class ClientSession implements Runnable {

    private final String host;
    private final int port;
    private final String localName;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private volatile boolean running = false;
    private volatile PlayerSnapshot remoteSnapshot;

    private volatile String remoteName = "";
    private volatile int selectedLevel = -1;

    public ClientSession(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.localName = name;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(host, port);

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            running = true;

            // 1. Recibir nombre del host primero
            receiveHandshake();

            // 2. Mandar nuestro nombre
            sendHandshake();

            // 3. Loop de red
            while (running) {
                int type = in.readUnsignedByte();

                if (type == 1) {
                    PlayerSnapshot snap = PlayerSnapshot.readFrom(in);
                    remoteSnapshot = snap;
                }
                else if (type == 2) {
                    String msg = in.readUTF();
                    if (msg.startsWith("LEVEL:")) {
                        selectedLevel = Integer.parseInt(msg.substring(6));
                    }
                }
            }

        } catch (Exception e) {
            Gdx.app.log("NET", "Client error", e);
        } finally {
            stop();
        }
    }

    private void receiveHandshake() throws IOException {
        int t = in.readUnsignedByte();
        if (t == 10) {
            remoteName = in.readUTF();
        }
    }

    private void sendHandshake() throws IOException {
        out.writeByte(10);
        out.writeUTF(localName);
        out.flush();
    }

    public void sendSnapshot(PlayerSnapshot snap) {
        if (!running) return;
        try {
            out.writeByte(1);
            snap.writeTo(out);
            out.flush();
        } catch (Exception ignored) {}
    }

    public PlayerSnapshot getRemoteSnapshot() {
        return remoteSnapshot;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public int getSelectedLevel() {
        return selectedLevel;
    }

    public void stop() {
        running = false;
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}
