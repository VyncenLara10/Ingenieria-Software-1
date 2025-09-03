package main;


import java.io.IOException;
import java.net.*;

import entities.Player;
import gamestates.Gamestate;
import java.awt.Graphics;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import levels.LevelManager;


public class Juego implements Runnable{
    private VentanaJuego ventanajuego;
    private PanelJuego paneljuego;
    private Thread hiloJuego;
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;
    private Player player;
    private Player player2;
    public boolean host;

    private LevelManager levelManager;

    public final static int TILES_DEFAULT_SIZE = 32;
    public final static float SCALE = 1.5f;
    public final static int TILES_IN_WIDTH = 26;
    public final static int TILES_IN_HEIGHT = 14;
    public final static int TILES_SIZE = (int)(TILES_DEFAULT_SIZE * SCALE);
    public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
    public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;

    public Juego(boolean host){
        initClasses();
        this.host = host;
        paneljuego = new PanelJuego(this);
        ventanajuego = new VentanaJuego(paneljuego);
        paneljuego.setFocusable(true);
        paneljuego.requestFocus();
        System.out.println(host);
        iniciarGameLoop();
    }

    private void initClasses() {
        levelManager = new LevelManager(this);
        player = new Player(200,200,(int)(64 * SCALE), (int)(40 * SCALE));
        player.loadLvlData(levelManager.getCurrentLevel().getLevelData());

        player2 = new Player(220,200,(int)(64 * SCALE), (int)(40 * SCALE));
        player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
    }

    private void iniciarGameLoop(){
        hiloJuego = new Thread(this);
        hiloJuego.start();
    }

    public void update(){
        switch(Gamestate.state){
            case MENU:
                //menu.update()
                break;
            case PLAYING:
                player.update();
                player2.update();
                levelManager.update();
                break;
            default:
                break;
        }
    }

    public void render(Graphics g){
        switch(Gamestate.state){
            case MENU:
                //menu.update()
                break;
            case PLAYING:
                levelManager.draw(g);
                player.render(g);
                player2.render(g);
                break;
            default:
                break;
        }

    }


    @Override
    public void run() {
        if (this.host) {
            try {
                DatagramSocket socket = new DatagramSocket(5000); // servidor escucha en 5000
                System.out.println("Servidor UDP escuchando en puerto 5000...");

                // =========================
                // Hilo de recepción de UDP
                // =========================




                // =========================
                // Game Loop principal
                // =========================
                double TiempoPorFrame = 1000000000.0 / FPS_SET;
                double TiempoPorUpdate = 1000000000.0 / UPS_SET;
                long tiempoAnterior = System.nanoTime();

                int frames = 0;
                int updates = 0;
                long ultimoCheck = System.currentTimeMillis();

                double deltaU = 0;
                double deltaF = 0;

                byte[] buffer = new byte[1024];
                int[] send = {0, 0, 0, 0, 0};

                while (true) {
                    long currentTime = System.nanoTime();

                    System.out.println(player.isLeft());

                    deltaU += (currentTime - tiempoAnterior) / TiempoPorUpdate;
                    deltaF += (currentTime - tiempoAnterior) / TiempoPorFrame;
                    tiempoAnterior = currentTime;

                    if (deltaU >= 1) {
                        update();
                        updates++;
                        deltaU--;
                    }

                    if (deltaF >= 1) {
                        paneljuego.repaint();
                        frames++;
                        deltaF--;
                    }

                    if (System.currentTimeMillis() - ultimoCheck >= 1000) {
                        ultimoCheck = System.currentTimeMillis();
                        frames = 0;
                        updates = 0;
                    }

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);


                    // Reconstruir array de enteros desde bytes
                    ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
                    int[] recibidos = new int[packet.getLength() / 4]; // cada int = 4 bytes
                    for (int i = 0; i < recibidos.length; i++) {
                        recibidos[i] = bb.getInt();
                    }

                    if (player.isLeft()){
                        send[0] = 1;
                    } else {
                        send[0] = 0;
                    }

                    if (player.isUp()){
                        send[1] = 1;
                    } else {
                        send[1] = 0;
                    }


                    if (player.isDown()){
                        send[2] = 1;
                    } else {
                        send[2] = 0;
                    }

                    if (player.isRight()){
                        send[3] = 1;
                    } else {
                        send[3] = 0;
                    }

                    System.out.println("Array recibido: " + Arrays.toString(recibidos));
                    enviarArray(socket, packet.getAddress(), packet.getPort(), send);
                    System.out.println("Array enviado: " + Arrays.toString(send));


                    if (recibidos[0] == 1){
                        paneljuego.getJuego().getPlayer2().setLeft(true);
                    } else if (recibidos[0] == 0) {
                        paneljuego.getJuego().getPlayer2().setLeft(false);

                    }

                    if (recibidos[1] == 1){
                        paneljuego.getJuego().getPlayer2().setUp(true);

                    } else if (recibidos[1] == 0){
                        paneljuego.getJuego().getPlayer2().setUp(false);

                    }

                    if (recibidos[2] == 1){
                        paneljuego.getJuego().getPlayer2().setDown(true);

                    } else if (recibidos[2] == 0){
                        paneljuego.getJuego().getPlayer2().setDown(false);

                    }


                    if (recibidos[3] == 1){
                        paneljuego.getJuego().getPlayer2().setRight(true);

                    } else if (recibidos[3] == 0) {
                        paneljuego.getJuego().getPlayer2().setRight(false);

                    }

                }

            } catch (SocketException ex) {
                throw new RuntimeException(ex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                DatagramSocket socket = new DatagramSocket(); // cliente usa un puerto libre
                InetAddress serverAddr = InetAddress.getByName("localhost"); // IP del servidor
                int serverPort = 5000; // puerto donde escucha el servidor

                // Array de enteros a enviar
                int[] numeros = {0, 0, 0, 0, 0};
                ByteBuffer bb = ByteBuffer.allocate(4 * numeros.length);
                for (int num : numeros) {
                    bb.putInt(num);
                }
                byte[] data = bb.array();

                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, serverPort);



                double TiempoPorFrame = 1000000000.0 / FPS_SET;
                double TiempoPorUpdate = 1000000000.0 / UPS_SET;
                long tiempoAnterior = System.nanoTime();

                int frames = 0;
                int updates = 0;
                long ultimoCheck = System.currentTimeMillis();

                double deltaU = 0;
                double deltaF = 0;



                while (true) {
                    long currentTime = System.nanoTime();

                    System.out.println(player.isLeft());

                    deltaU += (currentTime - tiempoAnterior) / TiempoPorUpdate;
                    deltaF += (currentTime - tiempoAnterior) / TiempoPorFrame;
                    tiempoAnterior = currentTime;

                    if (deltaU >= 1) {
                        update();
                        updates++;
                        deltaU--;
                    }

                    if (deltaF >= 1) {
                        paneljuego.repaint();
                        frames++;
                        deltaF--;
                    }

                    if (System.currentTimeMillis() - ultimoCheck >= 1000) {
                        ultimoCheck = System.currentTimeMillis();
                        frames = 0;
                        updates = 0;
                    }




                    if (player.isLeft()){
                        numeros[0] = 1;
                    } else {
                        numeros[0] = 0;
                    }

                    if (player.isUp()){
                        numeros[1] = 1;
                    } else {
                        numeros[1] = 0;
                    }


                    if (player.isDown()){
                        numeros[2] = 1;
                    } else {
                        numeros[2] = 0;
                    }

                    if (player.isRight()){
                        numeros[3] = 1;
                    } else {
                        numeros[3] = 0;
                    }


                    // ---- Enviar ----
                    enviarArray(socket, packet.getAddress(), packet.getPort(), numeros);
                    System.out.println("Array enviado al servidor: " + Arrays.toString(numeros));

                    // ---- Recibir ----
                    byte[] buffer_r = new byte[1024]; // buffer de recepción
                    DatagramPacket response = new DatagramPacket(buffer_r, buffer_r.length);
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

                    if (recibidos[0] == 1){
                        paneljuego.getJuego().getPlayer2().setLeft(true);
                    } else if (recibidos[0] == 0) {
                        paneljuego.getJuego().getPlayer2().setLeft(false);

                    }

                    if (recibidos[1] == 1){
                        paneljuego.getJuego().getPlayer2().setUp(true);

                    } else if (recibidos[1] == 0){
                        paneljuego.getJuego().getPlayer2().setUp(false);

                    }

                    if (recibidos[2] == 1){
                        paneljuego.getJuego().getPlayer2().setDown(true);

                    } else if (recibidos[2] == 0){
                        paneljuego.getJuego().getPlayer2().setDown(false);

                    }


                    if (recibidos[3] == 1){
                        paneljuego.getJuego().getPlayer2().setRight(true);

                    } else if (recibidos[3] == 0) {
                        paneljuego.getJuego().getPlayer2().setRight(false);

                    }

                }

            } catch (SocketException ex) {
                throw new RuntimeException(ex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void enviarArray(DatagramSocket socket, InetAddress ip, int port, int[] datos) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(datos.length * 4); // 4 bytes por int
        for (int valor : datos) {
            bb.putInt(valor);
        }
        byte[] data = bb.array();

        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
        socket.send(packet);
    }



    public void windowFocusLost(){
        player.resetDirBooleans();
    }

    public Player getPlayer(){
        return player;
    }

    public Player getPlayer2(){
        return player2;
    }
}
