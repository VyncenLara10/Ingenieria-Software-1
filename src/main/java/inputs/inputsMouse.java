
package inputs;

import gamestates.Gamestate;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import main.PanelJuego;

public class inputsMouse implements MouseListener,MouseMotionListener{
    
    private PanelJuego paneljuego;
    public inputsMouse(PanelJuego paneljuego){
    this.paneljuego = paneljuego;
    }

    @Override
	public void mouseDragged(MouseEvent e) {
		switch (Gamestate.state) {
		case PLAYING:
			paneljuego.getJuego().getPlaying().mouseDragged(e);
			break;
		default:
			break;

		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		switch (Gamestate.state) {
		case MENU:
			paneljuego.getJuego().getMenu().mouseMoved(e);
			break;
		case PLAYING:
			paneljuego.getJuego().getPlaying().mouseMoved(e);
			break;
		default:
			break;

		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		switch (Gamestate.state) {
		case PLAYING:
			paneljuego.getJuego().getPlaying().mouseClicked(e);
			break;
		default:
			break;

		}

	}

	@Override
	public void mousePressed(MouseEvent e) {
		switch (Gamestate.state) {
		case MENU:
			paneljuego.getJuego().getMenu().mousePressed(e);
			break;
		case PLAYING:
			paneljuego.getJuego().getPlaying().mousePressed(e);
			break;
		default:
			break;

		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		switch (Gamestate.state) {
		case MENU:
			paneljuego.getJuego().getMenu().mouseReleased(e);
			break;
		case PLAYING:
			paneljuego.getJuego().getPlaying().mouseReleased(e);
			break;
		default:
			break;

		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

}