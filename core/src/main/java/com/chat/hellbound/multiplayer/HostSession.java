package com.chat.hellbound.multiplayer;

import com.badlogic.gdx.Gdx;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HostSession implements Runnable {

    private final int port;
    private final String localName;

    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private volatile boolean running = false;
    private volatile PlayerSnapshot remoteSnapshot;

    private volatile String remoteName = "";
    private volatile int selectedLevel = -1; // 🔥 YA NO TIENE NIVEL POR DEFECTO

    public HostSession(int port, String localName) {
        this.port = port;
        this.localName = localName;
    }

    @Override
    public void run() {
        try {
            Gdx.app.log("NET", "Host esperando conexión en puerto " + port);
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();

            Gdx.app.log("NET", "Host: cliente conectado");

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            running = true;

            // 1. Mandar nuestro nombre (sin nivel)
            sendHandshake();

            // 2. Recibir nombre del cliente
            receiveHandshake();

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
                        // el host NO recibe nivel, así que ignoramos
                    }
                }
            }

        } catch (Exception e) {
            Gdx.app.log("NET", "HostSession error", e);
        } finally {
            stop();
        }
    }

    private void sendHandshake() throws IOException {
        out.writeByte(10);
        out.writeUTF(localName);
        out.flush();
    }

    private void receiveHandshake() throws IOException {
        int t = in.readUnsignedByte();
        if (t == 10) {
            remoteName = in.readUTF();
        }
    }

    public void sendSnapshot(PlayerSnapshot snap) {
        if (!running) return;
        try {
            out.writeByte(1);
            snap.writeTo(out);
            out.flush();
        } catch (Exception e) {
            running = false;
        }
    }

    public PlayerSnapshot getRemoteSnapshot() {
        return remoteSnapshot;
    }

    // 🔥 Ahora sí: solo manda el nivel cuando el host realmente lo elige
    public void setSelectedLevel(int lvl) {
        selectedLevel = lvl;
        try {
            out.writeByte(2);
            out.writeUTF("LEVEL:" + lvl);
            out.flush();
        } catch (Exception ignored) {}
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
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
    }
}
