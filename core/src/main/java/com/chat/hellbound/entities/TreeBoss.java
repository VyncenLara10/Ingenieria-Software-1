package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.HelpMethods;
import com.chat.hellbound.utilz.TextureUtils;

import static com.chat.hellbound.utilz.Constants.TreeBossConstants.*;

public class TreeBoss extends Boss {

    private final LevelManager levelManager;
    private final int[][] lvlData;
    private final int tileW, tileH;

    private TextureRegion[][] frames;

    private float attackCooldown = ATTACK_COOLDOWN;
    private float attackLock = 0f;
    private float hitLock = 0f;

    // Escala del jefe
    private float scale = BOSS_SCALE;

    // Hitbox para el ataque
    private final Rectangle attackBox = new Rectangle();

    // Estado de movimiento
    private int action = IDLE;
    private int aniIndex = 0;
    private int aniTick = 0;
    private boolean facingRight = true;
    private boolean dead = false;

    private static final boolean SPRITE_FACES_RIGHT = false;
    private static final float HIT_LOCK_TIME = 0.25f;

    public TreeBoss(float x, float y, LevelManager lm) {
        super(x, y, (int)(FRAME_W * BOSS_SCALE), (int)(FRAME_H * BOSS_SCALE), MAX_HP);
        
        this.levelManager = lm;
        this.lvlData = lm.getLevelData();
        this.tileW = lm.getTileWidth();
        this.tileH = lm.getTileHeight();

        Texture atlas = Assets.getTreeBossAtlas();
        TextureUtils.prepareTexture(atlas);
        frames = TextureRegion.split(atlas, FRAME_W, FRAME_H);
        TextureUtils.fixBleeding(frames);

        this.maxHealth = MAX_HP;
        this.currentHealth = maxHealth;
        this.moveSpeed = MOVE_SPEED;
        
        this.x = x;
        this.y = y;
        this.width = (int)(FRAME_W * scale);
        this.height = (int)(FRAME_H * scale);
    }

    @Override
    public void update(float delta, Player player) {
        if (!isActive) return;
        
        if (dead || isDefeated) {
            action = DEAD;
            updateDeath(delta);
            return;
        }

        attackCooldown = Math.max(0f, attackCooldown - delta);
        attackLock = Math.max(0f, attackLock - delta);
        hitLock = Math.max(0f, hitLock - delta);
        updateFlash(delta);

        if (hitLock > 0f) {
            action = HIT;
            updateAnimation(delta, HIT_COUNT);
            return;
        }

        Vector2 playerPos = EnemyShared.playerCenter();
        float cx = x + width * 0.5f;
        float cy = y + height * 0.5f;
        float dx = playerPos.x - cx;
        float dy = playerPos.y - cy;
        float dist2 = dx*dx + dy*dy;

        float desiredVx = 0f, desiredVy = 0f;

        if (dist2 < AGGRO_RANGE * AGGRO_RANGE) {
            if (Math.abs(dx) > FACE_EPS) facingRight = dx > 0;
            
            float len = (float)Math.sqrt(dist2);
            if (len > 1e-5f) {
                float nx = dx / len;
                float ny = dy / len;
                
                float speedMultiplier = 1.0f;
                if (currentPhase == 2) speedMultiplier = 1.3f;
                if (currentPhase == 3) speedMultiplier = 1.6f;
                
                desiredVx = nx * MOVE_SPEED * speedMultiplier;
                desiredVy = ny * MOVE_SPEED * speedMultiplier;
            }
            
            if (len < ATTACK_RANGE && attackCooldown == 0f && attackLock == 0f) {
                doAttack();
                attackCooldown = ATTACK_COOLDOWN * (currentPhase == 3 ? 0.7f : 1.0f);
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

        // Mover horizontalmente
        float newX = x + desiredVx * delta;
        if (HelpMethods.CanMoveHere(newX, y, width, height, lvlData, tileW, tileH)) {
            x = newX;
        } else {
            x = HelpMethods.GetEntityXPosNextToWall(getHitbox(), desiredVx * delta, lvlData, tileW, tileH);
        }

        // Mover verticalmente
        float newY = y + desiredVy * delta;
        if (HelpMethods.CanMoveHere(x, newY, width, height, lvlData, tileW, tileH)) {
            y = newY;
        } else {
            y = HelpMethods.GetEntityYPosUnderRoofOrAboveFloor(getHitbox(), desiredVy * delta, lvlData, tileW, tileH);
        }

        updateAnimation(delta, getActionFrameCount());
    }

    private void doAttack() {
        action = ATTACK;
        aniIndex = 0;
        aniTick = 0;

        float w = (width) * 0.7f;
        float h = (height) * 0.6f;
        float ax = facingRight ? (x + width) : (x - w);
        float ay = y + height * 0.2f;
        
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
        
        batch.draw(f, x, y, w * 0.5f, 0f, w, h, scaleX, 1f, 0f);
        
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

        float dir = facingRight ? -1f : 1f;
        x += dir * KNOCKBACK * 0.1f;
        
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
    }

    @Override
    public void executeAttack(Player player) {
    }

    @Override
    public Rectangle getHitbox() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isDead() {
        return dead;
    }

    public int getAction() {
        return action;
    }
}
