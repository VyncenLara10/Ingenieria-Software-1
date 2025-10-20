package main;

import gamestates.Gamestate;
import gamestates.Menu;
import gamestates.Playing;
import java.awt.Graphics;


public class Juego implements Runnable{
    private VentanaJuego ventanajuego;
    private PanelJuego paneljuego;
    private Thread hiloJuego;
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;
    
    private Playing playing;
    private Menu menu;
    
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
        menu = new Menu(this);
        playing = new Playing(this);

    }
    
    private void iniciarGameLoop(){
        hiloJuego = new Thread(this);
        hiloJuego.start();
    }
    
    public void update(){
        switch(Gamestate.state){
            case MENU:
                menu.update();
                break;
            case PLAYING:
                playing.update();
                break;
            case OPTIONS:
            case QUIT:
            default:
                System.exit(0);
                break;
        }
    }
    
    public void render(Graphics g){
        switch(Gamestate.state){
            case MENU:
                menu.draw(g);
                break;
            case PLAYING:
                playing.draw(g);
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
        if(Gamestate.state == Gamestate.PLAYING){
            playing.getPlayer().resetDirBooleans();
        }
    }
    
    public Menu getMenu(){
        return menu;
    }
    
    public Playing getPlaying(){
        return playing;
    }
    
}
