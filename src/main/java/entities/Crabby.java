package entities;

import static utilz.Constantes.EnemyConstants.*;
import static utilz.HelpMethods.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.Random;

import static utilz.Constantes.Direcciones.*;

import main.Juego;

public class Crabby extends Enemy {

	// AttackBox
	private Rectangle2D.Float attackBox;
	private int attackBoxOffsetX;

	private Random random = new Random();
	private int movX = 0, movY = 0;
	private int movTimer = 0;
	private int moveChangeInterval = 240;
	private float movSpeed = 0.3f * Juego.SCALE;

	public Crabby(float x, float y) {
		super(x, y, CRABBY_WIDTH, CRABBY_HEIGHT, CRABBY);
		initHitbox(x, y, (int) (22 * Juego.SCALE), (int) (19 * Juego.SCALE));
		initAttackBox();
	}

	private void initAttackBox() {
		attackBox = new Rectangle2D.Float(x, y, (int) (82 * Juego.SCALE), (int) (19 * Juego.SCALE));
		attackBoxOffsetX = (int) (Juego.SCALE * 30);
	}

	public void update(int[][] lvlData, Player player) {
		updateBehavior(lvlData, player);
		updateAnimationTick();
		updateAttackBox();
	}

	private void updateAttackBox() {
		attackBox.x = hitbox.x - attackBoxOffsetX;
		attackBox.y = hitbox.y;
	}

	private void updateBehavior(int[][] lvlData, Player player) {
		movTimer++;
		if (movTimer > moveChangeInterval) {
			movTimer = 0;
			moveChangeInterval = 60 + random.nextInt(180);
			setRandomDirection();	
		}

		if (firstUpdate)
			firstUpdateCheck(lvlData);

		if (inAir)
			updateInAir(lvlData);
		else {
			switch (enemyState) {
			case IDLE:
				newState(RUNNING);
				break;
			case RUNNING:
				if (detectPlayer(player)) {
					int[] outMovX = new int[1];
					int[] outMovY = new int[1];
					moveTowardsPlayer(player, outMovX, outMovY);
					movX = outMovX[0];
					movY = outMovY[0];
				}
				
				if (attackInAllDirections(player))
					newState(ATTACK);

				move(lvlData);
				break;
			case ATTACK:
				if (aniIndex == 0)
					attackChecked = false;
				// Changed the name for checkEnemyHit to checkPlayerHit
				if (aniIndex == 3 && !attackChecked)
					checkPlayerHit(attackBox, player);

				break;
			case HIT:
				break;
			}
		}
	}

	private boolean detectPlayer(Player player) {
		float distX = Math.abs(player.hitbox.x + player.hitbox.width/2 - (hitbox.x + hitbox.width/2));
		float distY = Math.abs(player.hitbox.y + player.hitbox.height/2 - (hitbox.y + hitbox.height/2));
		
		return distX <= detectionRange && distY <= detectionRange;
	}
	
	private boolean attackInAllDirections(Player player) {
		float distX = Math.abs(player.hitbox.x - hitbox.x);
		float distY = Math.abs(player.hitbox.y - hitbox.y);
		float totalDist = (float) Math.sqrt(distX * distX + distY * distY);
		
		return totalDist <= attackDistance;
	}

	private void setRandomDirection() {
		int dir = random.nextInt(9);
		switch (dir) {
			case 0 -> { movX = 1;  movY = 0;  walkDir = RIGHT; }  
			case 1 -> { movX = -1; movY = 0;  walkDir = LEFT; }   
			case 2 -> { movX = 0;  movY = 1; }                    
			case 3 -> { movX = 0;  movY = -1; }                   
			case 4 -> { movX = 1;  movY = 1;  walkDir = RIGHT; }  
			case 5 -> { movX = 1;  movY = -1; walkDir = RIGHT; }  
			case 6 -> { movX = -1; movY = 1;  walkDir = LEFT; }   
			case 7 -> { movX = -1; movY = -1; walkDir = LEFT; }   
			case 8 -> { movX = 0;  movY = 0; }                    
		}
	}
	
	protected void move(int[][] lvlData) {
		float xSpeed = movX * movSpeed;
		float ySpeed = movY * movSpeed;
		
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y + ySpeed, hitbox.width, hitbox.height, lvlData)) {
			hitbox.x += xSpeed;
			hitbox.y += ySpeed;
		} else {
			setRandomDirection();
		}
	}

	public void drawAttackBox(Graphics g, int xLvlOffset) {
		g.setColor(Color.red);
		g.drawRect((int) (attackBox.x - xLvlOffset), (int) attackBox.y, (int) attackBox.width, (int) attackBox.height);
	}

	public void drawDetectionRange(Graphics g, int xLvlOffset) {
		g.setColor(new Color(255, 255, 0, 50));
		int rangeSize = (int) (detectionRange * 2);
		g.fillOval(
			(int) (hitbox.x + hitbox.width/2 - detectionRange - xLvlOffset), 
			(int) (hitbox.y + hitbox.height/2 - detectionRange), 
			rangeSize, 
			rangeSize
		);
		
		g.setColor(Color.YELLOW);
		g.drawOval(
			(int) (hitbox.x + hitbox.width/2 - detectionRange - xLvlOffset), 
			(int) (hitbox.y + hitbox.height/2 - detectionRange), 
			rangeSize, 
			rangeSize
		);
	}

	public int flipX() {
		if (walkDir == RIGHT)
			return width;
		else
			return 0;
	}

	public int flipW() {
		if (walkDir == RIGHT)
			return -1;
		else
			return 1;
	}
}