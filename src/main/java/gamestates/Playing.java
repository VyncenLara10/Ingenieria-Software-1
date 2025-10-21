package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import entities.Crabby;
import entities.EnemyManager;
import entities.Player;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.util.ArrayList;
import levels.LevelManager;
import main.Juego;
import ui.GameOverOverlay;
import ui.PauseOverlay;
import utilz.LoadSave;
import static utilz.Constantes.Environment.*;

public class Playing extends State implements Statemethods {
	private Player player;
	private LevelManager levelManager;
	private EnemyManager enemyManager;
	private PauseOverlay pauseOverlay;
	private GameOverOverlay gameOverOverlay;
	private boolean paused = false;

	private int xLvlOffset;
	private int leftBorder = (int) (0.2 * Juego.GAME_WIDTH);
	private int rightBorder = (int) (0.8 * Juego.GAME_WIDTH);
	private int lvlTilesWide = LoadSave.GetLevelData()[0].length;
	private int maxTilesOffset = lvlTilesWide - Juego.TILES_IN_WIDTH;
	private int maxLvlOffsetX = maxTilesOffset * Juego.TILES_SIZE;

	private BufferedImage backgroundImg, bigCloud, smallCloud;
	private int[] smallCloudsPos;
	private Random rnd = new Random();

	private boolean gameOver;
        
        // Linterna
        private int playerFlashlightRadius = (int) (180 * Juego.SCALE);
        private int playerDarknessAlpha = 250;

        // Luz de los objetos 
        private ArrayList<LightSource> lightSources;

	public Playing(Juego juego) {
            super(juego);
            initClasses();

		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PLAYING_BG_IMG);
		bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
		smallCloud = LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);
		smallCloudsPos = new int[8];
		for (int i = 0; i < smallCloudsPos.length; i++)
			smallCloudsPos[i] = (int) (90 * Juego.SCALE) + rnd.nextInt((int) (100 * Juego.SCALE));
	}

	private void initClasses() {
		levelManager = new LevelManager(juego);
		enemyManager = new EnemyManager(this);
		player = new Player(200, 200, (int) (64 * Juego.SCALE), (int) (40 * Juego.SCALE), this);
		player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		pauseOverlay = new PauseOverlay(this);
		gameOverOverlay = new GameOverOverlay(this);
	}

	@Override
	public void update() {
		if (!paused && !gameOver) {
			levelManager.update();
			player.update();
			enemyManager.update(levelManager.getCurrentLevel().getLevelData(), player);
			checkCloseToBorder();
		} else
			pauseOverlay.update();

	}

	private void checkCloseToBorder() {
		int playerX = (int) player.getHitbox().x;
		int diff = playerX - xLvlOffset;

		if (diff > rightBorder)
			xLvlOffset += diff - rightBorder;
		else if (diff < leftBorder)
			xLvlOffset += diff - leftBorder;

		if (xLvlOffset > maxLvlOffsetX)
			xLvlOffset = maxLvlOffsetX;
		else if (xLvlOffset < 0)
			xLvlOffset = 0;
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT, null);

		drawClouds(g);

		levelManager.draw(g, xLvlOffset);
		player.render(g, xLvlOffset);
		enemyManager.draw(g, xLvlOffset);

		if (paused) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT);
			pauseOverlay.draw(g);
		} else if (gameOver)
			gameOverOverlay.draw(g);
	}

	private void drawClouds(Graphics g) {
		for (int i = 0; i < 3; i++)
			g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Juego.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

		for (int i = 0; i < smallCloudsPos.length; i++)
			g.drawImage(smallCloud, SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
	}

	public void resetAll() {
		gameOver = false;
		paused = false;
		player.resetAll();
		enemyManager.resetAllEnemies();
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public void checkEnemyHit(Rectangle2D.Float attackBox) {
		enemyManager.checkEnemyHit(attackBox);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!gameOver)
			if (e.getButton() == MouseEvent.BUTTON1)
				player.setAttacking(true);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (gameOver)
			gameOverOverlay.keyPressed(e);
		else
			switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
				player.setLeft(true);
				break;
			case KeyEvent.VK_D:
				player.setRight(true);
				break;
			case KeyEvent.VK_SPACE:
				player.setJump(true);
				break;
			case KeyEvent.VK_ESCAPE:
				paused = !paused;
				break;
			}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!gameOver)
			switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
				player.setLeft(false);
				break;
			case KeyEvent.VK_D:
				player.setRight(false);
				break;
			case KeyEvent.VK_SPACE:
				player.setJump(false);
				break;
			}

	}

	public void mouseDragged(MouseEvent e) {
		if (!gameOver)
			if (paused)
				pauseOverlay.mouseDragged(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!gameOver)
			if (paused)
				pauseOverlay.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!gameOver)
			if (paused)
				pauseOverlay.mouseReleased(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!gameOver)
			if (paused)
				pauseOverlay.mouseMoved(e);
	}

	public void unpauseGame() {
		paused = false;
	}

	public void windowFocusLost() {
		player.resetDirBooleans();
	}

	public Player getPlayer() {
		return player;
	}
        
        public void addLightSource(float worldX, float worldY, int radius, int intensity) {
            lightSources.add(new LightSource(worldX, worldY, radius, intensity));
        }

        public void addLightSource(float worldX, float worldY, float radiusScale, float intensityScale) {
            int radius = (int)(radiusScale * Juego.SCALE);
            int intensity = (int)intensityScale;
            lightSources.add(new LightSource(worldX, worldY, radius, intensity));
        }

        public void clearLightSources() {
            lightSources.clear();
        }

        public void updateLightSource(int index, float worldX, float worldY) {
            if (index >= 0 && index < lightSources.size()) {
                lightSources.get(index).setPosition(worldX, worldY);
            }
        }

        private void drawLightingEffect(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            BufferedImage darknessLayer = new BufferedImage(
                Juego.GAME_WIDTH, 
                Juego.GAME_HEIGHT, 
                BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D darkG = darknessLayer.createGraphics();

            darkG.setRenderingHint(
                java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON
            );

            darkG.setColor(new Color(0, 0, 0, playerDarknessAlpha));
            darkG.fillRect(0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT);
            darkG.setComposite(java.awt.AlphaComposite.DstOut);

            float playerCenterX = player.getHitbox().x + player.getHitbox().width / 2 - xLvlOffset;
            float playerCenterY = player.getHitbox().y + player.getHitbox().height / 2;
            drawSingleLight(darkG, playerCenterX, playerCenterY, playerFlashlightRadius, playerDarknessAlpha);

            for (LightSource light : lightSources) {
                float screenX = light.worldX - xLvlOffset;
                float screenY = light.worldY;
                if (screenX > -light.radius && screenX < Juego.GAME_WIDTH + light.radius) {
                    drawSingleLight(darkG, screenX, screenY, light.radius, light.intensity);
                }
            }

            darkG.dispose();
            g2d.drawImage(darknessLayer, 0, 0, null);
        }

        private void drawSingleLight(Graphics2D g, float centerX, float centerY, int radius, int maxAlpha) {
            float[] dist = {0.0f, 0.7f, 1.0f};
            Color[] colors = {
                new Color(255, 255, 255, 255),        
                new Color(255, 255, 255, 200),        
                new Color(255, 255, 255, 0)          
            };

            RadialGradientPaint gradient = new RadialGradientPaint(
                centerX, 
                centerY, 
                radius,
                dist, 
                colors
            );

            g.setPaint(gradient);
            g.fillOval(
                (int)(centerX - radius), 
                (int)(centerY - radius),
                radius * 2, 
                radius * 2
            );
        }

        public void setPlayerFlashlightRadius(int radius) {
            this.playerFlashlightRadius = radius;
        }

        public void setPlayerDarknessLevel(int alpha) {
            this.playerDarknessAlpha = Math.max(0, Math.min(255, alpha));
        }

        public int getPlayerFlashlightRadius() {
            return playerFlashlightRadius;
        }

        public int getPlayerDarknessLevel() {
            return playerDarknessAlpha;
        }
        
        private class LightSource {
            float worldX, worldY;
            int radius;           
            int intensity;

            public LightSource(float worldX, float worldY, int radius, int intensity) {
                this.worldX = worldX;
                this.worldY = worldY;
                this.radius = radius;
                this.intensity = intensity;
            }

            public void setPosition(float x, float y) {
                this.worldX = x;
                this.worldY = y;
            }

            public void setRadius(int radius) {
                this.radius = radius;
            }

            public void setIntensity(int intensity) {
                this.intensity = Math.max(0, Math.min(255, intensity));
            }
        }

}