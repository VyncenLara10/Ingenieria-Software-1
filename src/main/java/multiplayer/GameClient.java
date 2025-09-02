package multiplayer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class GameClient {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(); // cliente usa un puerto libre
        InetAddress serverAddr = InetAddress.getByName("localhost"); // IP del servidor
        int serverPort = 5000; // puerto donde escucha el servidor

        // Array de enteros a enviar
        int[] numeros = {10, 20, 30, 40, 50};
        ByteBuffer bb = ByteBuffer.allocate(4 * numeros.length);
        for (int num : numeros) {
            bb.putInt(num);
        }
        byte[] data = bb.array();

        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, serverPort);

        // Bucle infinito de envío/recepción
        while (true) {
            // ---- Enviar ----
            socket.send(packet);
            System.out.println("Array enviado al servidor.");

            // ---- Recibir ----
            byte[] buffer = new byte[1024]; // buffer de recepción
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response); // se queda bloqueado hasta que recibe

            // ---- Reconstruir array ----
            ByteBuffer bbResp = ByteBuffer.wrap(response.getData(), 0, response.getLength());
            int elementos = response.getLength() / 4; // cada int son 4 bytes
            int[] recibidos = new int[elementos];
            for (int i = 0; i < elementos; i++) {
                recibidos[i] = bbResp.getInt();
            }

            // ---- Imprimir ----
            System.out.print("Array recibido: ");
            for (int n : recibidos) {
                System.out.print(n + " ");
            }
            System.out.println();

            // Un pequeño delay para no saturar
        }
    }
}
