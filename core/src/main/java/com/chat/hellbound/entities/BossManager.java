package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
            Rectangle atk = player.consumeAttackBox();
            if (atk != null && !activeBoss.isDefeated()) {
                if (activeBoss.getHitbox().overlaps(atk)) {
                    activeBoss.takeDamage(com.chat.hellbound.utilz.Constants.PlayerConstants.DAMAGE * 5);
            }
            
            activeBoss.update(delta, player);
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
        // Condicionamos aqui la victoria o si lo pasamos al otro mapa
        System.out.println("Derrotado");
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
