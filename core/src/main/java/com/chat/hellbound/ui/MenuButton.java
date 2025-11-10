package com.chat.hellbound.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MenuButton {

    private final Rectangle bounds;
    private final String text;
    private final Viewport viewport;
    private final Vector3 tmp = new Vector3();

    public MenuButton(Viewport viewport, float x, float y, float w, float h, String text) {
        this.viewport = viewport;
        this.bounds = new Rectangle(x, y, w, h);
        this.text = text;
    }

    public Rectangle getBounds() { return bounds; }
    public String getText() { return text; }

    public void render(ShapeRenderer sr) {
        sr.setColor(isHover() ? 0.25f : 0.15f, 0.25f, 0.35f, 1f);
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean isClicked() {
        if (Gdx.input.justTouched()) {
            tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            viewport.unproject(tmp);
            return bounds.contains(tmp.x, tmp.y);
        }
        return false;
    }

    public boolean isHover() {
        tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(tmp);
        return bounds.contains(tmp.x, tmp.y);
    }
}
