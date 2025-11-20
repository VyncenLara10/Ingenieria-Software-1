package com.chat.hellbound.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class InputController {

    // Ejes finales que se usan en el juego
    public static float xAxis = 0f;
    public static float yAxis = 0f;

    private static boolean attackPressedThisFrame = false;
    private static boolean activeAvility = false;

    // Coordenadas de botones / joystick dentro del viewport
    private static float joyCX, joyCY, joyR;
    private static float atkCX, atkCY, atkR;
    private static float abiCX, abiCY, abiR;

    private static final float MARGIN = 24f;

    // Punteros táctiles
    private static int joyPointer = -1;
    private static int atkPointer = -1;
    private static int abiPointer = -1;

    private static boolean layoutDirty = true;

    private static final float JOY_HIT_SCALE = 1.20f;
    private static final float BTN_HIT_SCALE = 1.25f;
    private static final float JOY_DEADZONE = 0.08f;

    // Viewport real donde se dibuja el juego
    private static int vpX = 0;
    private static int vpY = 0;
    private static int vpW = 1;
    private static int vpH = 1;

    // Nuevo: referencia real al viewport
    private static Viewport viewport;
    private static final Vector2 tempVec = new Vector2();

    // -------------------------------------------------------------------------
    // CONFIGURAR VIEWPORT
    // -------------------------------------------------------------------------
    public static void setViewport(Viewport vp) {
        viewport = vp;
    }

    /** Se llama desde la Screen cada vez que se actualiza el viewport */
    public static void setViewportBounds(int x, int y, int w, int h) {
        vpX = x;
        vpY = y;
        vpW = Math.max(1, w);
        vpH = Math.max(1, h);
        layoutDirty = true;
    }

    // -------------------------------------------------------------------------
    // UPDATE GENERAL
    // -------------------------------------------------------------------------
    public static void update() {
        attackPressedThisFrame = false;
        activeAvility = false;

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            updateLayoutIfNeeded();
            pollTouch();
        } else {
            pollDesktop();
        }
    }

    // -------------------------------------------------------------------------
    // DESKTOP INPUT
    // -------------------------------------------------------------------------
    private static void pollDesktop() {
        float x = 0f, y = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  y -= 1f;

        xAxis = x;
        yAxis = y;

        boolean pressed = false;
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) pressed = true;
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) pressed = true;
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) pressed = true;
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) pressed = true;
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) pressed = true;

        attackPressedThisFrame = pressed;

        boolean ap = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) ap = true;
        activeAvility = ap;
    }

    // -------------------------------------------------------------------------
    // TOUCH INPUT (ANDROID)
    // -------------------------------------------------------------------------
    private static void pollTouch() {
        float x = 0f, y = 0f;

        if (joyPointer != -1 && !Gdx.input.isTouched(joyPointer)) joyPointer = -1;
        if (atkPointer != -1 && !Gdx.input.isTouched(atkPointer)) atkPointer = -1;
        if (abiPointer != -1 && !Gdx.input.isTouched(abiPointer)) abiPointer = -1;

        boolean newAttack = false;
        boolean newAbility = false;

        int maxP = 20;

        // ---------------------------------------------------------------------
        // 1) ATRIBUIR PUNTEROS
        // ---------------------------------------------------------------------
        for (int p = 0; p < maxP; p++) {
            if (!Gdx.input.isTouched(p)) continue;

            float rawX = Gdx.input.getX(p);
            float rawY = Gdx.input.getY(p);

            // Convertir TOUCH SCREEN → WORLD COORDS del viewport
            viewport.unproject(tempVec.set(rawX, rawY));
            float sx = tempVec.x;
            float sy = tempVec.y;

            boolean joyFree = (joyPointer == -1);
            boolean atkFree = (atkPointer == -1);
            boolean abiFree = (abiPointer == -1);

            boolean inJoy = joyFree && isInsideScaled(sx, sy, joyCX, joyCY, joyR, JOY_HIT_SCALE);
            boolean inAtk = atkFree && isInsideScaled(sx, sy, atkCX, atkCY, atkR, BTN_HIT_SCALE);
            boolean inAbi = abiFree && isInsideScaled(sx, sy, abiCX, abiCY, abiR, BTN_HIT_SCALE);

            if (!(inJoy || inAtk || inAbi)) continue;

            float bestDist2 = Float.MAX_VALUE;
            int best = 0;

            if (inJoy) {
                float d2 = dist2(sx, sy, joyCX, joyCY);
                if (d2 < bestDist2) { bestDist2 = d2; best = 1; }
            }
            if (inAtk) {
                float d2 = dist2(sx, sy, atkCX, atkCY);
                if (d2 < bestDist2) { bestDist2 = d2; best = 2; }
            }
            if (inAbi) {
                float d2 = dist2(sx, sy, abiCX, abiCY);
                if (d2 < bestDist2) { bestDist2 = d2; best = 3; }
            }

            if (best == 1 && joyPointer == -1) {
                joyPointer = p;
            } else if (best == 2 && atkPointer == -1) {
                atkPointer = p;
                newAttack = true;
            } else if (best == 3 && abiPointer == -1) {
                abiPointer = p;
                newAbility = true;
            }
        }

        // ---------------------------------------------------------------------
        // 2) JOYSTICK MOVIMIENTO
        // ---------------------------------------------------------------------
        if (joyPointer != -1 && Gdx.input.isTouched(joyPointer)) {
            float rawX = Gdx.input.getX(joyPointer);
            float rawY = Gdx.input.getY(joyPointer);

            // Convertir a coords del mundo
            viewport.unproject(tempVec.set(rawX, rawY));
            float sx = tempVec.x;
            float sy = tempVec.y;

            float dx = sx - joyCX;
            float dy = sy - joyCY;
            float len = (float) Math.sqrt(dx*dx + dy*dy);

            if (len > 0.0001f) {
                float m = Math.min(1f, len / joyR);
                float nx = dx / len;
                float ny = dy / len;

                if (m < JOY_DEADZONE) {
                    x = 0f; y = 0f;
                } else {
                    float t = (m - JOY_DEADZONE) / (1f - JOY_DEADZONE);
                    x = nx * t;
                    y = ny * t;
                }
            }
        }

        xAxis = x;
        yAxis = y;
        attackPressedThisFrame = newAttack;
        activeAvility = newAbility;

        if (atkPointer != -1 && !Gdx.input.isTouched(atkPointer)) atkPointer = -1;
        if (abiPointer != -1 && !Gdx.input.isTouched(abiPointer)) abiPointer = -1;
    }

    // -------------------------------------------------------------------------
    // UTILS
    // -------------------------------------------------------------------------
    private static boolean isInsideScaled(float x, float y, float cx, float cy, float r, float scale) {
        float rr = r * scale;
        float dx = x - cx, dy = y - cy;
        return dx*dx + dy*dy <= rr*rr;
    }

    private static float dist2(float x, float y, float cx, float cy) {
        float dx = x - cx, dy = y - cy;
        return dx*dx + dy*dy;
    }

    // -------------------------------------------------------------------------
    // LAYOUT
    // -------------------------------------------------------------------------
    private static void updateLayoutIfNeeded() {
        if (!layoutDirty) return;

        int sw = vpW;
        int sh = vpH;

        float localJoyR = Math.min(sw, sh) * 0.12f;
        float localJoyCX = MARGIN + localJoyR;
        float localJoyCY = MARGIN + localJoyR;

        float localAtkR = localJoyR * 0.9f;
        float localAtkCX = sw - (MARGIN + localAtkR);
        float localAtkCY = MARGIN + localAtkR;

        float localAbiR = localAtkR * 0.85f;
        float localAbiCX = localAtkCX;
        float localAbiCY = localAtkCY + localAtkR + MARGIN + localAbiR;

        float abiCYMax = sh - (MARGIN + localAbiR);
        if (localAbiCY > abiCYMax) localAbiCY = abiCYMax;

        joyR = localJoyR;
        joyCX = vpX + localJoyCX;
        joyCY = vpY + localJoyCY;

        atkR = localAtkR;
        atkCX = vpX + localAtkCX;
        atkCY = vpY + localAtkCY;

        abiR = localAbiR;
        abiCX = vpX + localAbiCX;
        abiCY = vpY + localAbiCY;

        layoutDirty = false;
    }

    public static void invalidateLayout() { layoutDirty = true; }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------
    public static boolean attackPressedThisFrame() { return attackPressedThisFrame; }
    public static boolean activeAvilityPressed()   { return activeAvility; }

    public static float getJoyCX() { return joyCX; }
    public static float getJoyCY() { return joyCY; }
    public static float getJoyR()  { return joyR; }
    public static float getAtkCX() { return atkCX; }
    public static float getAtkCY() { return atkCY; }
    public static float getAtkR()  { return atkR; }
    public static float getAbiCX() { return abiCX; }
    public static float getAbiCY() { return abiCY; }
    public static float getAbiR()  { return abiR; }

    public static boolean isJoyActive() {
        return joyPointer != -1;
    }

    public static boolean isAndroid() {
        return Gdx.app.getType() == Application.ApplicationType.Android;
    }
}
