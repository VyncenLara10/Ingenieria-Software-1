package com.chat.hellbound.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chat.hellbound.entities.EnemyShared;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.LoadSave;
import com.chat.hellbound.utilz.Constants;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Iterator;

public class ObjectManager {
    private static final boolean DEBUG_LOG_HITS = true;
    private final LevelManager levelManager;

    public ObjectManager(LevelManager lm) {
        this.levelManager = lm;
    }



}
