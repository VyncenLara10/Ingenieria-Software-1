package com.chat.hellbound.utilz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class LoadSave {

    public static final String PLAYER_ATLAS       = "player_sprites.png";
    public static final String LEVEL_ATLAS        = "spritesleveldefinitive.png";
    public static final String LEVEL_ONE_DATA     = "maptiletypebig.png";
    public static final String MENU_BUTTONS       = "button_atlas.png";
    public static final String MENU_BACKGROUND    = "menu_background.png";
    public static final String PAUSE_BACKGROUND   = "pause_menu.png";
    public static final String SOUND_BUTTONS      = "sound_button.png";
    public static final String URM_BUTTONS        = "urm_buttons.png";
    public static final String VOLUME_BUTTONS     = "volume_buttons.png";
    public static final String MENU_BACKGROUND_IMG= "background_menu.png";
    public static final String PLAYING_BG_IMG     = "playing_bg_img.png";
    public static final String BIG_CLOUDS         = "big_clouds.png";
    public static final String SMALL_CLOUDS       = "small_clouds.png";
    public static final String CRABBY_SPRITE      = "crabby_sprite.png";
    public static final String STATUS_BAR         = "health_power_bar.png";

    public static Texture GetSpriteAtlas(String fileName) {
        return new Texture(Gdx.files.internal(fileName));
    }

    public static int[][] GetLevelData() {
        FileHandle fh = Gdx.files.internal(LEVEL_ONE_DATA);
        Pixmap pm = new Pixmap(fh);

        int w = pm.getWidth();
        int h = pm.getHeight();
        int[][] lvlData = new int[h][w];

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int rgba = pm.getPixel(i, j);
                int r = (rgba >>> 24) & 0xFF;
                int tileType = TileMapping.getTileTypeFromRed(r);
                lvlData[j][i] = tileType;
            }
        }
        pm.dispose();
        return lvlData;
    }
}
