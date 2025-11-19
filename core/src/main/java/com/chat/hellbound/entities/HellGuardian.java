package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.Constants;

import java.util.ArrayList;
import java.util.List;

public class HellGuardian extends Boss {

    // Visual properties
    private Color bossColor;
    private float pulseTimer;
    private TextureRegion sprite;

    // Attack properties
    private List<Projectile> projectiles;
    private float dashSpeed;
    private boolean isDashing;
    private float dashTimer;
    private float originalX, originalY;
    private float velocityX, velocityY; // Para el dash

    // Slam attack
    private boolean isSlamming;
    private float slamTimer;
    private float slamHeight;

    public HellGuardian(float x, float y) {
        super(x, y, 96, 96, 1000); // 1000 HP
        this.moveSpeed = 50f;
        this.bossColor = new Color(0.8f, 0.2f, 0.1f, 1f);
        this.projectiles = new ArrayList<>();
        this.dashSpeed = 400f;
        this.isDashing = false;
        this.isSlamming = false;
        this.pulseTimer = 0;
        this.originalX = x;
        this.originalY = y;
        this.velocityX = 0;
        this.velocityY = 0;

        // Obtener un sprite de Crabby como placeholder
        Texture atlas = Assets.getCrabbyAtlas();
        this.sprite = new TextureRegion(atlas, 0, 0, 72, 33);
    }

    @Override
    public void update(float delta, Player player) {
        if (isDefeated || !isActive) return;

        pulseTimer += delta;
        updateFlash(delta);

        // Update attack timer
        attackTimer -= delta;

        // Execute current attack
        if (isDashing) {
            updateDash(delta, player);
        } else if (isSlamming) {
            updateSlam(delta, player);
        } else if (attackTimer <= 0) {
            selectAttackPattern();
            executeAttack(player);
            attackTimer = attackCooldown;
        }

        // Update projectiles
        updateProjectiles(delta, player);

        // Gentle hover movement
        if (!isDashing && !isSlamming) {
            y = originalY + (float) Math.sin(pulseTimer * 2) * 10;
            hitbox.y = y;
        }
    }

    @Override
    public void selectAttackPattern() {
        // Phase-based attack selection
        if (currentPhase == 1) {
            attackPattern = MathUtils.random(0, 2); // Basic attacks
        } else if (currentPhase == 2) {
            attackPattern = MathUtils.random(0, 3); // Add dash
        } else {
            attackPattern = MathUtils.random(0, 4); // All attacks including slam
        }
    }

    @Override
    public void executeAttack(Player player) {
        switch (attackPattern) {
            case 0:
                fireballAttack(player);
                break;
            case 1:
                spreadShotAttack(player);
                break;
            case 2:
                circularAttack();
                break;
            case 3:
                dashAttack(player);
                break;
            case 4:
                groundSlamAttack();
                break;
        }
    }

    private void fireballAttack(Player player) {
        // Single aimed fireball
        Rectangle playerHB = player.getHitbox();
        float playerX = playerHB.x + playerHB.width / 2;
        float playerY = playerHB.y + playerHB.height / 2;
        float angle = (float) Math.atan2(playerY - y, playerX - x);
        projectiles.add(new Projectile(x + width/2, y + height/2, angle, 200f, 20));
    }

    private void spreadShotAttack(Player player) {
        // 5 projectiles in a spread
        Rectangle playerHB = player.getHitbox();
        float playerX = playerHB.x + playerHB.width / 2;
        float playerY = playerHB.y + playerHB.height / 2;
        float baseAngle = (float) Math.atan2(playerY - y, playerX - x);
        for (int i = -2; i <= 2; i++) {
            float angle = baseAngle + (i * 0.3f);
            projectiles.add(new Projectile(x + width/2, y + height/2, angle, 180f, 15));
        }
    }

    private void circularAttack() {
        // Ring of projectiles
        int count = currentPhase * 8; // More projectiles in later phases
        for (int i = 0; i < count; i++) {
            float angle = (float) (i * 2 * Math.PI / count);
            projectiles.add(new Projectile(x + width/2, y + height/2, angle, 150f, 12));
        }
    }

    private void dashAttack(Player player) {
        isDashing = true;
        dashTimer = 0.8f;
        // Calculate dash direction towards player
        Rectangle playerHB = player.getHitbox();
        float playerX = playerHB.x + playerHB.width / 2;
        float playerY = playerHB.y + playerHB.height / 2;
        float angle = (float) Math.atan2(playerY - y, playerX - x);
        velocityX = (float) Math.cos(angle) * dashSpeed;
        velocityY = (float) Math.sin(angle) * dashSpeed;
    }

    private void groundSlamAttack() {
        isSlamming = true;
        slamTimer = 1.0f;
        slamHeight = y + 200;
    }

    private void updateDash(float delta, Player player) {
        dashTimer -= delta;

        x += velocityX * delta;
        y += velocityY * delta;
        hitbox.x = x;
        hitbox.y = y;

        if (dashTimer <= 0) {
            isDashing = false;
            velocityX = 0;
            velocityY = 0;
        }

        // Check collision with player during dash
        if (getHitbox().overlaps(player.getHitbox())) {
            // El jugador recibirá daño del sistema de colisión
            isDashing = false;
        }
    }

    private void updateSlam(float delta, Player player) {
        slamTimer -= delta;

        if (slamTimer > 0.5f) {
            // Rising phase
            y += 300 * delta;
            hitbox.y = y;
        } else if (slamTimer > 0) {
            // Falling phase
            y -= 600 * delta;
            hitbox.y = y;
        } else {
            // Impact
            isSlamming = false;
            y = originalY;
            hitbox.y = y;

            // Damage player if close
            Rectangle playerHB = player.getHitbox();
            float playerX = playerHB.x + playerHB.width / 2;
            float playerY = playerHB.y + playerHB.height / 2;
            float distance = (float) Math.sqrt(
                Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2)
            );
            if (distance < 150) {
                // El daño se maneja por el sistema de colisión
            }

            // Create shockwave projectiles
            for (int i = 0; i < 12; i++) {
                float angle = (float) (i * 2 * Math.PI / 12);
                projectiles.add(new Projectile(x + width/2, y, angle, 200f, 15));
            }
        }
    }

    private void updateProjectiles(float delta, Player player) {
        List<Projectile> toRemove = new ArrayList<>();

        for (Projectile proj : projectiles) {
            proj.update(delta);

            // Check collision with player
            if (proj.getHitbox().overlaps(player.getHitbox())) {
                // El daño se maneja por el sistema de colisión del juego
                toRemove.add(proj);
            }

            // Remove if out of bounds
            if (proj.isOutOfBounds()) {
                toRemove.add(proj);
            }
        }

        projectiles.removeAll(toRemove);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isActive) return;

        // Determine boss color based on state
        Color renderColor = bossColor;
        if (isFlashing) {
            renderColor = Color.WHITE;
        } else if (isEnraged) {
            renderColor = new Color(1f, 0.1f, 0f, 1f);
        }

        // Pulsing effect
        float scale = 1f + (float) Math.sin(pulseTimer * 4) * 0.05f;

        // Draw boss
        batch.setColor(renderColor);
        batch.draw(sprite,
            x - (width * scale - width) / 2,
            y - (height * scale - height) / 2,
            width * scale,
            height * scale);

        // Draw projectiles
        batch.setColor(new Color(1f, 0.5f, 0f, 1f));
        for (Projectile proj : projectiles) {
            batch.draw(sprite,
                proj.x - 8, proj.y - 8, 16, 16);
        }

        batch.setColor(Color.WHITE);

        // Draw health bar
        drawHealthBar(batch);
    }

    private void drawHealthBar(SpriteBatch batch) {
        float barWidth = 200;
        float barHeight = 20;
        float barX = x + width/2 - barWidth/2;
        float barY = y + height + 20;

        // Background (usar un pixel blanco del sprite)
        batch.setColor(Color.DARK_GRAY);
        batch.draw(sprite, barX, barY, barWidth, barHeight);

        // Health
        float healthPercent = (float) currentHealth / maxHealth;
        Color healthColor = Color.RED;
        if (healthPercent > 0.66f) healthColor = Color.GREEN;
        else if (healthPercent > 0.33f) healthColor = Color.YELLOW;

        batch.setColor(healthColor);
        batch.draw(sprite, barX, barY, barWidth * healthPercent, barHeight);

        batch.setColor(Color.WHITE);
    }

    @Override
    protected void onDefeat() {
        projectiles.clear();
        // Play defeat animation/sound
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    // Inner class for projectiles
    public static class Projectile {
        float x, y;
        float velocityX, velocityY;
        int damage;
        float lifetime;

        public Projectile(float x, float y, float angle, float speed, int damage) {
            this.x = x;
            this.y = y;
            this.velocityX = (float) Math.cos(angle) * speed;
            this.velocityY = (float) Math.sin(angle) * speed;
            this.damage = damage;
            this.lifetime = 5f;
        }

        public void update(float delta) {
            x += velocityX * delta;
            y += velocityY * delta;
            lifetime -= delta;
        }

        public Rectangle getHitbox() {
            return new Rectangle(x - 8, y - 8, 16, 16);
        }

        public boolean isOutOfBounds() {
            return lifetime <= 0 || x < -100 || x > Constants.GAME_WIDTH + 100
                || y < -100 || y > Constants.GAME_HEIGHT + 100;
        }
    }
}
