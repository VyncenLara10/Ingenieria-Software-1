package com.chat.hellbound.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class EnemyShared {

    private static EnemyManager manager;
    private static Player player;

    public static void hookManager(EnemyManager m) { manager = m; }
    public static void hookPlayer(Player p) { player = p; }

    public static Vector2 playerCenter() {
        if (player == null) return new Vector2(0,0);
        return player.getCameraFocus();
    }

    public static void queueEnemyAttack(Object from, Rectangle box, int damage) {
        if (manager != null) manager.enqueueEnemyAttack(box, damage);
    }
}
