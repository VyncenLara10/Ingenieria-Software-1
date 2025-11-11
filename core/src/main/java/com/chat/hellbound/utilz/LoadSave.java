package com.chat.hellbound.utilz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import static com.chat.hellbound.utilz.Constants.EnemyConstants;

public class LoadSave {

    public static final String PLAYER_ATLAS       = "player_sprites.png";
    public static final String LEVEL_ATLAS        = "spritesleveldefinitive.png";
    public static final String LEVEL_ONE_DATA     = "maptilesections.png";
    public static  final String WIN_OBJ1_MAP1     = "dados.png";
    public static  final String WIN_OBJ2_MAP1     = "retro.png";
    public static  final String WIN_OBJ3_MAP1     = "selfone.png";
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
        Pixmap pm = MapGenerator.generarMapaFinal(LEVEL_ONE_DATA);

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

        pm.dispose(); // Liberar el Pixmap de la RAM de video

        return lvlData;
    }

    public static ArrayList<Vector2> GetCrabs(int tileW, int tileH) {
        Pixmap pm = new Pixmap(Gdx.files.internal(LEVEL_ONE_DATA));
        int w = pm.getWidth();
        int h = pm.getHeight();
        ArrayList<Vector2> list = new ArrayList<>();

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int rgba = pm.getPixel(i, j);
                int g = (rgba >>> 16) & 0xFF;

                if (g == Constants.EnemyConstants.CRABBY) {
                    boolean upIsCrabby   = false;
                    boolean leftIsCrabby = false;

                    if (j > 0) {
                        int rgbaUp = pm.getPixel(i, j - 1);
                        int gUp = (rgbaUp >>> 16) & 0xFF;
                        upIsCrabby = (gUp == Constants.EnemyConstants.CRABBY);
                    }
                    if (i > 0) {
                        int rgbaLeft = pm.getPixel(i - 1, j);
                        int gLeft = (rgbaLeft >>> 16) & 0xFF;
                        leftIsCrabby = (gLeft == Constants.EnemyConstants.CRABBY);
                    }

                    if (!upIsCrabby && !leftIsCrabby) {
                        int drawY = (h - 1 - j); // invertir Y una sola vez
                        list.add(new Vector2(i * tileW, drawY * tileH));
                    }
                }
            }
        }
        pm.dispose();
        return list;
    }

    public static ArrayList<MapObject> GetObjects(int tileW, int tileH) {
        Pixmap pm = new Pixmap(Gdx.files.internal(LEVEL_ONE_DATA));
        int w = pm.getWidth();
        int h = pm.getHeight();

        ArrayList<MapObject> list = new ArrayList<>();

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int rgba = pm.getPixel(i, j);

                int b = (rgba >>> 8) & 0xFF;

                if (b == 255 || b == 254 || b == 253) {
                    int drawY = (h - 1 - j);

                    int type = 0;
                    if (b == 255) type = 1;
                    else if (b == 254) type = 2;
                    else if (b == 253) type = 3;

                    list.add(new MapObject(new Vector2(i * tileW, drawY * tileH), type));
                }
            }
        }

        pm.dispose();
        return list;
    }

}
