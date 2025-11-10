package com.chat.hellbound.utilz;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class Assets {

    private static AssetManager manager;

    public static void load() {
        if (manager != null) return;
        manager = new AssetManager();

        manager.load(LoadSave.PLAYER_ATLAS, Texture.class);
        manager.load(LoadSave.LEVEL_ATLAS, Texture.class);
        manager.load(LoadSave.CRABBY_SPRITE, Texture.class);

        manager.finishLoading();
    }

    public static Texture getPlayerAtlas() {
        return manager.get(LoadSave.PLAYER_ATLAS, Texture.class);
    }

    public static Texture getLevelAtlas() {
        return manager.get(LoadSave.LEVEL_ATLAS, Texture.class);
    }

    public static Texture getCrabbyAtlas() {
        return manager.get(LoadSave.CRABBY_SPRITE, Texture.class);
    }

    public static void dispose() {
        if (manager != null) {
            manager.dispose();
            manager = null;
        }
    }
}
