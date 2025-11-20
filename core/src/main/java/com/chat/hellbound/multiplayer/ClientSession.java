package com.chat.hellbound.multiplayer;

import com.badlogic.gdx.Gdx;
import com.chat.hellbound.entities.Player;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    private volatile int selectedLevel = 0;

    public ClientSession(String host, int port, String localName) {
        this.host = host;
        this.port = port;
        this.localName = localName != null ? localName : "Client";
    }

    @Override
    public void run() {
        try {
            Gdx.app.log("NET", "Client: conectando a " + host + ":" + port);
            socket = new Socket(host, port);
            Gdx.app.log("NET", "Client: conectado a " + socket.getRemoteSocketAddress());

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            out.writeUTF(localName);
            out.flush();
            remoteName = in.readUTF();
            Gdx.app.log("NET", "Client: nombre remoto = " + remoteName);

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
                } else if (type == 1) {
                    int lvl = in.readInt();
                    selectedLevel = lvl;
                    Gdx.app.log("NET", "Client: nivel seleccionado = " + lvl);
                }
            }

        } catch (IOException e) {
            Gdx.app.log("NET", "Client: error en red", e);
            running = false;
        } finally {
            close();
        }
    }

    public void stop() {
        running = false;
        close();
    }

    private void close() {
        Gdx.app.log("NET", "Client: cerrando sockets");
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
        try {
            if (out != null) out.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public PlayerSnapshot getRemoteSnapshot() {
        return remoteSnapshot;
    }

    public void sendLocalSnapshot(Player player) {
        DataOutputStream localOut = out;
        if (localOut == null) return;

        PlayerSnapshot snap = new PlayerSnapshot(player);
        try {
            synchronized (localOut) {
                localOut.writeByte(0);
                localOut.writeFloat(snap.x);
                localOut.writeFloat(snap.y);
                localOut.writeInt(snap.hp);
                localOut.flush();
            }
        } catch (IOException e) {
            Gdx.app.log("NET", "Client: error enviando snapshot", e);
            running = false;
            close();
        }
    }

    public String getRemoteName() {
        return remoteName;
    }

    public String getLocalName() {
        return localName;
    }

    public int getSelectedLevel() {
        return selectedLevel;
    }
}
