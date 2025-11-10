package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.LoadSave;
import com.chat.hellbound.utilz.Constants;

import java.util.ArrayList;
import java.util.Iterator;

public class EnemyManager {

    private final LevelManager levelManager;
    private final ArrayList<Crabby> crabbies = new ArrayList<>();
    private final ArrayList<PendingAttack> pendingEnemyAttacks = new ArrayList<>();

    public EnemyManager(LevelManager lm) {
        this.levelManager = lm;
        spawnCrabbies();
        EnemyShared.hookManager(this);
    }

    private void spawnCrabbies() {
        int tileW = levelManager.getTileWidth();
        int tileH = levelManager.getTileHeight();
        ArrayList<Vector2> spawns = LoadSave.GetCrabs(tileW, tileH);
        for (Vector2 p : spawns) {
            Crabby c = new Crabby(p.x, p.y, levelManager);
            crabbies.add(c);
        }
    }

    public void update(float dt, Player player) {
        if (player.hasAttackBox()) {
            Rectangle atk = player.getAttackBox();
            for (Crabby c : crabbies) {
                if (!c.isDead() && c.getHitbox().overlaps(atk)) {
                    c.applyDamage(Constants.PlayerConstants.DAMAGE);
                }
            }
        }

        for (Crabby c : crabbies) {
            c.update(dt);
        }

        for (PendingAttack pa : pendingEnemyAttacks) {
            if (!player.isDead() && player.getHitbox().overlaps(pa.box)) {
                player.applyDamage(pa.damage);
            }
        }
        pendingEnemyAttacks.clear();

        Iterator<Crabby> it = crabbies.iterator();
        while (it.hasNext()) {
            Crabby c = it.next();
            if (c.isDead() && !c.dying) {
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
}
