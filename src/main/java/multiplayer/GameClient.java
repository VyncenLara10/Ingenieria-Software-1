package multiplayer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GameClient {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverAddr = InetAddress.getByName("localhost"); // o IP del servidor
        int serverPort = 5000;

        // Hilo para recibir mensajes del servidor
        Thread listener = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Update recibido: " + msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        listener.start();

        // Simula enviar     posición cada 500ms
        float x = 0;
        while (true) {
            String mensaje = Boolean.toString(true);  // convertir float a string
            byte[] data = mensaje.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, serverPort);
            socket.send(packet);

            x += 0.5; // mover jugador
        }
    }
}
