package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class BossManager {
    private List<Boss> bosses;
    private Boss activeBoss;
    private boolean bossEncounterActive;

    public BossManager() {
        bosses = new ArrayList<>();
        activeBoss = null;
        bossEncounterActive = false;
    }

    public void addBoss(Boss boss) {
        bosses.add(boss);
    }

    public void triggerBossEncounter(int bossIndex) {
        if (bossIndex >= 0 && bossIndex < bosses.size()) {
            activeBoss = bosses.get(bossIndex);
            activeBoss.setActive(true);
            bossEncounterActive = true;
        }
    }

    public void update(float delta, Player player) {
        if (activeBoss != null && activeBoss.isActive()) {
            // Check if player attacks boss
            Rectangle atk = player.consumeAttackBox();
            if (atk != null && !activeBoss.isDefeated()) {
                if (activeBoss.getHitbox().overlaps(atk)) {
                    activeBoss.takeDamage(com.chat.hellbound.utilz.Constants.PlayerConstants.DAMAGE * 5); // Boss takes more damage
                }
            }

            activeBoss.update(delta, player);

            // Check if boss is defeated
            if (activeBoss.isDefeated()) {
                bossEncounterActive = false;
                onBossDefeated();
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (activeBoss != null && activeBoss.isActive()) {
            activeBoss.render(batch);
        }
    }

    private void onBossDefeated() {
        // Handle boss defeat (unlock areas, give rewards, etc.)
        System.out.println("CHICHIFLIX");
    }

    public boolean isBossEncounterActive() {
        return bossEncounterActive;
    }

    public Boss getActiveBoss() {
        return activeBoss;
    }

    public void reset() {
        bosses.clear();
        activeBoss = null;
        bossEncounterActive = false;
    }
}
