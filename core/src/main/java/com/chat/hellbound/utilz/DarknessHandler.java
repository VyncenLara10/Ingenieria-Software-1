package com.chat.hellbound.utilz;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * DarknessHandler: overlay mayormente negro con una pequeña zona clara alrededor del jugador.
 * - innerRadiusRatio: radio 100% claro (transparente).
 * - outerRadiusRatio: donde la máscara ya es totalmente negra (alpha = darknessAlpha).
 * El degradado se produce ENTRE innerR y outerR y es SHORT (muy cerca del jugador).
 */
public class DarknessHandler {

    private Texture darknessMask;
    private int maskDiameterPx;
    private float darknessAlpha; // 0..1 (cuánto oscuro será el borde)

    /**
     * @param screenW ancho de pantalla en px
     * @param screenH alto de pantalla en px
     * @param alpha   0..1 valor de opacidad máxima fuera del círculo (recomiendo 1.0f)
     */
    public DarknessHandler(int screenW, int screenH, float alpha) {
        this.darknessAlpha = Math.max(0f, Math.min(1f, alpha));
        rebuild(screenW, screenH);
    }

    /** Reconstruye la máscara (llamar en resize) */
    public void rebuild(int screenW, int screenH) {
        maskDiameterPx = Math.max(screenW, screenH) * 2; // suficientemente grande para cubrir pantalla
        if (darknessMask != null) darknessMask.dispose();
        darknessMask = buildRadialDarkTexture(maskDiameterPx, darknessAlpha);
    }

    /**
     * Dibuja la máscara centrada en (playerX, playerY) en coordenadas del world viewport.
     * Este método hace su propio begin/end del SpriteBatch para evitar errores de "draw" si el batch no está begun.
     */
    public void render(SpriteBatch batch,
                       float playerX, float playerY,
                       float camX, float camY,
                       float worldW, float worldH) {
        if (darknessMask == null) return;

        // convertir posición del jugador a coords de la textura en el viewport (igual que hacías antes)
        float sx = playerX - camX + worldW * 0.5f;
        float sy = playerY - camY + worldH * 0.5f;
        float drawX = sx - maskDiameterPx / 2f;
        float drawY = sy - maskDiameterPx / 2f;

        // Ajustamos la matriz de proyección a coordenadas de pantalla/viewport para dibujar la textura
        batch.setProjectionMatrix(batch.getProjectionMatrix().cpy().setToOrtho2D(0, 0, worldW, worldH));
        batch.begin();

        // blending estándar para máscaras con alpha
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.draw(darknessMask, drawX, drawY, maskDiameterPx, maskDiameterPx);
        // restauramos (opcional, pero seguro)
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        batch.end();
    }

    /**
     * Crea la textura radial con:
     * - centro totalmente transparente (d <= innerR)
     * - entre innerR y outerR: transición suave (0 -> aMax)
     * - fuera de outerR: alpha = aMax (negro sólido)
     */
    private Texture buildRadialDarkTexture(int size, float alpha) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        final float cx = size / 2f;
        final float cy = size / 2f;
        final float maxR = size / 2f;

        final float innerRadiusRatio = 0.02f;
        final float outerRadiusRatio = 0.09f;
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
                    // centro claro
                    ia = 0;
                } else if (d >= outerR) {
                    // fuera del degradado: negro sólido
                    ia = aMax;
                } else {
                    // en la banda de transición: interpolación suave
                    float t = (d - innerR) / (outerR - innerR); // normalizado 0..1
                    // easing para suavizar la curva
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
