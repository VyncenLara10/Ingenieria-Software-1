package com.chat.hellbound.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.chat.hellbound.gamestates.Gamestate;

public class Main extends Game {

    private Gamestate currentState = Gamestate.MENU;
    private boolean showIntroVideo = false; // Cambiar a false para saltar el video

    @Override
    public void create() {
        // Si showIntroVideo es true, mostrar video; sino ir directo al menú

        setState(Gamestate.MENU);
    }

    public void setState(Gamestate state) {
        this.currentState = state;
        switch (state) {
            case MENU:
                setScreen(new MenuScreen(this));
                break;
            case PLAYING:
                setScreen(new GameScreen(this, 1));
                break;
            case MULTIPLAYER:
            case OPTIONS:
                setScreen(new MenuScreen(this));
                break;
            case EXIT:
                Gdx.app.exit();
                break;
        }
    }

    public Gamestate getState() {
        return currentState;
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        super.dispose();
    }
}
