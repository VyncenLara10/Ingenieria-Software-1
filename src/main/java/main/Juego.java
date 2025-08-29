package main;


import java.io.IOException;
import java.net.*;

import entities.Player;
import gamestates.Gamestate;
import java.awt.Graphics;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
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

    private LevelManager levelManager;
    
    public final static int TILES_DEFAULT_SIZE = 32;
    public final static float SCALE = 1.5f;
    public final static int TILES_IN_WIDTH = 26;
    public final static int TILES_IN_HEIGHT = 14;
    public final static int TILES_SIZE = (int)(TILES_DEFAULT_SIZE * SCALE);
    public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
    public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;
    
    public Juego(){
        initClasses();
        
        paneljuego = new PanelJuego(this);
        ventanajuego = new VentanaJuego(paneljuego);
        paneljuego.setFocusable(true);
        paneljuego.requestFocus();
        
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
        // se hace la conexión
        try {
            DatagramSocket serverSocket = new DatagramSocket(5000);
            System.out.println("Servidor UDP en puerto 5000...");

            byte[] buffer = new byte[1024];
            byte[] buffer2 = new byte[1];

            List<DatagramPacket> clients = new ArrayList<>();
            double TiempoPorFrame = 1000000000.0 / FPS_SET;
            double TiempoPorUpdate = 1000000000.0 / UPS_SET;
            long tiempoAnterior = System.nanoTime();

            int frames = 0;
            int updates = 0;
            long ultimoCheck = System.currentTimeMillis();

            double deltaU = 0;
            double deltaF = 0;

            while(true){
                long currentTime = System.nanoTime();

                deltaU += (currentTime - tiempoAnterior) / TiempoPorUpdate;
                deltaF += (currentTime - tiempoAnterior) / TiempoPorFrame;
                tiempoAnterior = currentTime;

                if(deltaU >= 1){
                    update();
                    updates++;
                    deltaU--;
                }

                if(deltaF >= 1){
                    paneljuego.repaint();
                    frames ++;
                    deltaF--;
                }


                if(System.currentTimeMillis() - ultimoCheck >= 1000){
                    ultimoCheck = System.currentTimeMillis();
                    frames = 0;
                    updates = 0;
                }
                // conexion de menseajes aqui

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

                for (DatagramPacket c : clients) {
                    DatagramPacket forward = new DatagramPacket(
                            msg.getBytes(), msg.length(),
                            c.getAddress(), c.getPort()
                    );
                    serverSocket.send(forward);
                }

                if (msg.equals("true")) {
                    paneljuego.getJuego().getPlayer2().setRight(true);

                }




            }


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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
