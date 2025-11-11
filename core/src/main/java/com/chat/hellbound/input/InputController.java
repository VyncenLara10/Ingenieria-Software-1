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

    // --- NUEVO: botón de habilidad (E) en Android ---
    private static float abiCX, abiCY, abiR;

    private static final float MARGIN = 24f;

    private static int joyPointer = -1;
    private static int atkPointer = -1;
    // --- NUEVO ---
    private static int abiPointer = -1;

    private static boolean layoutDirty = true;

    public static void update() {
        // reiniciar flags de “just pressed” cada frame
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

        // “E” en desktop
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
        for (int p = 0; p < maxP; p++) {
            if (!Gdx.input.isTouched(p)) continue;
            float sx = Gdx.input.getX(p);
            float sy = sh - Gdx.input.getY(p);

            if (joyPointer == -1 && isInside(sx, sy, joyCX, joyCY, joyR)) {
                joyPointer = p;
            }
            if (atkPointer == -1 && isInside(sx, sy, atkCX, atkCY, atkR)) {
                atkPointer = p;
                newAttack = true;
            }
            // --- NUEVO: botón de habilidad (E) ---
            if (abiPointer == -1 && isInside(sx, sy, abiCX, abiCY, abiR)) {
                abiPointer = p;
                newAbility = true; // “just pressed”
            }
        }

        if (joyPointer != -1 && Gdx.input.isTouched(joyPointer)) {
            float sx = Gdx.input.getX(joyPointer);
            float sy = sh - Gdx.input.getY(joyPointer);
            float dx = sx - joyCX;
            float dy = sy - joyCY;
            float len = (float) Math.sqrt(dx*dx + dy*dy);
            if (len > 0.0001f) {
                float nx = dx / len;
                float ny = dy / len;
                float m = Math.min(1f, len / joyR);
                x = nx * m;
                y = ny * m;
            }
        }

        xAxis = x;
        yAxis = y;

        attackPressedThisFrame = newAttack;
        activeAvility = newAbility;

        // limpiar punteros sueltos
        if (atkPointer != -1 && !Gdx.input.isTouched(atkPointer)) atkPointer = -1;
        if (abiPointer != -1 && !Gdx.input.isTouched(abiPointer)) abiPointer = -1;
    }

    private static boolean isInside(float x, float y, float cx, float cy, float r) {
        float dx = x - cx, dy = y - cy;
        return dx*dx + dy*dy <= r*r;
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

        // --- NUEVO: Habilidad (E) — a la derecha, encima del botón de ataque ---
        abiR  = atkR * 0.85f;
        abiCX = atkCX;
        abiCY = atkCY + atkR + MARGIN + abiR;
        // si no entra en pantalla (dispositivos muy bajos), lo pegamos al borde superior con margen
        float abiCYMax = sh - (MARGIN + abiR);
        if (abiCY > abiCYMax) abiCY = abiCYMax;

        layoutDirty = false;
    }

    public static void invalidateLayout() { layoutDirty = true; }

    public static boolean attackPressedThisFrame() {
        return attackPressedThisFrame;
    }

    // “E” / habilidad
    public static boolean activeAvilityPressed(){
        return activeAvility;
    }

    public static float getJoyCX() { return joyCX; }
    public static float getJoyCY() { return joyCY; }
    public static float getJoyR()  { return joyR;  }
    public static float getAtkCX() { return atkCX; }
    public static float getAtkCY() { return atkCY; }
    public static float getAtkR()  { return atkR;  }

    // --- NUEVOS getters para dibujar el botón “E” ---
    public static float getAbiCX() { return abiCX; }
    public static float getAbiCY() { return abiCY; }
    public static float getAbiR()  { return abiR;  }

    public static boolean isAndroid() {
        return Gdx.app.getType() == Application.ApplicationType.Android;
    }
}
