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

    private final LevelManager levelManager;
    private int level;

    private final ArrayList<Enemy> enemies = new ArrayList<>();
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
            enemies.add(new Crabby(p.x, p.y, levelManager, level));
        }
    }

    public void addEnemy(Enemy e) {
        enemies.add(e);
    }

    public void update(float dt, Player player) {

        Rectangle atk = player.consumeAttackBox();
        if (atk != null) {
            for (Enemy e : enemies) {
                if (!e.isDead() && e.getHitbox().overlaps(atk)) {
                    e.applyDamage(Constants.PlayerConstants.DAMAGE);
                }
            }
        }

        for (Enemy e : enemies) e.update(dt);

        for (PendingAttack pa : pendingEnemyAttacks) {
            if (!player.isDead() && player.getHitbox().overlaps(pa.box)) {
                player.applyDamage(pa.damage);
            }
        }
        pendingEnemyAttacks.clear();

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (e.isDead() && !e.dying) {
                it.remove();
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Enemy e : enemies) e.render(batch);
    }

    public void enqueueEnemyAttack(Rectangle box, int damage) {
        pendingEnemyAttacks.add(new PendingAttack(new Rectangle(box), damage));
    }

    public void renderDebug(ShapeRenderer sr) {
        sr.setColor(1f, 1f, 0f, 1f);
        for (Enemy e : enemies) {
            Rectangle hb = e.getHitbox();
            sr.rect(hb.x, hb.y, hb.width, hb.height);
        }
    }

    private static class PendingAttack {
        Rectangle box; int damage;
        PendingAttack(Rectangle b, int d){ this.box=b; this.damage=d; }
    }
}
