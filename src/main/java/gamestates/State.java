package gamestates;

import java.awt.event.MouseEvent;

import main.Juego;
import ui.MenuButton;

public class State {

	protected Juego juego;

	public State(Juego juego) {
		this.juego = juego;
	}
	
	public boolean isIn(MouseEvent e, MenuButton mb) {
		return mb.getBounds().contains(e.getX(), e.getY());
	}
	

	public Juego getGame() {
		return juego;
	}
}