package com.chat.hellbound.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.LoadSave;
import com.chat.hellbound.utilz.MapObject;

import java.util.ArrayList;
import java.util.Iterator;

public class InteractiveObject {
    private final LevelManager levelManager;
    private final ArrayList<WinObject> winObjects = new ArrayList<>();

    public InteractiveObject(LevelManager lm, int level) {
        this.levelManager = lm;
        spawnObjects(level);
    }

    private void spawnObjects(int level) {
        int tileW = levelManager.getTileWidth();
        int tileH = levelManager.getTileHeight();
        ArrayList<MapObject> spawns = LoadSave.GetObjects(tileW, tileH);

        for (MapObject p : spawns) {
            winObjects.add(new WinObject(p, levelManager, level));
        }
    }

    public boolean allCollected() {
        for (WinObject obj : winObjects) {
            if (!obj.isCollected()) {
                return false; // si alguno aún no se recogió, falta
            }
        }
        return true; // todos fueron recogidos
    }


    public void update(float dt) {
        for (WinObject c : winObjects) {
            c.update(dt);
        }
    }

    public void render(SpriteBatch batch) {
        for (WinObject c : winObjects) {
            c.render(batch);
        }
    }

    private void removeCollected() {
        Iterator<WinObject> iterator = winObjects.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isCollected()) {
                iterator.remove();
            }
        }
    }
}
