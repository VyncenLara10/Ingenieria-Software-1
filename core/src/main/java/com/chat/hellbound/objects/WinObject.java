package com.chat.hellbound.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.MapObject;
import com.chat.hellbound.entities.EnemyShared;
import com.chat.hellbound.utilz.TextureUtils;

public class WinObject {

    private final LevelManager levelManager;
    private final Rectangle hitbox;
    private final TextureRegion frame;
    private boolean collected = false;

    private static final float SCALE = 0.2f;
    private static final float INTERACT_RANGE = 40f;

    public WinObject(MapObject ob, LevelManager lm, int level) {
        this.levelManager = lm;

        Texture tex = Assets.getWinObjectAtlas(level, ob.type);
        TextureUtils.prepareTexture(tex);

        TextureRegion region = new TextureRegion(tex);
        TextureUtils.fixBleeding(region);
        this.frame = region;

        float width = frame.getRegionWidth() * SCALE;
        float height = frame.getRegionHeight() * SCALE;
        this.hitbox = new Rectangle(ob.position.x, ob.position.y, width, height);
    }

    public void update(float dt) {
        if (collected) return;

        Vector2 playerPos = EnemyShared.playerCenter();

        // Calcular distancia entre el jugador y el objeto
        float cx = hitbox.x + hitbox.width * 0.5f;
        float cy = hitbox.y + hitbox.height * 0.5f;
        float dx = playerPos.x - cx;
        float dy = playerPos.y - cy;
        float dist2 = dx * dx + dy * dy;

        // Si el jugador está cerca, recoger el objeto
        if (dist2 < INTERACT_RANGE * INTERACT_RANGE) {
            onCollect();
        }
    }

    public void render(SpriteBatch batch) {
        if (collected) return;

        float w = frame.getRegionWidth() * SCALE;
        float h = frame.getRegionHeight() * SCALE;
        batch.draw(frame, hitbox.x, hitbox.y, w, h);

    }

    private void onCollect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}
