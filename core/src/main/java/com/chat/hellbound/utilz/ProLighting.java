package com.chat.hellbound.utilz;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class ProLighting {

    private Texture darknessMask;
    private Pixmap pixmap;
    private int maskWidth;
    private int maskHeight;
    private float darknessAlpha;

    private static final float INNER_RADIUS_RATIO = 0.02f;
    private static final float OUTER_RADIUS_RATIO = 0.07f;
    private static final float FALLOFF_POW = 1.8f;

    public ProLighting(int screenW, int screenH, float ambient) {
        this.darknessAlpha = clamp01(ambient);
        initMaskSize(screenW, screenH);
        rebuild();
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private void initMaskSize(int screenW, int screenH) {
        int targetW = 256;
        float aspect = (float) screenH / (float) screenW;
        int targetH = (int) (targetW * aspect);

        this.maskWidth = Math.max(64, targetW);
        this.maskHeight = Math.max(36, targetH);
    }

    private void rebuild() {
        if (pixmap != null) {
            pixmap.dispose();
        }
        pixmap = new Pixmap(maskWidth, maskHeight, Pixmap.Format.RGBA8888);

        if (darknessMask != null) {
            darknessMask.dispose();
        }
        darknessMask = new Texture(pixmap);
        darknessMask.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    }

    public void resize(int screenW, int screenH) {
        initMaskSize(screenW, screenH);
        rebuild();
    }

    public void render(SpriteBatch batch,
                       float camX, float camY,
                       float worldW, float worldH,
                       float[] lightWorldX, float[] lightWorldY,
                       int lightCount) {
        if (darknessMask == null || pixmap == null || lightCount <= 0) return;

        float minDim = Math.min(worldW, worldH);
        float innerR = minDim * INNER_RADIUS_RATIO;
        float outerR = minDim * OUTER_RADIUS_RATIO;
        float invBand = 1f / (outerR - innerR);
        float aMax = darknessAlpha;

        float worldPerPixelX = worldW / (float) maskWidth;
        float worldPerPixelY = worldH / (float) maskHeight;

        pixmap.setBlending(Pixmap.Blending.None);

        float left = camX - worldW * 0.5f;
        float bottom = camY - worldH * 0.5f;

        for (int j = 0; j < maskHeight; j++) {
            float wy = bottom + (j + 0.5f) * worldPerPixelY;
            for (int i = 0; i < maskWidth; i++) {
                float wx = left + (i + 0.5f) * worldPerPixelX;

                float dMin = Float.MAX_VALUE;

                for (int k = 0; k < lightCount; k++) {
                    float lx = lightWorldX[k];
                    float ly = lightWorldY[k];
                    float dx = wx - lx;
                    float dy = wy - ly;
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    if (d < dMin) dMin = d;
                }

                float a;
                if (dMin <= innerR) {
                    a = 0f;
                } else if (dMin >= outerR) {
                    a = aMax;
                } else {
                    float t = (dMin - innerR) * invBand;
                    float eased = (float) Math.pow(t, FALLOFF_POW);
                    a = eased * aMax;
                }

                pixmap.setColor(0f, 0f, 0f, a);
                pixmap.drawPixel(i, j);
            }
        }

        darknessMask.draw(pixmap, 0, 0);

        Matrix4 oldProj = new Matrix4(batch.getProjectionMatrix());
        Matrix4 proj = new Matrix4().setToOrtho2D(0, 0, worldW, worldH);
        batch.setProjectionMatrix(proj);

        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.draw(darknessMask, 0, 0, worldW, worldH);
        batch.end();

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(oldProj);
    }

    public void dispose() {
        if (darknessMask != null) {
            darknessMask.dispose();
            darknessMask = null;
        }
        if (pixmap != null) {
            pixmap.dispose();
            pixmap = null;
        }
    }
}
