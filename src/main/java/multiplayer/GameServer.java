package multiplayer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;


public class GameServer {
    public static void main(String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(5000);
        System.out.println("Servidor UDP en puerto 5000...");

        byte[] buffer = new byte[1024];
        List<DatagramPacket> clients = new ArrayList<>();

        while (true) {
            // Recibe mensaje de un cliente
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Recibido: " + msg);

            // Guarda cliente si es nuevo
            boolean nuevo = true;
            for (DatagramPacket c : clients) {
                if (c.getAddress().equals(packet.getAddress()) && c.getPort() == packet.getPort()) {
                    nuevo = false;
                    break;
                }
            }
            if (nuevo) {
                clients.add(packet);
                System.out.println("Nuevo cliente conectado: " + packet.getAddress() + ":" + packet.getPort());
            }

            // Reenvía mensaje a todos los clientes
            for (DatagramPacket c : clients) {
                DatagramPacket forward = new DatagramPacket(
                        msg.getBytes(), msg.length(),
                        c.getAddress(), c.getPort()
                );
                serverSocket.send(forward);
            }
        }
    }
}
