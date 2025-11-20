package com.chat.hellbound.utilz;

import com.badlogic.gdx.math.Rectangle;

public class HelpMethods {

    public static boolean CanMoveHere(float x, float y, float width, float height,
                                      int[][] lvlData, int tileW, int tileH) {
        float xCenter = x + width / 2;
        float yCenter = y + height / 2;

        return !(
            IsSolid(x, y, lvlData, tileW, tileH) ||
                IsSolid(x + width, y, lvlData, tileW, tileH) ||
                IsSolid(x, y + height, lvlData, tileW, tileH) ||
                IsSolid(x + width, y + height, lvlData, tileW, tileH) ||
                IsSolid(xCenter, y, lvlData, tileW, tileH) ||
                IsSolid(xCenter, y + height, lvlData, tileW, tileH) ||
                IsSolid(x, yCenter, lvlData, tileW, tileH) ||
                IsSolid(x + width, yCenter, lvlData, tileW, tileH)
        );
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

        if (!TileMapping.isSolid(tileId))
            return false;

        float worldX = tileX * tileW;
        float worldY = (rows - 1 - tileY) * tileH;

        Rectangle hitbox = null;

        switch (tileId) {
            case TileMapping.TREE:
                hitbox = createCenteredHitbox(worldX, worldY, tileW, tileH, 0.3f, 0.8f);
                break;

            case TileMapping.ROCK1:
                hitbox = createCenteredHitbox(worldX, worldY, tileW, tileH, 0.4f, 0.3f);
                break;

            case TileMapping.ROCK2:
                hitbox = createCenteredHitbox(worldX, worldY, tileW, tileH, 0.4f, 0.3f);
                break;

            case TileMapping.TRUNK1:
                hitbox = createCenteredHitbox(worldX, worldY, tileW, tileH, 0.8f, 0.3f);
                break;

            case TileMapping.TRUNK2:
                hitbox = createCenteredHitbox(worldX, worldY, tileW, tileH, 0.4f, 0.5f);
                break;

            case TileMapping.TRUNK3:
                hitbox = createCenteredHitbox(worldX, worldY, tileW, tileH, 0.4f, 0.4f);
                break;

            default:
                hitbox = new Rectangle(worldX, worldY, tileW, tileH);
                break;
        }

        return hitbox.overlaps(new Rectangle(x, y, 1, 1));
    }

    /**
     * Crea una hitbox centrada dentro del tile.
     * @param wFactor proporción del ancho respecto al tile
     * @param hFactor proporción del alto respecto al tile
     */
    private static Rectangle createCenteredHitbox(float worldX, float worldY, int tileW, int tileH,
                                                  float wFactor, float hFactor) {
        float hitboxW = tileW * wFactor;
        float hitboxH = tileH * hFactor;
        float offsetX = (tileW - hitboxW) / 2f;
        float offsetY = (tileH - hitboxH) / 2f;
        return new Rectangle(worldX + offsetX, worldY + offsetY, hitboxW, hitboxH);
    }

    public static float GetEntityXPosNextToWall(Rectangle hitbox, float xDelta,
                                                int[][] lvlData, int tileW, int tileH) {
        if (xDelta > 0) { // moviéndose a la derecha
            int tileX = (int)((hitbox.x + hitbox.width + xDelta) / tileW);
            float newX = tileX * tileW - hitbox.width - 0.05f;

            // Solo ajusta si realmente hay colisión
            if (IsSolid(hitbox.x + hitbox.width + xDelta, hitbox.y + hitbox.height / 2, lvlData, tileW, tileH))
                return newX;
            return hitbox.x + xDelta;

        } else if (xDelta < 0) { // moviéndose a la izquierda
            int tileX = (int)((hitbox.x + xDelta) / tileW);
            float newX = (tileX + 1) * tileW + 0.05f;

            if (IsSolid(hitbox.x + xDelta, hitbox.y + hitbox.height / 2, lvlData, tileW, tileH))
                return newX;
            return hitbox.x + xDelta;
        }

        return hitbox.x;
    }

    public static float GetEntityYPosUnderRoofOrAboveFloor(Rectangle hitbox, float yDelta,
                                                           int[][] lvlData, int tileW, int tileH) {
        if (yDelta > 0) { // subiendo - techo
            int tileY = (int)((hitbox.y + hitbox.height + yDelta) / tileH);
            float newY = tileY * tileH - hitbox.height - 0.05f;

            if (IsSolid(hitbox.x + hitbox.width / 2, hitbox.y + hitbox.height + yDelta, lvlData, tileW, tileH))
                return newY;
            return hitbox.y + yDelta;

        } else if (yDelta < 0) { // bajando - piso
            int tileY = (int)((hitbox.y + yDelta) / tileH);
            float newY = (tileY + 1) * tileH + 0.05f;

            if (IsSolid(hitbox.x + hitbox.width / 2, hitbox.y + yDelta, lvlData, tileW, tileH))
                return newY;
            return hitbox.y + yDelta;
        }

        return hitbox.y;
    }

    public static boolean IsEntityOnFloor(Rectangle hitbox,
                                          int[][] lvlData, int tileW, int tileH) {
        // Un solo punto central inferior, evita falsos positivos
        float xCenter = hitbox.x + hitbox.width / 2f;
        float yBelow = hitbox.y - 0.5f;
        return IsSolid(xCenter, yBelow, lvlData, tileW, tileH);
    }

}
