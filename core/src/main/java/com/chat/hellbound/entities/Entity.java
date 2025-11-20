package com.chat.hellbound.entities;

import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {

    protected Rectangle hitbox;

    protected void initHitbox(float x, float y, float width, float height) {
        hitbox = new Rectangle(x, y, width, height);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public float getX(){ return hitbox.x; }
    public float getY(){ return hitbox.y; }
    public float getW(){ return hitbox.width; }
    public float getH(){ return hitbox.height; }
}
