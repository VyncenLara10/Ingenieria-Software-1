package com.chat.hellbound.ui;

import com.badlogic.gdx.Gdx;
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

        float ex = InputController.getAbiCX();
        float ey = InputController.getAbiCY();
        float er = InputController.getAbiR();

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(0, 0, 0, 0.25f);
        sr.circle(jx + 6, jy - 6, jr + 4);

        sr.setColor(1, 1, 1, 0.20f);
        sr.circle(jx, jy, jr);

        sr.setColor(1, 1, 1, 0.45f);
        sr.circle(jx, jy, jr * 0.45f);

        sr.setColor(0, 0, 0, 0.25f);
        sr.circle(ax + 6, ay - 6, ar + 4);

        sr.setColor(1, 0.3f, 0.3f, 0.30f);
        sr.circle(ax, ay, ar);

        sr.setColor(1, 0.6f, 0.6f, 0.35f);
        sr.circle(ax, ay, ar * 0.65f);


        sr.setColor(0, 0, 0, 0.25f);
        sr.circle(ex + 6, ey - 6, er + 4);

        sr.setColor(0.3f, 0.3f, 1f, 0.30f);
        sr.circle(ex, ey, er);

        sr.setColor(0.6f, 0.6f, 1f, 0.35f);
        sr.circle(ex, ey, er * 0.65f);

        sr.end();
    }

    public void dispose() {
        sr.dispose();
    }
}
