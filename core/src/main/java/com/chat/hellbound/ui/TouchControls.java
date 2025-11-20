package com.chat.hellbound.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.chat.hellbound.input.InputController;

public class TouchControls {

    private final ShapeRenderer sr = new ShapeRenderer();
    private final Matrix4 proj = new Matrix4();

    private final float outerA = 0.28f;
    private final float innerA = 0.75f;
    private final float shadowOffset = 8f;
    private final float shadowA = 0.22f;

    public void render() {
        if (!InputController.isAndroid()) return;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        proj.setToOrtho2D(0, 0, sw, sh);
        sr.setProjectionMatrix(proj);

        // --- READ POSITIONS FROM INPUT ---
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


        // Move RIGHT (much more than before)
        ax += sw * 0.14f;
        ex += sw * 0.18f;

        // Move DOWN slightly
        ay -= sh * 0.04f;
        ey -= sh * 0.08f;

        // Make buttons SMALLER (only render scale)
        float renderAR = ar * 0.70f;
        float renderER = er * 0.68f;

        // Joystick stick stays as is
        float stickX = jx;
        float stickY = jy;

        if (InputController.isJoyActive() &&
            (Math.abs(axisX) > 0.01f || Math.abs(axisY) > 0.01f)) {

            float maxOffset = jr * 0.55f;
            stickX = jx + axisX * maxOffset;
            stickY = jy + axisY * maxOffset;
        }

        sr.begin(ShapeRenderer.ShapeType.Filled);


        sr.setColor(0, 0, 0, shadowA);
        sr.circle(jx + shadowOffset, jy - shadowOffset, jr + 6);

        sr.setColor(0.95f, 0.95f, 0.95f, 0.32f);
        sr.circle(jx, jy, jr);

        sr.setColor(0.20f, 0.70f, 1f, 0.85f);
        sr.circle(stickX, stickY, jr * 0.40f);



        sr.setColor(0, 0, 0, shadowA);
        sr.circle(ax + shadowOffset, ay - shadowOffset, renderAR + 4);

        sr.setColor(1f, 0.25f, 0.25f, 0.45f);
        sr.circle(ax, ay, renderAR);

        sr.setColor(1f, 0.55f, 0.55f, 0.50f);
        sr.circle(ax, ay, renderAR * 0.60f);



        sr.setColor(0, 0, 0, shadowA);
        sr.circle(ex + shadowOffset, ey - shadowOffset, renderER + 4);

        sr.setColor(0.35f, 0.45f, 1f, 0.40f);
        sr.circle(ex, ey, renderER);

        sr.setColor(0.60f, 0.70f, 1f, 0.52f);
        sr.circle(ex, ey, renderER * 0.60f);

        sr.end();
    }


    public void dispose() {
        sr.dispose();
    }
}
