package com.chat.hellbound.utilz;

import com.badlogic.gdx.math.Vector2;

public class MapObject {
    public Vector2 position;
    public int type; // 1, 2 o 3 según el azul

    public MapObject(Vector2 pos, int type) {
        this.position = pos;
        this.type = type;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }
}
