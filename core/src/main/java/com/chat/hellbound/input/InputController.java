package com.chat.hellbound.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {

    public static float xAxis = 0f;
    public static float yAxis = 0f;

    private static boolean attackPressedThisFrame = false;
    private static boolean activeAvility = false; // “E” (habilidad)

    private static float joyCX, joyCY, joyR;
    private static float atkCX, atkCY, atkR;

    // botón habilidad (E)
    private static float abiCX, abiCY, abiR;

    private static final float MARGIN = 24f;

    private static int joyPointer = -1;
    private static int atkPointer = -1;
    private static int abiPointer = -1;

    private static boolean layoutDirty = true;

    // --- NUEVO: “slop” para hacer más fácil acertar al círculo ---
    private static final float JOY_HIT_SCALE = 1.20f; // 20% más grande para capturar toque
    private static final float BTN_HIT_SCALE = 1.25f; // botones un poco más permisivos
    // deadzone pequeña para joystick (no cambia funcionalidad, sólo evita ruido)
    private static final float JOY_DEADZONE = 0.08f;

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

    private static void pollTouch() {
        float x = 0f, y = 0f;

        if (joyPointer != -1 && !Gdx.input.isTouched(joyPointer)) joyPointer = -1;
        if (atkPointer != -1 && !Gdx.input.isTouched(atkPointer)) atkPointer = -1;
        if (abiPointer != -1 && !Gdx.input.isTouched(abiPointer)) abiPointer = -1;

        boolean newAttack = false;
        boolean newAbility = false;

        int sh = Gdx.graphics.getHeight();
        int maxP = 20;

        // 1) Asignar punteros nuevos con preferencia por el control más cercano si cae en varios
        for (int p = 0; p < maxP; p++) {
            if (!Gdx.input.isTouched(p)) continue;
            float sx = Gdx.input.getX(p);
            float sy = sh - Gdx.input.getY(p);

            boolean joyFree = (joyPointer == -1);
            boolean atkFree = (atkPointer == -1);
            boolean abiFree = (abiPointer == -1);

            boolean inJoy = joyFree && isInsideScaled(sx, sy, joyCX, joyCY, joyR, JOY_HIT_SCALE);
            boolean inAtk = atkFree && isInsideScaled(sx, sy, atkCX, atkCY, atkR, BTN_HIT_SCALE);
            boolean inAbi = abiFree && isInsideScaled(sx, sy, abiCX, abiCY, abiR, BTN_HIT_SCALE);

            if (!(inJoy || inAtk || inAbi)) continue;

            // Si toca varios, elegimos el más cercano al centro
            float bestDist2 = Float.MAX_VALUE;
            int best = 0; // 1=joy, 2=atk, 3=abi

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

        // 2) Joystick: calcular ejes con deadzone suave (misma funcionalidad, menos ruido)
        if (joyPointer != -1 && Gdx.input.isTouched(joyPointer)) {
            float sx = Gdx.input.getX(joyPointer);
            float sy = sh - Gdx.input.getY(joyPointer);
            float dx = sx - joyCX;
            float dy = sy - joyCY;
            float len = (float) Math.sqrt(dx*dx + dy*dy);
            if (len > 0.0001f) {
                float m = Math.min(1f, len / joyR);
                float nx = dx / len;
                float ny = dy / len;
                // aplicar deadzone
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

        // limpiar punteros sueltos (por si acaso)
        if (atkPointer != -1 && !Gdx.input.isTouched(atkPointer)) atkPointer = -1;
        if (abiPointer != -1 && !Gdx.input.isTouched(abiPointer)) abiPointer = -1;
    }

    private static boolean isInsideScaled(float x, float y, float cx, float cy, float r, float scale) {
        float rr = r * scale;
        float dx = x - cx, dy = y - cy;
        return dx*dx + dy*dy <= rr*rr;
    }

    private static float dist2(float x, float y, float cx, float cy) {
        float dx = x - cx, dy = y - cy;
        return dx*dx + dy*dy;
    }

    private static void updateLayoutIfNeeded() {
        if (!layoutDirty) return;
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        // Joystick (abajo-izquierda)
        joyR = Math.min(sw, sh) * 0.12f;
        joyCX = MARGIN + joyR;
        joyCY = MARGIN + joyR;

        // Ataque (abajo-derecha)
        atkR = joyR * 0.9f;
        atkCX = sw - (MARGIN + atkR);
        atkCY = MARGIN + atkR;

        // Habilidad (E) — encima del ataque
        abiR  = atkR * 0.85f;
        abiCX = atkCX;
        abiCY = atkCY + atkR + MARGIN + abiR;
        float abiCYMax = sh - (MARGIN + abiR);
        if (abiCY > abiCYMax) abiCY = abiCYMax;

        layoutDirty = false;
    }

    public static void invalidateLayout() { layoutDirty = true; }

    public static boolean attackPressedThisFrame() { return attackPressedThisFrame; }
    public static boolean activeAvilityPressed(){ return activeAvility; }

    public static float getJoyCX() { return joyCX; }
    public static float getJoyCY() { return joyCY; }
    public static float getJoyR()  { return joyR;  }
    public static float getAtkCX() { return atkCX; }
    public static float getAtkCY() { return atkCY; }
    public static float getAtkR()  { return atkR;  }
    public static float getAbiCX() { return abiCX; }
    public static float getAbiCY() { return abiCY; }
    public static float getAbiR()  { return abiR;  }

    public static boolean isAndroid() {
        return Gdx.app.getType() == Application.ApplicationType.Android;
    }
}
