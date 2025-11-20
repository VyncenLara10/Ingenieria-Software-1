package com.chat.hellbound.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.chat.hellbound.input.InputController;

public class TouchControls {

    private final ShapeRenderer sr = new ShapeRenderer();
    private final Matrix4 proj = new Matrix4();

    private final float outerA = 0.25f;
    private final float innerA = 0.50f;
    private final float shadowOffset = 6f;
    private final float shadowA = 0.25f;

    private final Vector3 touchTmp = new Vector3();

    public void render() {
        if (!InputController.isAndroid()) return;

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        proj.setToOrtho2D(0, 0, screenW, screenH);
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

        float axisX = InputController.xAxis;
        float axisY = InputController.yAxis;

        float stickX = jx;
        float stickY = jy;

        if (InputController.isJoyActive()
            && (Math.abs(axisX) > 0.001f || Math.abs(axisY) > 0.001f)) {

            float maxOffset = jr * 0.55f;

            stickX = jx + axisX * maxOffset;
            stickY = jy + axisY * maxOffset;
        }

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(0, 0, 0, shadowA);
        sr.circle(jx + shadowOffset, jy - shadowOffset, jr + 4f);

        sr.setColor(1f, 1f, 1f, outerA);
        sr.circle(jx, jy, jr);

        sr.setColor(1f, 1f, 1f, innerA);
        sr.circle(stickX, stickY, jr * 0.40f);

        sr.setColor(0, 0, 0, shadowA);
        sr.circle(ax + shadowOffset, ay - shadowOffset, ar + 4f);

        sr.setColor(1f, 0.35f, 0.35f, 0.35f);
        sr.circle(ax, ay, ar);

        sr.setColor(1f, 0.6f, 0.6f, 0.40f);
        sr.circle(ax, ay, ar * 0.65f);

        sr.setColor(0, 0, 0, shadowA);
        sr.circle(ex + shadowOffset, ey - shadowOffset, er + 4f);

        sr.setColor(0.35f, 0.35f, 1f, 0.35f);
        sr.circle(ex, ey, er);

        sr.setColor(0.65f, 0.65f, 1f, 0.40f);
        sr.circle(ex, ey, er * 0.65f);

        sr.end();
    }

    public void dispose() {
        sr.dispose();
    }
}
