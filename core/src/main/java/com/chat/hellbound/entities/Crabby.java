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

import static com.chat.hellbound.utilz.Constants.EnemyConstants.*;

public class Crabby extends Enemy {

    private final LevelManager levelManager;
    private final int[][] lvlData;
    private final int tileW, tileH;

    private TextureRegion[][] frames;

    private float attackCooldown = 2f;
    private float attackLock = 0f;
    private float hitLock = 0f;

    private float scale = 2.5f;

    private final Rectangle attackBox = new Rectangle();

    private static final boolean SPRITE_FACES_RIGHT = false;
    private static final float HIT_LOCK_TIME = 0.25f;

    public Crabby(float x, float y, LevelManager lm) {
        this.levelManager = lm;
        this.lvlData = lm.getLevelData();
        this.tileW = lm.getTileWidth();
        this.tileH = lm.getTileHeight();

        Texture atlas = Assets.getCrabbyAtlas();
        TextureUtils.prepareTexture(atlas);
        frames = TextureRegion.split(atlas, FRAME_W, FRAME_H);
        TextureUtils.fixBleeding(frames);

        this.maxHp = MAX_HP;
        this.hp = maxHp;
        this.aniSpeed = ANI_SPEED;

        initHitbox(x, y, FRAME_W * scale, FRAME_H * scale);
    }

    @Override
    public void update(float dt) {
        if (dead) {
            action = DEAD;
            updateDeath(dt, DEAD_COUNT);
            return;
        }

        attackCooldown = Math.max(0f, attackCooldown - dt);
        attackLock = Math.max(0f, attackLock - dt);
        hitLock = Math.max(0f, hitLock - dt);

        if (hitLock > 0f) {
            action = HIT;
            updateAnimation(HIT_COUNT);
            return;
        }

        Vector2 playerPos = EnemyShared.playerCenter();
        float cx = hitbox.x + hitbox.width * 0.5f;
        float cy = hitbox.y + hitbox.height * 0.5f;
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
                desiredVx = nx * MOVE_SPEED;
                desiredVy = ny * MOVE_SPEED;
            }
            if (len < ATTACK_RANGE && attackCooldown == 0f && attackLock == 0f) {
                doAttack();
                attackCooldown = ATTACK_COOLDOWN;
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

        float newX = hitbox.x + desiredVx * dt;
        if (HelpMethods.CanMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, lvlData, tileW, tileH)) {
            hitbox.x = newX;
        } else {
            hitbox.x = HelpMethods.GetEntityXPosNextToWall(hitbox, desiredVx * dt, lvlData, tileW, tileH);
        }

        float newY = hitbox.y + desiredVy * dt;
        if (HelpMethods.CanMoveHere(hitbox.x, newY, hitbox.width, hitbox.height, lvlData, tileW, tileH)) {
            hitbox.y = newY;
        } else {
            hitbox.y = HelpMethods.GetEntityYPosUnderRoofOrAboveFloor(hitbox, desiredVy * dt, lvlData, tileW, tileH);
        }

        updateAnimation(getActionFrameCount());
    }

    private void doAttack() {
        action = ATTACK;
        aniIndex = 0;
        aniTick = 0;
        float w = (FRAME_W * scale) * 0.6f;
        float h = (FRAME_H * scale) * 0.5f;
        float ax = facingRight ? (hitbox.x + hitbox.width) : (hitbox.x - w);
        float ay = hitbox.y + hitbox.height * 0.25f;
        attackBox.set(ax, ay, w, h);
        EnemyShared.queueEnemyAttack(this, attackBox, DAMAGE);
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion f = currentFrame();
        float w = FRAME_W * scale;
        float h = FRAME_H * scale;
        boolean wantRight = facingRight;
        float scaleX = (SPRITE_FACES_RIGHT ? (wantRight ? 1f : -1f) : (wantRight ? -1f : 1f));
        batch.draw(f, hitbox.x, hitbox.y, w * 0.5f, 0f, w, h, scaleX, 1f, 0f);
    }

    @Override
    public TextureRegion currentFrame() {
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

    @Override
    protected void onHit() {
        float dir = facingRight ? -1f : 1f;
        hitbox.x += dir * KNOCKBACK * 0.1f;
        action = HIT;
        aniIndex = 0;
        aniTick = 0;
        hitLock = HIT_LOCK_TIME;
    }

    public void setScale(float scale) {
        float cx = hitbox.x + hitbox.width * 0.5f;
        float cy = hitbox.y + hitbox.height * 0.5f;
        this.scale = scale;
        hitbox.setSize(FRAME_W * scale, FRAME_H * scale);
        hitbox.setCenter(cx, cy);
    }
}
