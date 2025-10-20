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
public class GameServer implements NetworkManager{
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public GameServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Servidor esperando conexión...");
        socket = serverSocket.accept();
        System.out.println("Cliente conectado!");

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
        serverSocket.close();
    }
}
