package multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class servidor {

    public static void main(String[] args) {
        ServerSocket servidor = null;
        Socket sc = null;
        DataInputStream in;
        DataOutputStream out;

        final int PUERTO = 5000;
        try {
            servidor = new ServerSocket(PUERTO);
            System.out.println("Servidor Iniciado");

            while (true) {
                sc = servidor.accept();

                in = new DataInputStream(sc.getInputStream());
                out = new DataOutputStream(sc.getOutputStream());

                String mensaje = in.readUTF();
                System.out.println(mensaje);
                out.writeUTF("hoa mundo dedsde el servidor! ");

                sc.close();
                System.out.println("cliente desconetado");

            }

        } catch (IOException ex){
            Logger.getLogger(servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


}
