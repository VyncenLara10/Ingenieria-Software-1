package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.TextureUtils;

import static com.chat.hellbound.utilz.Constants.TreeBossConstants.*;

/**
 * TreeBoss - Version ULTRA SIMPLE sin HelpMethods complicados
 */
public class TreeBoss extends Boss {

    private final LevelManager levelManager;

    private TextureRegion[][] frames;

    private float attackCooldownTimer = ATTACK_COOLDOWN;
    private float attackLock = 0f;
    private float hitLock = 0f;

    private float scale = BOSS_SCALE;

    private final Rectangle attackBox = new Rectangle();

    // Estados
    private int action = IDLE;
    private int aniIndex = 0;
    private float aniTick = 0;
    private boolean facingRight = true;
    private boolean dead = false;

    private static final boolean SPRITE_FACES_RIGHT = false;
    private static final float HIT_LOCK_TIME = 0.25f;

    public TreeBoss(float x, float y, LevelManager lm) {
        super(x, y, (int)(FRAME_W * BOSS_SCALE), (int)(FRAME_H * BOSS_SCALE), MAX_HP);

        this.levelManager = lm;

        Texture atlas = Assets.getTreeBossAtlas();
        TextureUtils.prepareTexture(atlas);
        frames = TextureRegion.split(atlas, FRAME_W, FRAME_H);
        TextureUtils.fixBleeding(frames);

        this.maxHealth = MAX_HP;
        this.currentHealth = maxHealth;
    }

    @Override
    public void update(float delta, Player player) {
        if (!isActive) return;

        if (dead || isDefeated) {
            action = DEAD;
            updateDeath(delta);
            return;
        }

        // Actualizar timers
        attackCooldownTimer = Math.max(0f, attackCooldownTimer - delta);
        attackLock = Math.max(0f, attackLock - delta);
        hitLock = Math.max(0f, hitLock - delta);
        updateFlash(delta);

        if (hitLock > 0f) {
            action = HIT;
            updateAnimation(delta, HIT_COUNT);
            return;
        }

        // Calcular dirección al jugador
        Vector2 playerPos = EnemyShared.playerCenter();
        float cx = hitbox.x + hitbox.width * 0.5f;
        float cy = hitbox.y + hitbox.height * 0.5f;
        float dx = playerPos.x - cx;
        float dy = playerPos.y - cy;
        float dist2 = dx*dx + dy*dy;

        float desiredVx = 0f, desiredVy = 0f;

        // Comportamiento de persecución
        if (dist2 < AGGRO_RANGE * AGGRO_RANGE) {
            if (Math.abs(dx) > FACE_EPS) facingRight = dx > 0;
            float len = (float)Math.sqrt(dist2);
            if (len > 1e-5f) {
                float nx = dx / len;
                float ny = dy / len;

                // Velocidad aumentada por fase
                float speedMultiplier = 1.0f;
                if (currentPhase == 2) speedMultiplier = 1.3f;
                if (currentPhase == 3) speedMultiplier = 1.6f;

                desiredVx = nx * MOVE_SPEED * speedMultiplier;
                desiredVy = ny * MOVE_SPEED * speedMultiplier;
            }
            if (len < ATTACK_RANGE && attackCooldownTimer == 0f && attackLock == 0f) {
                doAttack();
                attackCooldownTimer = ATTACK_COOLDOWN * (currentPhase == 3 ? 0.7f : 1.0f);
                attackLock = ATTACK_LOCK_TIME;
            }
        }

        if (attackLock > 0f) {
            action = ATTACK;
            desiredVx = 0f;
            desiredVy = 0f;
        } else {
            action = (Math.abs(desiredVx) > 0.01f || Math.abs(desiredVy) > 0.01f) ? RUN : IDLE;
        }

        // MOVIMIENTO SIMPLE - sin colisiones complicadas
        hitbox.x += desiredVx * delta;
        hitbox.y += desiredVy * delta;

        updateAnimation(delta, getActionFrameCount());
    }

    private void doAttack() {
        action = ATTACK;
        aniIndex = 0;
        aniTick = 0;
        float w = (FRAME_W * scale) * 0.7f;
        float h = (FRAME_H * scale) * 0.6f;
        float ax = facingRight ? (hitbox.x + hitbox.width) : (hitbox.x - w);
        float ay = hitbox.y + hitbox.height * 0.25f;
        attackBox.set(ax, ay, w, h);
        EnemyShared.queueEnemyAttack(this, attackBox, DAMAGE);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isActive) return;

        TextureRegion f = currentFrame();
        float w = FRAME_W * scale;
        float h = FRAME_H * scale;
        boolean wantRight = facingRight;
        float scaleX = (SPRITE_FACES_RIGHT ? (wantRight ? 1f : -1f) : (wantRight ? -1f : 1f));

        if (isFlashing) {
            batch.setColor(1f, 0.3f, 0.3f, 1f);
        }

        batch.draw(f, hitbox.x, hitbox.y, w * 0.5f, 0f, w, h, scaleX, 1f, 0f);

        if (isFlashing) {
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private TextureRegion currentFrame() {
        int row, count;
        switch (action) {
            case RUN:    row = RUN_ROW;    count = RUN_COUNT;    break;
            case ATTACK: row = ATTACK_ROW; count = ATTACK_COUNT; break;
            case HIT:    row = HIT_ROW;    count = HIT_COUNT;    break;
            case DEAD:   row = DEAD_ROW;   count = DEAD_COUNT;   break;
            case IDLE:
            default:     row = IDLE_ROW;   count = IDLE_COUNT;   break;
        }
        row = Math.max(0, Math.min(row, frames.length - 1));
        int cols = frames[row].length;
        int col = aniIndex % Math.max(1, Math.min(count, cols));
        return frames[row][col];
    }

    private int getActionFrameCount() {
        switch (action) {
            case RUN:    return RUN_COUNT;
            case ATTACK: return ATTACK_COUNT;
            case HIT:    return HIT_COUNT;
            case DEAD:   return DEAD_COUNT;
            case IDLE:
            default:     return IDLE_COUNT;
        }
    }

    private void updateAnimation(float delta, int frameCount) {
        aniTick += delta * ANI_SPEED * 60f;
        if (aniTick >= 1f) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= frameCount) {
                aniIndex = 0;
            }
        }
    }

    private void updateDeath(float delta) {
        aniTick += delta * ANI_SPEED * 60f;
        if (aniTick >= 1f) {
            aniTick = 0;
            if (aniIndex < DEAD_COUNT - 1) {
                aniIndex++;
            }
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (isDefeated || dead) return;

        currentHealth -= damage;
        isFlashing = true;
        flashTimer = 0.2f;

        // Knockback
        float dir = facingRight ? -1f : 1f;
        hitbox.x += dir * KNOCKBACK * 0.1f;

        action = HIT;
        aniIndex = 0;
        aniTick = 0;
        hitLock = HIT_LOCK_TIME;

        if (currentHealth <= 0) {
            currentHealth = 0;
            isDefeated = true;
            dead = true;
            onDefeat();
        } else {
            updatePhase();
        }
    }

    @Override
    protected void onDefeat() {
        dead = true;
        action = DEAD;
        aniIndex = 0;
        aniTick = 0;
        System.out.println("¡Tree Boss derrotado!");
    }

    @Override
    public void selectAttackPattern() {
        // Simple
    }

    @Override
    public void executeAttack(Player player) {
        // Se ejecuta en doAttack()
    }

    public boolean isDead() {
        return dead;
    }

    public int getAction() {
        return action;
    }
}
