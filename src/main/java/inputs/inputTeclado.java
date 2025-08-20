
package inputs;

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
            switch(e.getKeyCode()){
                case KeyEvent.VK_W:
                    paneljuego.getJuego().getPlayer().setUp(false);
                    break;
                case KeyEvent.VK_A:
                    paneljuego.getJuego().getPlayer().setLeft(false);
                    break;
                case KeyEvent.VK_S:
                    paneljuego.getJuego().getPlayer().setDown(false);
                    break;
                case KeyEvent.VK_D:
                    paneljuego.getJuego().getPlayer().setRight(false);
                    break;
                case KeyEvent.VK_SPACE:
                    paneljuego.getJuego().getPlayer().setJump(false);
                    break;
            }
        }
        @Override
        public void keyPressed(KeyEvent e){
            switch(e.getKeyCode()){
                case KeyEvent.VK_W:
                    paneljuego.getJuego().getPlayer().setUp(true);
                    break;
                case KeyEvent.VK_A:
                    paneljuego.getJuego().getPlayer().setLeft(true);
                    break;
                case KeyEvent.VK_S:
                    paneljuego.getJuego().getPlayer().setDown(true);
                    break;
                case KeyEvent.VK_D:
                    paneljuego.getJuego().getPlayer().setRight(true);
                    break;
                case KeyEvent.VK_SPACE:
                    paneljuego.getJuego().getPlayer().setJump(true);
                    break;
            }
        }
} 
