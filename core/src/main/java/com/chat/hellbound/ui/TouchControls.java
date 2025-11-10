package com.chat.hellbound.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.chat.hellbound.input.InputController;

public class TouchControls {

    private final ShapeRenderer sr = new ShapeRenderer();
    private final Matrix4 proj = new Matrix4();

    public void render() {
        if (!InputController.isAndroid()) return;

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        proj.setToOrtho2D(0, 0, w, h);
        sr.setProjectionMatrix(proj);

        float jx = InputController.getJoyCX();
        float jy = InputController.getJoyCY();
        float jr = InputController.getJoyR();

        float ax = InputController.getAtkCX();
        float ay = InputController.getAtkCY();
        float ar = InputController.getAtkR();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(1f, 1f, 1f, 0.12f);
        sr.circle(jx, jy, jr);

        sr.setColor(1f, 1f, 1f, 0.25f);
        sr.circle(jx, jy, jr * 0.45f);

        sr.setColor(1f, 0.2f, 0.2f, 0.18f);
        sr.circle(ax, ay, ar);
        sr.setColor(1f, 0.2f, 0.2f, 0.32f);
        sr.circle(ax, ay, ar * 0.55f);
        sr.end();
    }

    public void dispose() { sr.dispose(); }
}
