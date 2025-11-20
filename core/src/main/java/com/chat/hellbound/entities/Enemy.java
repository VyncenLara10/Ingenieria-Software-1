package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Enemy extends Entity {

    protected int maxHp;
    protected int hp;
    protected boolean dead = false;

    protected boolean dying = false;
    protected float deathTimer = 0f;
    protected float deathDuration = 0.6f;

    protected int action = 0;
    protected int aniIndex = 0;
    protected int aniTick = 0;
    protected float aniSpeed = 6;

    protected boolean facingRight = true;
    protected float vx = 0f, vy = 0f;

    public boolean isDead() { return dead; }

    public void applyDamage(int dmg) {
        if (dead) return;
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            startDeath();
        } else {
            onHit();
        }
    }

    protected void startDeath() {
        dead = true;
        dying = true;
        deathTimer = deathDuration;
        aniIndex = 0;
        aniTick = 0;
    }

    protected void updateDeath(float dt, int deathFrames) {
        if (!dying) return;
        deathTimer -= dt;
        updateAnimation(deathFrames);
        if (deathTimer <= 0f) {
            dying = false;
        }
    }

    protected void onHit() {}

    protected void updateAnimation(int framesForAction) {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= framesForAction) aniIndex = 0;
        }
    }

    public abstract void update(float dt);
    public abstract void render(SpriteBatch batch);
    public abstract TextureRegion currentFrame();
}
