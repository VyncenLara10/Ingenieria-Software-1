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
    private int totalObjects;
    private int collectedCount;

    public InteractiveObject(LevelManager lm, int level) {
        this.levelManager = lm;
        this.collectedCount = 0;
        spawnObjects(level);
        this.totalObjects = winObjects.size();
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
        return collectedCount >= totalObjects;
    }

    public int getCollectedCount() {
        return collectedCount;
    }

    public int getTotalObjects() {
        return totalObjects;
    }

    public float getCollectionProgress() {
        if (totalObjects == 0) return 1f;
        return (float) collectedCount / totalObjects;
    }


    public void update(float dt) {
        collectedCount = 0;
        for (WinObject c : winObjects) {
            c.update(dt);
            if (c.isCollected()) {
                collectedCount++;
            }
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
