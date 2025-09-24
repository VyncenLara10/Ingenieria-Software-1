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
                DatagramSocket socket = new DatagramSocket(5000); // servidor
                System.out.println("Servidor UDP escuchando en puerto 5000...");

                // =========================
                // Hilo de recepción de UDP
                // =========================
                new Thread(() -> {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    while (true) {
                        try {
                            socket.receive(packet);

                            ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
                            int[] recibidos = new int[packet.getLength() / 4];
                            for (int i = 0; i < recibidos.length; i++) {
                                recibidos[i] = bb.getInt();
                            }

                            // Actualiza estado del jugador remoto de forma thread-safe
                            synchronized(player2) {
                                player2.setLeft(recibidos[0] == 1);
                                player2.setUp(recibidos[1] == 1);
                                player2.setDown(recibidos[2] == 1);
                                player2.setRight(recibidos[3] == 1);
                            }

                            // Prepara array de respuesta con el estado del host
                            int[] send = new int[4];
                            send[0] = player.isLeft() ? 1 : 0;
                            send[1] = player.isUp() ? 1 : 0;
                            send[2] = player.isDown() ? 1 : 0;
                            send[3] = player.isRight() ? 1 : 0;

                            enviarArray(socket, packet.getAddress(), packet.getPort(), send);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                // =========================
                // Game Loop principal
                // =========================
                double TiempoPorFrame = 1000000000.0 / FPS_SET;
                double TiempoPorUpdate = 1000000000.0 / UPS_SET;
                long tiempoAnterior = System.nanoTime();
                int frames = 0, updates = 0;
                long ultimoCheck = System.currentTimeMillis();
                double deltaU = 0, deltaF = 0;

                while (true) {
                    long currentTime = System.nanoTime();
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
                }

            } catch (SocketException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            // =========================
            // Cliente
            // =========================
            try {
                DatagramSocket socket = new DatagramSocket(); // cliente usa puerto libre
                InetAddress serverAddr = InetAddress.getByName("localhost");
                int serverPort = 5000;

                // Hilo para recibir datos del servidor
                new Thread(() -> {
                    byte[] buffer_r = new byte[1024];
                    DatagramPacket response = new DatagramPacket(buffer_r, buffer_r.length);

                    while (true) {
                        try {
                            socket.receive(response);
                            ByteBuffer bbResp = ByteBuffer.wrap(response.getData(), 0, response.getLength());
                            int elementos = response.getLength() / 4;
                            int[] recibidos = new int[elementos];
                            for (int i = 0; i < elementos; i++) {
                                recibidos[i] = bbResp.getInt();
                            }

                            synchronized(player2) {
                                player2.setLeft(recibidos[0] == 1);
                                player2.setUp(recibidos[1] == 1);
                                player2.setDown(recibidos[2] == 1);
                                player2.setRight(recibidos[3] == 1);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                // =========================
                // Game Loop principal (envío)
                // =========================
                double TiempoPorFrame = 1000000000.0 / FPS_SET;
                double TiempoPorUpdate = 1000000000.0 / UPS_SET;
                long tiempoAnterior = System.nanoTime();
                int frames = 0, updates = 0;
                long ultimoCheck = System.currentTimeMillis();
                double deltaU = 0, deltaF = 0;

                int[] numeros = new int[4];

                while (true) {
                    long currentTime = System.nanoTime();
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

                    // Preparar array de envío
                    numeros[0] = player.isLeft() ? 1 : 0;
                    numeros[1] = player.isUp() ? 1 : 0;
                    numeros[2] = player.isDown() ? 1 : 0;
                    numeros[3] = player.isRight() ? 1 : 0;

                    // Enviar al servidor
                    enviarArray(socket, serverAddr, serverPort, numeros);

                    // Para no saturar la red, puedes dormir unos milisegundos
                    Thread.sleep(10);
                }

            } catch (Exception e) {
                e.printStackTrace();
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
