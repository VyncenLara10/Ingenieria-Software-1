package main;

import entities.Player;
import gamestates.Gamestate;
import java.awt.Graphics;
import levels.LevelManager;


public class Juego implements Runnable{
    private VentanaJuego ventanajuego;
    private PanelJuego paneljuego;
    private Thread hiloJuego;
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;
    private Player player;
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
    }
    
    public Player getPlayer(){
        return player;
    }
}
