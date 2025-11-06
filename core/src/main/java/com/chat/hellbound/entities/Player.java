package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import com.chat.hellbound.utilz.CameraController.CameraTarget;
import static com.chat.hellbound.utilz.Constants.PlayerConstants.*;

import com.chat.hellbound.input.InputController;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.HelpMethods;
import com.chat.hellbound.levels.LevelManager;

public class Player extends Entity implements CameraTarget {

    private TextureRegion[][] grid;
    private int playerAction = IDLE;

    private int aniIndex = 0;
    private int aniTick  = 0;
    private int aniSpeed = ANI_SPEED;

    private float vx, vy;
    private boolean facingRight = true;

    private float scale = 2.50f;

    private final LevelManager levelManager;
    private int[][] lvlData;
    private int tileW, tileH;

    private final Vector2 camFocus = new Vector2();

    public Player(float startX, float startY, LevelManager levelManager) {
        this.levelManager = levelManager;
        this.lvlData = levelManager.getLevelData();
        this.tileW = levelManager.getTileWidth();
        this.tileH = levelManager.getTileHeight();

        loadAnimations();

        float hbW = FRAME_W * scale;
        float hbH = FRAME_H * scale;
        initHitbox(startX, startY, hbW, hbH);
    }

    private void loadAnimations() {
        Texture atlas = Assets.getPlayerAtlas();
        grid = TextureRegion.split(atlas, FRAME_W, FRAME_H);
    }

    public void update(float dt) {
        float ix = InputController.xAxis;
        float iy = InputController.yAxis;

        vx = ix * MOVE_SPEED;
        vy = iy * MOVE_SPEED;

        float newX = hitbox.x + vx * dt;
        if (HelpMethods.CanMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, lvlData, tileW, tileH)) {
            hitbox.x = newX;
        } else {
            hitbox.x = HelpMethods.GetEntityXPosNextToWall(hitbox, vx * dt, lvlData, tileW, tileH);
            vx = 0f;
        }

        float newY = hitbox.y + vy * dt;
        if (HelpMethods.CanMoveHere(hitbox.x, newY, hitbox.width, hitbox.height, lvlData, tileW, tileH)) {
            hitbox.y = newY;
        } else {
            hitbox.y = HelpMethods.GetEntityYPosUnderRoofOrAboveFloor(hitbox, vy * dt, lvlData, tileW, tileH);
            vy = 0f;
        }

        if (ix > 0.1f)  facingRight = true;
        if (ix < -0.1f) facingRight = false;

        int desiredAction = (Math.abs(ix) > 0.05f || Math.abs(iy) > 0.05f) ? RUNNING : IDLE;
        setAction(desiredAction);

        updateAnimationTick();
    }

    public void render(SpriteBatch batch) {
        TextureRegion frame = getCurrentFrame();

        float w = FRAME_W * scale;
        float h = FRAME_H * scale;

        if (facingRight) {
            batch.draw(frame, hitbox.x, hitbox.y, w, h);
        } else {
            batch.draw(frame, hitbox.x + w, hitbox.y, -w, h);
        }
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

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    public Vector2 getCameraFocus() {
        float w = FRAME_W * scale;
        float h = FRAME_H * scale;
        camFocus.set(hitbox.x + w * 0.5f, hitbox.y + h * 0.5f);
        return camFocus;
    }

    public void setScale(float scale) {
        // actualizar también el hitbox si cambias la escala en runtime
        float cx = hitbox.x + hitbox.width * 0.5f;
        float cy = hitbox.y + hitbox.height * 0.5f;
        this.scale = scale;
        hitbox.setSize(FRAME_W * scale, FRAME_H * scale);
        hitbox.setCenter(cx, cy);
    }

    public void refreshLevelRefs() {
        this.lvlData = levelManager.getLevelData();
        this.tileW = levelManager.getTileWidth();
        this.tileH = levelManager.getTileHeight();
    }
}
