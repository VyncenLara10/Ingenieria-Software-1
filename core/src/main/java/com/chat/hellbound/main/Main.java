package com.chat.hellbound.main;

import com.badlogic.gdx.Game;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}
