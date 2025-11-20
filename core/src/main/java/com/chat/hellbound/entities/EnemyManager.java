package com.chat.hellbound.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.LoadSave;
import com.chat.hellbound.utilz.Constants;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Iterator;

public class EnemyManager {

    private static final boolean DEBUG_LOG_HITS = true;

    private final LevelManager levelManager;
    private int level;
    private final ArrayList<Crabby> crabbies = new ArrayList<>();
    private final ArrayList<PendingAttack> pendingEnemyAttacks = new ArrayList<>();

    public EnemyManager(LevelManager lm, int level) {
        this.levelManager = lm;
        this.level = level;
        spawnCrabbies();
        EnemyShared.hookManager(this);
    }

    private void spawnCrabbies() {
        int tileW = levelManager.getTileWidth();
        int tileH = levelManager.getTileHeight();
        ArrayList<Vector2> spawns = LoadSave.GetCrabs(tileW, tileH);
        for (Vector2 p : spawns) {
            Crabby c = new Crabby(p.x, p.y, levelManager, level);
            crabbies.add(c);
        }
    }

    public void update(float dt, Player player) {
        Rectangle atk = player.consumeAttackBox();
        if (atk != null) {
            for (Crabby c : crabbies) {
                if (!c.isDead() && c.getHitbox().overlaps(atk)) {
                    c.applyDamage(Constants.PlayerConstants.DAMAGE);
                    if (DEBUG_LOG_HITS) {
                        Gdx.app.log("HIT", "Crabby HP=" + c.hp + " dmg=" + Constants.PlayerConstants.DAMAGE);
                    }
                }
            }
        }


        for (Crabby c : crabbies) {
            c.update(dt);
        }

        for (PendingAttack pa : pendingEnemyAttacks) {
            if (!player.isDead() && player.getHitbox().overlaps(pa.box)) {
                player.applyDamage(pa.damage);
                if (DEBUG_LOG_HITS) {
                    Gdx.app.log("HIT", "Player HP=" + player.getHp() + " dmg=" + pa.damage);
                }
            }
        }
        pendingEnemyAttacks.clear();

        Iterator<Crabby> it = crabbies.iterator();
        while (it.hasNext()) {
            Crabby c = it.next();
            if (c.isDead() && !c.dying) {
                if (DEBUG_LOG_HITS) {
                    Gdx.app.log("DEAD", "Crabby removed");
                }
                it.remove();
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Crabby c : crabbies) {
            c.render(batch);
        }
    }

    public void enqueueEnemyAttack(Rectangle box, int damage) {
        pendingEnemyAttacks.add(new PendingAttack(new Rectangle(box), damage));
    }

    private static class PendingAttack {
        Rectangle box; int damage;
        PendingAttack(Rectangle b, int d){ this.box=b; this.damage=d; }
    }

    public void renderDebug(ShapeRenderer sr) {
        sr.setColor(1f, 1f, 0f, 1f);
        for (Crabby c : crabbies) {
            Rectangle hb = c.getHitbox();
            sr.rect(hb.x, hb.y, hb.width, hb.height);
        }
    }
}
