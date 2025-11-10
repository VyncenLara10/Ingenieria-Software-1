package com.chat.hellbound.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Input unificado:
 * - Desktop: WASD / flechas para mover; click izquierdo (o J/Z) para atacar.
 * - Android: joystick virtual (círculo izq) para mover; botón circular (der) para atacar.
 *
 * Consulta:
 *   - InputController.xAxis / yAxis
 *   - InputController.attackPressedThisFrame()
 */
public class InputController {

    public static float xAxis = 0f;
    public static float yAxis = 0f;

    private static boolean attack = false;
    private static boolean attackPressedThisFrame = false;


    private static float joyCX, joyCY, joyR;     // joystick base (círculo izq)
    private static float atkCX, atkCY, atkR;     // botón ataque (círculo der)
    private static final float MARGIN = 24f;     // px

    private static int joyPointer = -1;
    private static int atkPointer = -1;

    private static boolean layoutDirty = true;

    public static void update() {
        attackPressedThisFrame = false;

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            updateLayoutIfNeeded();
            pollTouch();
        } else {
            pollDesktop();
        }
    }

    // -------- PC ----------
    private static void pollDesktop() {
        float x = 0f, y = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  y -= 1f;

        xAxis = x;
        yAxis = y;

        boolean newAttack = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyJustPressed(Input.Keys.J)
            || Gdx.input.isKeyJustPressed(Input.Keys.Z);

        attackPressedThisFrame = newAttack && !attack;
        attack = newAttack || attack;
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            && !Gdx.input.isKeyPressed(Input.Keys.J)
            && !Gdx.input.isKeyPressed(Input.Keys.Z)) {
            attack = false;
        }
    }

    // -------- Android ----------
    private static void pollTouch() {
        float x = 0f, y = 0f;

        final int maxP = 20;
        if (joyPointer != -1 && !Gdx.input.isTouched(joyPointer)) joyPointer = -1;
        if (atkPointer != -1 && !Gdx.input.isTouched(atkPointer)) atkPointer = -1;

        boolean atkJustNow = false;

        for (int p = 0; p < maxP; p++) {
            if (!Gdx.input.isTouched(p)) continue;

            float sx = Gdx.input.getX(p);
            float sy = Gdx.graphics.getHeight() - Gdx.input.getY(p); // origen abajo

            if (joyPointer == -1 && isInside(sx, sy, joyCX, joyCY, joyR)) {
                joyPointer = p;
            }
            if (atkPointer == -1 && isInside(sx, sy, atkCX, atkCY, atkR)) {
                atkPointer = p;
                atkJustNow = true; // press este frame
            }
        }

        if (joyPointer != -1 && Gdx.input.isTouched(joyPointer)) {
            float sx = Gdx.input.getX(joyPointer);
            float sy = Gdx.graphics.getHeight() - Gdx.input.getY(joyPointer);
            float dx = sx - joyCX;
            float dy = sy - joyCY;
            float len = (float)Math.sqrt(dx*dx + dy*dy);
            if (len > 0.0001f) {
                float nx = dx / len;
                float ny = dy / len;
                float m = Math.min(1f, len / joyR); // hasta el borde = 1.0
                x = nx * m;
                y = ny * m;
            }
        }

        xAxis = x;
        yAxis = y;

        boolean newAttack = atkJustNow;
        attackPressedThisFrame = newAttack && !attack;
        attack = newAttack || (atkPointer != -1 && Gdx.input.isTouched(atkPointer));

        if (atkPointer != -1 && !Gdx.input.isTouched(atkPointer)) {
            atkPointer = -1;
            attack = false;
        }
    }

    private static boolean isInside(float x, float y, float cx, float cy, float r) {
        float dx = x - cx, dy = y - cy;
        return (dx*dx + dy*dy) <= r*r;
    }

    private static void updateLayoutIfNeeded() {
        if (!layoutDirty) return;
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        joyR = Math.min(sw, sh) * 0.12f; // radio 12% de la menor dimensión
        joyCX = MARGIN + joyR;
        joyCY = MARGIN + joyR;

        atkR = joyR * 0.9f;
        atkCX = sw - (MARGIN + atkR);
        atkCY = MARGIN + atkR;

        layoutDirty = false;
    }

    public static void invalidateLayout() { layoutDirty = true; }

    public static boolean attackPressedThisFrame() {
        return attackPressedThisFrame;
    }

    public static float getJoyCX() { return joyCX; }
    public static float getJoyCY() { return joyCY; }
    public static float getJoyR()  { return joyR;  }
    public static float getAtkCX() { return atkCX; }
    public static float getAtkCY() { return atkCY; }
    public static float getAtkR()  { return atkR;  }
    public static boolean isAndroid() { return Gdx.app.getType() == Application.ApplicationType.Android; }
}
