package entities;

import static utilz.Constantes.ConstantesJugador.*;
import static utilz.HelpMethods.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;

import gamestates.Playing;
import main.Juego;
import utilz.LoadSave;

public class Player extends Entity{
    private BufferedImage[][] animations;
    private int aniTick, aniIndex, aniSpeed = 35;
    private int playerAction = IDLE;
    private boolean moving = false, attacking = false;
    private boolean left, up, right, down, jump;
    private float playerSpeed = 1.0f * Juego.SCALE;
    private int[][] lvlData;
    private float xDrawOffset = 21 * Juego.SCALE;
    private float yDrawOffset = 4 * Juego.SCALE;
    
    public Player(float x, float y, int width, int height){
        super(x, y, width, height);
        loadAnimations();
        initHitbox(x, y, (int)(20*Juego.SCALE), (int)(27*Juego.SCALE));
    }
    
    public void update(){
        updatePos();
        updateAnimationTick();
        setAnimation();
    }
    
    public void render(Graphics g, int lvlOffset) {
        g.drawImage(animations[playerAction][aniIndex], (int) (hitbox.x - xDrawOffset) - lvlOffset, (int) (hitbox.y - yDrawOffset), width, height, null);
//		drawHitbox(g);
    }
    
    private void updateAnimationTick() {
        aniTick++;
        if(aniTick >= aniSpeed){
            aniTick = 0;
            aniIndex++;
            if(aniIndex >= GetSpriteAmount(playerAction)){
                aniIndex = 0;
                attacking = false;
            }
        }
    }
    
    private void setAnimation() {
        int startAni = playerAction;
        
        if(attacking){
            playerAction = ATTACK_1;
        } else if(moving){
            if(up){
                playerAction = UP;
            } else if(down){
                playerAction = DOWN;
            } else {
                playerAction = RUNNING;
            }
        } else {
            playerAction = IDLE;
        }
        
        if(startAni != playerAction){
            resetAniTick();
        }
    }
    
    private void resetAniTick(){
        aniTick = 0;
        aniIndex = 0;
    }
    
    private void updatePos() {
        moving = false;
        
        if(!left && !right && !up && !down){
            return;
        }
        
        float xSpeed = 0, ySpeed = 0;
        
        if(left){
            xSpeed -= playerSpeed;
        }
        if(right) {
            xSpeed += playerSpeed;
        }
        if(up){
            ySpeed -= playerSpeed;
        }
        if(down) {
            ySpeed += playerSpeed;
        }
        
        if(CanMoveHere(hitbox.x + xSpeed, hitbox.y + ySpeed, hitbox.width, hitbox.height, lvlData)){
            hitbox.x += xSpeed;
            hitbox.y += ySpeed;
            moving = true;
        }
    }
    
    private void loadAnimations() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);
        
   
        animations = new BufferedImage[5][7];
        
        for(int j = 0; j < animations.length; j++){
            for(int i = 0; i < animations[j].length; i++){
                animations[j][i] = img.getSubimage(i*81, j*75, 81, 75);
            }
        }
    }
    
    public void loadLvlData(int[][] lvlData){
        this.lvlData = lvlData;
    }
    
    public void resetDirBooleans(){
        left = false;
        right = false;
        up = false;
        down = false;
    }
   
    public void setAtacking(boolean attacking){
        this.attacking = attacking;
    }

	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	public void resetDirBooleans() {
		left = false;
		right = false;
		up = false;
		down = false;
	}

	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isUp() {
		return up;
	}

	public void setUp(boolean up) {
		this.up = up;
	}

    public void setDown(boolean down) {
        this.down = down;
    }
}