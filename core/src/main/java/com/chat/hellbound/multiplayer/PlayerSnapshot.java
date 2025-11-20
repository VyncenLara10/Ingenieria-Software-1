package com.chat.hellbound.multiplayer;

import com.chat.hellbound.entities.Player;
import com.badlogic.gdx.math.Rectangle;

public class PlayerSnapshot {

    public float x;
    public float y;
    public int hp;

    public PlayerSnapshot() {}

    public PlayerSnapshot(Player player) {
        Rectangle hb = player.getHitbox();
        this.x = hb.x;
        this.y = hb.y;
        this.hp = player.getHp();
    }

    public void applyTo(Player player) {
        // Solo movemos posición por ahora
        player.setNetworkPosition(x, y);
        // Si luego quieres sincronizar HP, aquí podrías hacerlo
        // pero tendríamos que agregar un setter de HP “desde red”.
    }
}
