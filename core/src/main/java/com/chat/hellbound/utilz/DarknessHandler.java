package com.chat.hellbound.utilz;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class DarknessHandler {

    private Texture darknessMask;
    private int maskDiameterPx;
    private float darknessAlpha;

    public DarknessHandler(int screenW, int screenH, float alpha) {
        this.darknessAlpha = clamp01(alpha);
        rebuild(screenW, screenH);
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    public void rebuild(int screenW, int screenH) {
        maskDiameterPx = Math.max(screenW, screenH) * 2;

        if (darknessMask != null) {
            darknessMask.dispose();
        }
        darknessMask = buildRadialDarkTexture(maskDiameterPx, darknessAlpha);
    }

    public void resize(int screenW, int screenH) {
        rebuild(screenW, screenH);
    }

    public void render(SpriteBatch batch,
                       float playerX, float playerY,
                       float camX, float camY,
                       float worldW, float worldH) {
        if (darknessMask == null) return;

        float sx = playerX - camX + worldW * 0.5f;
        float sy = playerY - camY + worldH * 0.5f;
        float drawX = sx - maskDiameterPx / 2f;
        float drawY = sy - maskDiameterPx / 2f;

        Matrix4 oldProj = new Matrix4(batch.getProjectionMatrix());
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, worldW, worldH));

        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.draw(darknessMask, drawX, drawY, maskDiameterPx, maskDiameterPx);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.end();

        batch.setProjectionMatrix(oldProj);
    }

    private Texture buildRadialDarkTexture(int size, float alpha) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        final float cx = size / 2f;
        final float cy = size / 2f;
        final float maxR = size / 2f;

        final float innerRadiusRatio = 0.02f;
        final float outerRadiusRatio = 0.07f;
        final float falloffPow = 1.8f;

        final float innerR = maxR * innerRadiusRatio;
        final float outerR = maxR * outerRadiusRatio;
        final int aMax = (int) (alpha * 255f);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float d = (float) Math.sqrt(dx * dx + dy * dy);

                int ia;
                if (d <= innerR) {
                    ia = 0;
                } else if (d >= outerR) {
                    ia = aMax;
                } else {
                    float t = (d - innerR) / (outerR - innerR);
                    float eased = (float) Math.pow(t, falloffPow);
                    ia = (int) (eased * aMax);
                }

                pm.setColor(0f, 0f, 0f, ia / 255f);
                pm.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    public void dispose() {
        if (darknessMask != null) {
            darknessMask.dispose();
            darknessMask = null;
        }
    }
}
