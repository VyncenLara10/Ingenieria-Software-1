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
        manager.load(LoadSave.WIN_OBJ1_MAP1, Texture.class);
        manager.load(LoadSave.WIN_OBJ2_MAP1, Texture.class);
        manager.load(LoadSave.WIN_OBJ3_MAP1, Texture.class);
        manager.load(LoadSave.WIN_OBJ1_MAP2, Texture.class);
        manager.load(LoadSave.WIN_OBJ2_MAP2, Texture.class);
        manager.load(LoadSave.WIN_OBJ3_MAP2, Texture.class);

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

    public static Texture getWinObjectAtlas(int level, int type) {
        if (level == 1) {
            if (type == 1) return manager.get(LoadSave.WIN_OBJ1_MAP1, Texture.class);
            if (type == 2) return manager.get(LoadSave.WIN_OBJ2_MAP1, Texture.class);
            if (type == 3) return manager.get(LoadSave.WIN_OBJ3_MAP1, Texture.class);
        }
        if (level == 2) {
            if (type == 1) return manager.get(LoadSave.WIN_OBJ1_MAP2, Texture.class);
            if (type == 2) return manager.get(LoadSave.WIN_OBJ2_MAP2, Texture.class);
            if (type == 3) return manager.get(LoadSave.WIN_OBJ3_MAP2, Texture.class);
        }
        return manager.get(LoadSave.WIN_OBJ1_MAP1, Texture.class);
    }

    public static void dispose() {
        if (manager != null) {
            manager.dispose();
            manager = null;
        }
    }
}
