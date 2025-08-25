package main;

import entities.Player;
import gamestates.Gamestate;
import java.awt.Graphics;
import levels.LevelManager;
import multiplayer.*;
import java.io.IOException;


public class Juego implements Runnable{
    private VentanaJuego ventanajuego;
    private PanelJuego paneljuego;
    private Thread hiloJuego;
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;
    private Player player;
    private Player player2;
    private LevelManager levelManager;

    // multiplayer
    private boolean twoPlayers = true;   // para pruebas: true
    private boolean isServer = true;     // decide si actúa como servidor o cliente
    private NetworkManager network;

    public final static int TILES_DEFAULT_SIZE = 32;
    public final static float SCALE = 1.5f;
    public final static int TILES_IN_WIDTH = 26;
    public final static int TILES_IN_HEIGHT = 14;
    public final static int TILES_SIZE = (int)(TILES_DEFAULT_SIZE * SCALE);
    public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
    public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;

    public Juego(){
        initClasses();

        try {
            if (twoPlayers) {
                if (isServer) {
                    network = new GameServer(1234);
                } else {
                    network = new GameClient("localhost", 1234);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        if (twoPlayers){
            player2 = new Player(250,250,(int)(64 * SCALE), (int)(40 * SCALE));
            player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        }
    }

    private void iniciarGameLoop(){
        hiloJuego = new Thread(this);
        hiloJuego.start();
    }

    public void update(){
        switch(Gamestate.state){
            case MENU:
                break;
            case PLAYING:
                // actualizar jugador local
                player.update();

                // si hay multiplayer, mandar y recibir
                if (twoPlayers && network != null) {
                    // enviar posición local
                    network.send(new PlayerData(
                        (int) player.getHitbox().x,
                        (int) player.getHitbox().y
                    ));

                    // recibir posición remota
                    try {
                        PlayerData data = network.receive();
                        if (data != null) {
                            player2.getHitbox().x = data.x;
                            player2.getHitbox().y = data.y;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (twoPlayers) player2.update();
                levelManager.update();
                break;
            default:
                break;
        }
    }

    public void render(Graphics g){
        switch(Gamestate.state){
            case MENU:
                break;
            case PLAYING:
                levelManager.draw(g);
                player.render(g);
                if (twoPlayers) player2.render(g);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
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
                System.out.println("FPS: "+frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    public void windowFocusLost(){
        player.resetDirBooleans();
        if (twoPlayers) player2.resetDirBooleans();
    }

    public Player getPlayer(){
        return player;
    }

    public Player getPlayer2(){
        return player2;
    }
}
