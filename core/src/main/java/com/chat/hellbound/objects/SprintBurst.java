package com.chat.hellbound.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.chat.hellbound.entities.Player;
import com.chat.hellbound.levels.LevelManager;

public class SprintBurst implements Object{
    public String resume = "Bebida energetica que aumenta temporalmente la velocidad de movimiento cooldown 25s";
    public Player p;

    public SprintBurst(LevelManager lm, Player p) {
        this.p = p;
    }

    public void use(){
        p.setBonusSpeed(2f);
    };
    public void reset(){

    };
    public void updateAnimation(int framesForAction){

    };
    public void render(SpriteBatch batch){

    };
    public void update(float dt){

    };
}
