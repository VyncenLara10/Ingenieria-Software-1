package com.chat.hellbound.multiplayer;

import com.badlogic.gdx.Gdx;
import com.chat.hellbound.entities.Player;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public HostSession(int port, String localName) {
        this.port = port;
        this.localName = localName != null ? localName : "Host";
    }

    @Override
    public void run() {
        try {
            Gdx.app.log("NET", "Host: creando ServerSocket en puerto " + port);
            serverSocket = new ServerSocket(port);
            Gdx.app.log("NET", "Host: esperando cliente...");
            socket = serverSocket.accept();
            Gdx.app.log("NET", "Host: cliente conectado desde " + socket.getInetAddress());

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            out.writeUTF(localName);
            out.flush();
            remoteName = in.readUTF();
            Gdx.app.log("NET", "Host: nombre remoto = " + remoteName);

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
                } else {
                }
            }

        } catch (IOException e) {
            Gdx.app.log("NET", "Host: error en red", e);
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
        Gdx.app.log("NET", "Host: cerrando sockets");
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
        try {
            if (out != null) out.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        try {
            if (serverSocket != null) serverSocket.close();
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
            Gdx.app.log("NET", "Host: error enviando snapshot", e);
            running = false;
            close();
        }
    }

    public void sendLevelSelection(int level) {
        DataOutputStream localOut = out;
        if (localOut == null) return;
        try {
            synchronized (localOut) {
                localOut.writeByte(1);
                localOut.writeInt(level);
                localOut.flush();
            }
        } catch (IOException e) {
            Gdx.app.log("NET", "Host: error enviando nivel", e);
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
}
