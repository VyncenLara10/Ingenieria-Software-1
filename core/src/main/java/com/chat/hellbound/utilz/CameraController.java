package com.chat.hellbound.utilz;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class CameraController {

    public interface CameraTarget {
        Vector2 getCameraFocus();
    }

    private final OrthographicCamera cam;
    private float worldW, worldH;
    private float viewW, viewH;
    private float lerp = 0.22f;
    private final Array<CameraTarget> targets = new Array<>();

    public CameraController(OrthographicCamera cam, float worldW, float worldH, float viewW, float viewH) {
        this.cam = cam;
        setWorldBounds(worldW, worldH);
        setViewportSize(viewW, viewH);
    }

    public void setWorldBounds(float worldW, float worldH) { this.worldW = worldW; this.worldH = worldH; }
    public void setViewportSize(float viewW, float viewH) { this.viewW = viewW; this.viewH = viewH; }
    public void setLerp(float lerp) { this.lerp = Math.max(0f, Math.min(1f, lerp)); }

    public void clearTargets() { targets.clear(); }
    public void setPrimaryTarget(CameraTarget t) { targets.clear(); if (t != null) targets.add(t); }
    public void addTarget(CameraTarget t) { if (t != null) targets.add(t); }

    public void update(float dt) {
        if (targets.size == 0) return;

        float cx = 0f, cy = 0f;
        for (CameraTarget t : targets) {
            Vector2 p = t.getCameraFocus();
            cx += p.x; cy += p.y;
        }
        cx /= targets.size;
        cy /= targets.size;

        float tx = cam.position.x + (cx - cam.position.x) * lerp;
        float ty = cam.position.y + (cy - cam.position.y) * lerp;

        float halfW = viewW * 0.5f, halfH = viewH * 0.5f;
        tx = clamp(tx, halfW, Math.max(halfW, worldW - halfW));
        ty = clamp(ty, halfH, Math.max(halfH, worldH - halfH));

        cam.position.set(tx, ty, 0f);
        cam.update();
    }

    private float clamp(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }
}
