package multiplayer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class GameServer {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(5000); // servidor escucha en 5000
        System.out.println("Servidor UDP escuchando en puerto 5000...");

        byte[] buffer = new byte[1024];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            // Reconstruir array de enteros desde bytes
            ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
            int[] recibidos = new int[packet.getLength() / 4]; // cada int = 4 bytes
            for (int i = 0; i < recibidos.length; i++) {
                recibidos[i] = bb.getInt();
            }

            System.out.println("Array recibido: " + Arrays.toString(recibidos));
        }
    }
}
