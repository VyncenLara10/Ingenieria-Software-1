package com.chat.hellbound.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

interface Object{
    public void use();
    public void reset();
    public void updateAnimation(int framesForAction);
    public void render(SpriteBatch batch);
    public void update(float dt);
}
