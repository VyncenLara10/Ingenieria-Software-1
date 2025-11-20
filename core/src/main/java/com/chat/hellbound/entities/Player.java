package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chat.hellbound.objects.SprintBurst;
import com.chat.hellbound.utilz.CameraController.CameraTarget;
import static com.chat.hellbound.utilz.Constants.PlayerConstants.*;
import com.chat.hellbound.input.InputController;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.HelpMethods;
import com.chat.hellbound.utilz.TextureUtils;

public class Player extends Entity implements CameraTarget {

    private TextureRegion[][] grid;
    private int playerAction = IDLE;
    private int aniIndex = 0, aniTick = 0, aniSpeed = ANI_SPEED;
    private float vx = 0f, vy = 0f;
    private boolean facingRight = true;
    private float scale = 2.5f;
    private float BonusSpeed = 1f;

    private final LevelManager levelManager;
    private int[][] lvlData;
    private int tileW, tileH;

    private int hp = MAX_HP;
    private boolean dead = false;

    private final Rectangle attackBox = new Rectangle();
    private float attackCd = 0f;
    private float attackActiveTimer = 0f;
    private boolean attackDealtThisWindow = false;
    private float cooldown = 0f;

    private final Vector2 camFocus = new Vector2();

    private String object = "";

    public Player(float startX, float startY, LevelManager levelManager) {
        this.object = object;
        this.BonusSpeed = BonusSpeed;
        this.cooldown = cooldown;
        this.levelManager = levelManager;
        this.lvlData = levelManager.getLevelData();
        this.tileW = levelManager.getTileWidth();
        this.tileH = levelManager.getTileHeight();
        loadAnimations();
        float hbW = FRAME_W * (scale*0.8f);
        float hbH = FRAME_H * (scale*0.8f);
        initHitbox(startX, startY, hbW, hbH);
    }

    private void loadAnimations() {
        Texture atlas = Assets.getPlayerAtlas();
        TextureUtils.prepareTexture(atlas);
        grid = TextureRegion.split(atlas, FRAME_W, FRAME_H);
        TextureUtils.fixBleeding(grid);
    }

    private TextureRegion getCurrentFrame() {
        int row = clamp(playerAction, 0, grid.length - 1);
        int designed = getSpriteAmount(playerAction);
        int cols = grid[row].length;
        int max = Math.min(designed, cols);
        int col = clamp(aniIndex, 0, max - 1);
        return grid[row][col];
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            int row = clamp(playerAction, 0, grid.length - 1);
            int designed = getSpriteAmount(playerAction);
            int cols = grid[row].length;
            int max = Math.min(designed, cols);
            if (max <= 0) max = cols;
            aniIndex %= max;
        }
    }

    public void setAction(int action) {
        if (action != playerAction) {
            playerAction = action;
            aniIndex = 0;
            aniTick = 0;
        }
    }

    public void update(float dt) {
        float ix = InputController.xAxis;
        float iy = InputController.yAxis;

        vx = ix * (MOVE_SPEED*BonusSpeed);
        vy = iy * (MOVE_SPEED*BonusSpeed);

        if (vx != 0) {
            float nx = hitbox.x + vx * dt;

            if (HelpMethods.CanMoveHere(nx, hitbox.y, hitbox.width, hitbox.height,
                lvlData, tileW, tileH))
            {
                hitbox.x = nx;
            } else {
                vx = 0;
            }
        }

        if (vy != 0) {
            float ny = hitbox.y + vy * dt;

            if (HelpMethods.CanMoveHere(hitbox.x, ny, hitbox.width, hitbox.height,
                lvlData, tileW, tileH))
            {
                hitbox.y = ny;
            } else {
                vy = 0;
            }
        }


        if (ix > 0.1f)  facingRight = true;
        if (ix < -0.1f) facingRight = false;

        int desiredAction = (Math.abs(ix) > 0.05f || Math.abs(iy) > 0.05f) ? RUNNING : IDLE;
        setAction(desiredAction);

        attackCd = Math.max(0f, attackCd - dt);

        if (InputController.attackPressedThisFrame() && attackCd == 0f) {
            attackActiveTimer = 0.22f;
            attackCd = ATTACK_COOLDOWN;
            setAction(ATTACK_1);
            attackDealtThisWindow = false;
        }

        if (attackActiveTimer > 0f) {
            attackActiveTimer -= dt;

            float bw = FRAME_W * scale * 0.90f;
            float bh = FRAME_H * scale * 0.75f;

            float pCenterX = hitbox.x + hitbox.width * 0.5f;
            float pCenterY = hitbox.y + hitbox.height * 0.35f;

            float reach = (hitbox.width * 0.5f) + (bw * 0.5f) - bw * 0.30f;

            float ax = pCenterX + (facingRight ? +reach : -reach) - bw * 0.5f;
            float ay = pCenterY - bh * 0.5f;

            attackBox.set(ax, ay, bw, bh);

            if (attackActiveTimer <= 0f) {
                attackActiveTimer = 0f;
                attackBox.set(0, 0, 0, 0);
            }
        }

        if (InputController.activeAvilityPressed() && cooldown == 0) {
            useObject();
        }

        if (cooldown > 0f) {
            cooldown -= dt;

            if (cooldown <= 8f && object == "SprintBurst") {
                setBonusSpeed(1f);
            }

            if (cooldown <= 0f) {
                cooldown = 0f;
            }
        }

        updateAnimationTick();
    }


    public void render(SpriteBatch batch) {
        TextureRegion frame = getCurrentFrame();
        float w = FRAME_W * scale;
        float h = FRAME_H * scale;
        float scaleX = facingRight ? 1f : -1f;
        batch.draw(frame, hitbox.x, hitbox.y, w * 0.5f, 0f, w, h, scaleX, 1f, 0f);
    }

    public void renderDebug(ShapeRenderer sr) {
        sr.setColor(0f, 1f, 0f, 1f);
        sr.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        if (hasAttackBox()) {
            sr.setColor(1f, 0f, 0f, 1f);
            sr.rect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
        }
    }

    @Override
    public Vector2 getCameraFocus() {
        float w = FRAME_W * scale;
        float h = FRAME_H * scale;
        camFocus.set(hitbox.x + w * 0.5f, hitbox.y + h * 0.5f);
        return camFocus;
    }

    public void applyDamage(int dmg) {
        if (dead) return;
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            dead = true;
        }
    }

    public boolean isDead() { return dead; }
    public int getHp() { return hp; }
    public int getMaxHp() { return MAX_HP; }

    public boolean hasAttackBox() {
        return attackActiveTimer > 0f && attackBox.width > 0 && attackBox.height > 0;
    }

    public Rectangle getAttackBox() {
        return attackBox;
    }

    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    public void setScale(float scale) {
        float cx = hitbox.x + hitbox.width * 0.5f;
        float cy = hitbox.y + hitbox.height * 0.5f;
        this.scale = scale;
        hitbox.setSize(FRAME_W * scale, FRAME_H * scale);
        hitbox.setCenter(cx, cy);
    }

    public Rectangle getHitbox() { return hitbox; }

    public void refreshLevelRefs() {
        this.lvlData = levelManager.getLevelData();
        this.tileW = levelManager.getTileWidth();
        this.tileH = levelManager.getTileHeight();
    }

    public Rectangle consumeAttackBox() {
        if (!hasAttackBox()) return null;
        if (attackDealtThisWindow) return null;
        attackDealtThisWindow = true;
        return new Rectangle(attackBox);
    }

    public void SetObject(String obj){
        this.object = obj;
    }

    public void useObject(){
        if(this.object.equals("SprintBurst")){
            SprintBurst spr = new SprintBurst(this.levelManager,this);
            this.cooldown = 10f;
            spr.use();
        }
    }

    public void setBonusSpeed(float bonus){
        this.BonusSpeed = bonus;
    }

    public void setNetworkPosition(float x, float y) {
        if (hitbox != null) {
            hitbox.x = x;
            hitbox.y = y;
        }
    }

}
