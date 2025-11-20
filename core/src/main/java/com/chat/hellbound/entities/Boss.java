package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Boss extends Entity {
    protected int maxHealth;
    protected int currentHealth;
    protected boolean isDefeated;
    protected boolean isActive;

    // Boss phases
    protected int currentPhase;
    protected float phaseThreshold;

    // Attack patterns
    protected float attackCooldown;
    protected float attackTimer;
    protected int attackPattern;

    // Movement
    protected float moveSpeed;
    protected boolean isEnraged;

    // Visual effects
    protected float flashTimer;
    protected boolean isFlashing;

    // Propiedades adicionales para compatibilidad
    protected float x, y;
    protected int width, height;

    public Boss(float x, float y, int width, int height, int maxHealth) {
        // Inicializar el hitbox usando el método de Entity
        initHitbox(x, y, width, height);

        // Guardar las coordenadas y dimensiones para uso interno
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

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
        // Override in subclasses for phase-specific behavior
        attackCooldown *= 0.8f; // Attacks become faster
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
        // Override in subclasses
    }

    protected void updateFlash(float delta) {
        if (isFlashing) {
            flashTimer -= delta;
            if (flashTimer <= 0) {
                isFlashing = false;
            }
        }
    }

    // Métodos para actualizar posición y sincronizar con hitbox
    protected void updatePosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
        if (hitbox != null) {
            hitbox.x = newX;
            hitbox.y = newY;
        }
    }

    protected void setPosition(float newX, float newY) {
        updatePosition(newX, newY);
    }

    // Getters
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDefeated() { return isDefeated; }
    public boolean isActive() { return isActive; }
    public int getCurrentPhase() { return currentPhase; }
    public boolean isFlashing() { return isFlashing; }

    // Getters para posición y tamaño
    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // Setters
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public Rectangle getHitbox() {
        return hitbox;
    }
}
