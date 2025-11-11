package com.chat.hellbound.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.chat.hellbound.utilz.LoadSave;

public class LevelManager {

    private final Texture atlas;
    private final TextureRegion[] slots;
    private Level currentLevel;

    public LevelManager() {
        atlas = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);

        atlas.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        int cols = 10;
        int slotW = atlas.getWidth() / cols;
        int slotH = atlas.getHeight();
        slots = new TextureRegion[cols];

        for (int i = 0; i < cols; i++) {
            slots[i] = new TextureRegion(atlas, i * slotW, 0, slotW, slotH);
        }

        loadLevel();
    }

    public void loadLevel() {
        int[][] data = LoadSave.GetLevelData();
        currentLevel = new Level(data);
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public void draw(SpriteBatch batch, float xLvlOffset) {
        if (currentLevel == null) return;

        int[][] data = currentLevel.getLevelData();
        int h = currentLevel.getHeight();
        int w = currentLevel.getWidth();

        int spriteW = slots[0].getRegionWidth();
        int spriteH = slots[0].getRegionHeight();

        for (int mapY = 0; mapY < h; mapY++) {
            int drawY = (h - 1 - mapY) * spriteH;

            for (int mapX = 0; mapX < w; mapX++) {

                int id = data[mapY][mapX];

                if (id < 0 || id >= slots.length)
                    continue;

                TextureRegion region = slots[id];

                float drawX = mapX * spriteW - xLvlOffset;
                batch.draw(region, drawX, drawY);
            }
        }
    }

    public void dispose() {
        atlas.dispose();
    }

    public int getWorldWidthPx() {
        int w = currentLevel != null ? currentLevel.getWidth() : 0;
        return w * slots[0].getRegionWidth();   // cada celda usa el ancho NATIVO del sprite
    }

    public int getWorldHeightPx() {
        int h = currentLevel != null ? currentLevel.getHeight() : 0;
        return h * slots[0].getRegionHeight();  // alto nativo
    }

    public int[][] getLevelData() {
        return currentLevel != null ? currentLevel.getLevelData() : null;
    }

    public int getTileWidth() {
        return slots[0].getRegionWidth();
    }

    public int getTileHeight() {
        return slots[0].getRegionHeight();
    }

}
