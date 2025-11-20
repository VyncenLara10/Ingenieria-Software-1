package com.chat.hellbound.multiplayer;

import com.chat.hellbound.entities.Player;
import com.badlogic.gdx.math.Rectangle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    // 🔥 NECESARIO PARA HostSession y ClientSession
    public static PlayerSnapshot fromPlayer(Player player) {
        return new PlayerSnapshot(player);
    }

    // 🔥 NECESARIO para enviar por red
    public void writeTo(DataOutputStream out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeInt(hp);
    }

    // 🔥 NECESARIO para recibir por red
    public static PlayerSnapshot readFrom(DataInputStream in) throws IOException {
        PlayerSnapshot snap = new PlayerSnapshot();
        snap.x = in.readFloat();
        snap.y = in.readFloat();
        snap.hp = in.readInt();
        return snap;
    }

    public void applyTo(Player player) {
        // Mueve al jugador remoto
        player.setNetworkPosition(x, y);

        // Si quieres sincronizar HP real, luego agregamos:
        // player.setNetworkHp(hp);
    }
}
