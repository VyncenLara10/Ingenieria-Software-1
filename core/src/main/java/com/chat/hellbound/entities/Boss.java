package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Boss extends Entity {
    protected int maxHealth;
    protected int currentHealth;
    protected boolean isDefeated;
    protected boolean isActive;

    // Si queres quitamos las fases
    protected int currentPhase;
    protected float phaseThreshold;

    protected float attackCooldown;
    protected float attackTimer;
    protected int attackPattern;

    protected float moveSpeed;
    protected boolean isEnraged;
    // Esto si me da tiempo de arreglar el sprite
    protected float flashTimer;
    protected boolean isFlashing;

    public Boss(float x, float y, int width, int height, int maxHealth) {
        super(x, y, width, height);
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.isDefeated = false;
        this.isActive = false;
        this.currentPhase = 1;
        this.attackCooldown = 2.0f;
        this.attackTimer = 0;
        this.attackPattern = 0;
        this.flashTimer = 0;
        this.isFlashing = false;
    }

    public abstract void update(float delta, Player player);
    public abstract void render(SpriteBatch batch);
    public abstract void selectAttackPattern();
    public abstract void executeAttack(Player player);

    protected void updatePhase() {
        float healthPercent = (float) currentHealth / maxHealth;

        if (healthPercent <= 0.66f && currentPhase == 1) {
            currentPhase = 2;
            onPhaseChange();
        } else if (healthPercent <= 0.33f && currentPhase == 2) {
            currentPhase = 3;
            isEnraged = true;
            onPhaseChange();
        }
    }

    protected void onPhaseChange() {
        attackCooldown *= 0.8f;
    }

    public void takeDamage(int damage) {
        if (isDefeated) return;

        currentHealth -= damage;
        isFlashing = true;
        flashTimer = 0.2f;

        if (currentHealth <= 0) {
            currentHealth = 0;
            isDefeated = true;
            onDefeat();
        } else {
            updatePhase();
        }
    }

    protected void onDefeat() {
    }

    protected void updateFlash(float delta) {
        if (isFlashing) {
            flashTimer -= delta;
            if (flashTimer <= 0) {
                isFlashing = false;
            }
        }
    }

    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDefeated() { return isDefeated; }
    public boolean isActive() { return isActive; }
    public int getCurrentPhase() { return currentPhase; }
    public boolean isFlashing() { return isFlashing; }

    public void setActive(boolean active) { this.isActive = active; }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, width, height);
    }
}
