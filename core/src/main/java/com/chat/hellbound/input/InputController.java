package com.chat.hellbound.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {
    public static float xAxis = 0f;
    public static float yAxis = 0f;

    public static boolean left, right, up, down, attack;

    public static void update() {
        float x = 0f, y = 0f;

        left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        up    = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        down  = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        attack= Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isTouched(2); // ejemplo

        if (left)  x -= 1f;
        if (right) x += 1f;
        if (down)  y -= 1f;
        if (up)    y += 1f;

        if (Gdx.input.isTouched()) {
            float tx = Gdx.input.getX();
            float ty = Gdx.input.getY();
            float w  = Gdx.graphics.getWidth();
            float h  = Gdx.graphics.getHeight();

            if (tx < w * 0.33f) x -= 1f;
            else if (tx > w * 0.66f) x += 1f;

            if (ty < h * 0.33f) y += 1f;
            else if (ty > h * 0.66f) y -= 1f;
        }

        xAxis = clamp(x, -1f, 1f);
        yAxis = clamp(y, -1f, 1f);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
