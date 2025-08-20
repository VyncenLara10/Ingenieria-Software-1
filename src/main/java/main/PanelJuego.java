package main;


import inputs.inputTeclado;
import inputs.inputsMouse;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import static utilz.Constantes.ConstantesJugador.*;
import static utilz.Constantes.Direcciones.*;
import static main.Juego.GAME_HEIGHT;
import static main.Juego.GAME_WIDTH;

public class PanelJuego extends JPanel{
    
    private inputsMouse inputsmouse;
    private inputTeclado inputsteclado;
    private Juego juego;

    public PanelJuego(Juego juego){
    inputsmouse = new inputsMouse(this);
    inputsteclado = new inputTeclado(this);
    this.juego = juego;
    
    setPanelSize();
    addKeyListener(inputsteclado);
    addMouseListener(inputsmouse);
    addMouseMotionListener(inputsmouse);
    }
    
    
    private void setPanelSize() {
        Dimension size = new Dimension(GAME_WIDTH,GAME_HEIGHT);
        setPreferredSize(size);
        System.out.println("size : "+GAME_WIDTH+" : "+ GAME_HEIGHT);
    }
    
    
    
    public void updateGame(){
       
    }
    
    public Juego getJuego(){
        return juego;
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        juego.render(g);
    }
    
    
  
}
