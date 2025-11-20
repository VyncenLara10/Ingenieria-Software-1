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
    private int currentLevelIndex;

    public LevelManager(Texture atlas, int level) {
        this.atlas = atlas;

        atlas.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        // Atlas dividido en 10 columnas (como dijiste)
        int cols = 10;
        int tW = atlas.getWidth() / cols;
        int tH = atlas.getHeight();

        slots = new TextureRegion[cols];

        for (int c = 0; c < cols; c++) {
            slots[c] = new TextureRegion(atlas, c * tW, 0, tW, tH);
        }

        loadLevel(level);
    }

    public void loadLevel(int level) {
        currentLevelIndex = level;

        int[][] data = LoadSave.GetLevelData(level);
        currentLevel = new Level(data);
    }

    /** 🔥 Para MULTIJUGADOR — sobreescribir mapa con el del HOST */
    public void setLevelData(int[][] data) {
        if (data == null) return;
        currentLevel = new Level(data);
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public int[][] getLevelData() {
        return (currentLevel != null ? currentLevel.getLevelData() : null);
    }

    public int getTileWidth() { return slots[0].getRegionWidth(); }
    public int getTileHeight() { return slots[0].getRegionHeight(); }

    public int getWorldWidthPx() {
        return currentLevel.getWidth() * getTileWidth();
    }

    public int getWorldHeightPx() {
        return currentLevel.getHeight() * getTileHeight();
    }

    public void draw(SpriteBatch batch, float xOffset) {
        if (currentLevel == null) return;

        int[][] map = currentLevel.getLevelData();
        int h = currentLevel.getHeight();
        int w = currentLevel.getWidth();

        int tileW = getTileWidth();
        int tileH = getTileHeight();

        for (int y = 0; y < h; y++) {
            int drawY = (h - 1 - y) * tileH;

            for (int x = 0; x < w; x++) {

                int id = map[y][x];

                if (id < 0 || id >= slots.length) continue;

                TextureRegion region = slots[id];
                float drawX = x * tileW - xOffset;

                batch.draw(region, drawX, drawY);
            }
        }
    }

    public void dispose() {
        atlas.dispose();
    }
}
