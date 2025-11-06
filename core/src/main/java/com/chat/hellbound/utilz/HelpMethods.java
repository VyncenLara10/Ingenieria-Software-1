package com.chat.hellbound.utilz;

public class HelpMethods {

    public static boolean CanMoveHere(float x, float y, float width, float height,
                                      int[][] lvlData, int tileW, int tileH) {
        if (IsSolid(x,y,lvlData, tileW, tileH)) return false;
        if (IsSolid(x + width,y, lvlData, tileW, tileH)) return false;
        if (IsSolid(x,y + height,lvlData, tileW, tileH)) return false;
        if (IsSolid(x + width,y + height,lvlData, tileW, tileH)) return false;
        return true;
    }

    public static boolean IsSolid(float x, float y, int[][] lvlData, int tileW, int tileH) {
        if (x < 0 || y < 0) return true;

        int rows = lvlData.length;
        int cols = lvlData[0].length;
        int maxX = cols * tileW;
        int maxY = rows * tileH;
        if (x >= maxX || y >= maxY) return true;

        int tileX = (int)(x / tileW);
        int tileY = rows - 1 - (int)(y / tileH);

        int tileId = lvlData[tileY][tileX];
        return com.chat.hellbound.utilz.TileMapping.isSolid(tileId);
    }

    public static float GetEntityXPosNextToWall(com.badlogic.gdx.math.Rectangle hitbox, float xDelta,
                                                int[][] lvlData, int tileW, int tileH) {
        if (xDelta > 0) {
            int tileX = (int)((hitbox.x + hitbox.width + xDelta) / tileW);
            return tileX * tileW - hitbox.width - 0.01f;
        } else {
            int tileX = (int)((hitbox.x + xDelta) / tileW);
            return (tileX + 1) * tileW + 0.01f;
        }
    }

    public static float GetEntityYPosUnderRoofOrAboveFloor(com.badlogic.gdx.math.Rectangle hitbox, float yDelta,
                                                           int[][] lvlData, int tileW, int tileH) {
        if (yDelta > 0) {
            int tileY = (int)((hitbox.y + hitbox.height + yDelta) / tileH);
            return tileY * tileH - hitbox.height - 0.01f;
        } else {
            int tileY = (int)((hitbox.y + yDelta) / tileH);
            return (tileY + 1) * tileH + 0.01f;
        }
    }

    public static boolean IsEntityOnFloor(com.badlogic.gdx.math.Rectangle hitbox,
                                          int[][] lvlData, int tileW, int tileH) {
        float xLeft  = hitbox.x + 1;
        float xRight = hitbox.x + hitbox.width - 1;
        float yBelow = hitbox.y - 1;
        return IsSolid(xLeft, yBelow, lvlData, tileW, tileH)
            || IsSolid(xRight, yBelow, lvlData, tileW, tileH);
    }
}
