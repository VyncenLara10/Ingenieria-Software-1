package com.chat.hellbound.utilz;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureUtils {

    public static void prepareTexture(Texture tex) {
        if (tex == null) return;
        tex.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
    }

    public static void fixBleeding(TextureRegion region) {
        if (region == null) return;
        float texW = region.getTexture().getWidth();
        float texH = region.getTexture().getHeight();
        float epsilonU = 0.5f / texW;
        float epsilonV = 0.5f / texH;

        float u = region.getU();
        float v = region.getV();
        float u2 = region.getU2();
        float v2 = region.getV2();

        if (u < u2) { u  += epsilonU; u2 -= epsilonU; }
        else        { u  -= epsilonU; u2 += epsilonU; }
        if (v < v2) { v  += epsilonV; v2 -= epsilonV; }
        else        { v  -= epsilonV; v2 += epsilonV; }

        region.setU(u);
        region.setV(v);
        region.setU2(u2);
        region.setV2(v2);
    }

    public static void fixBleeding(TextureRegion[][] grid) {
        if (grid == null) return;
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                fixBleeding(grid[r][c]);
            }
        }
    }
}
