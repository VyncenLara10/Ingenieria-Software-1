package com.chat.hellbound.utilz;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class Assets {

    private static AssetManager manager;

    public static void load() {
        if (manager != null) return;
        manager = new AssetManager();

        manager.load("player_sprites.png", Texture.class);

        manager.finishLoading();
    }

    public static Texture getPlayerAtlas() {
        return manager.get("player_sprites.png", Texture.class);
    }

    public static void dispose() {
        if (manager != null) {
            manager.dispose();
            manager = null;
        }
    }
}
