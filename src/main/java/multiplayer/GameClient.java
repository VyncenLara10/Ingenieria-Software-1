/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package multiplayer;
import java.io.*;
import java.net.*;
/**
 *
 * @author USUARIO
 */
public class GameClient implements NetworkManager{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public GameClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        System.out.println("Conectado al servidor!");

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void send(PlayerData data) {
        if (out != null) out.println(data.toJSON());
    }

    @Override
    public PlayerData receive() throws IOException {
        if (in != null) {
            String line = in.readLine();
            if (line != null) return PlayerData.fromJSON(line);
        }
        return null;
    }

    @Override
    public void stop() throws IOException {
        socket.close();
    }
}
