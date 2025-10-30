
package inputs;

import gamestates.Gamestate;
import static gamestates.Gamestate.MENU;
import static gamestates.Gamestate.PLAYING;
import main.PanelJuego;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import static utilz.Constantes.Direcciones.*;

public class inputTeclado implements KeyListener{
    private PanelJuego paneljuego;
    
    public inputTeclado(PanelJuego paneljuego){
    this.paneljuego = paneljuego;
    }
        @Override
        public void keyTyped(KeyEvent e){
        }
        @Override
        public void keyReleased(KeyEvent e){
            switch(Gamestate.state){
            case MENU:
                paneljuego.getJuego().getMenu().keyReleased(e);
                break;
            case PLAYING:
                paneljuego.getJuego().getPlaying().keyReleased(e);
                break;
            default:
                break;
            }
        }
        
        @Override
        public void keyPressed(KeyEvent e){
            switch(Gamestate.state){
            case MENU:
                paneljuego.getJuego().getMenu().keyPressed(e);
                break;
            case PLAYING:
                paneljuego.getJuego().getPlaying().keyPressed(e);
                break;
            default:
                break;
        }
            
        }
} 
