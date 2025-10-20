package gamestates;

import entities.Player;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.awt.image.BufferedImage;
import java.util.Random;
import levels.LevelManager;
import main.Juego;
import static main.Juego.SCALE;
import ui.PauseOverlay;
import static utilz.Constantes.Environment.BIG_CLOUD_HEIGHT;
import static utilz.Constantes.Environment.BIG_CLOUD_WIDTH;
import static utilz.Constantes.Environment.SMALL_CLOUD_HEIGHT;
import static utilz.Constantes.Environment.SMALL_CLOUD_WIDTH;
import utilz.LoadSave;

public class Playing extends State implements Statemethods{
    private Player player;
    private LevelManager levelManager;
    private PauseOverlay pauseOverlay;
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
        player = new Player(200,200,(int)(64 * Juego.SCALE), (int)(40 * Juego.SCALE));
        player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        pauseOverlay = new PauseOverlay(this);
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
    
    public void windowFocusLost(){
        player.resetDirBooleans();
    }
    
    public Player getPlayer(){
        return player;
    }
    
    public void unpauseGame() {
		paused = false;
	}
    
    private void drawClouds(Graphics g) {

		for (int i = 0; i < 3; i++)
			g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Juego.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

		for (int i = 0; i < smallCloudsPos.length; i++)
			g.drawImage(smallCloud, SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);

	}

    @Override
	public void update() {
		if (!paused) {
			levelManager.update();
			player.update();
                        checkCloseToBorder();
		} else {
			pauseOverlay.update();
		}
	}

    @Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT, null);

		drawClouds(g);
                
		levelManager.draw(g, xLvlOffset);
                g.setColor(new Color(0, 0, 0, 150));
		g.fillRect(0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT);
		player.render(g, xLvlOffset);

		if (paused) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT);
			pauseOverlay.draw(g);
		}
	}

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1){
            player.setAtacking(true);
        }
    }

    @Override
	public void mousePressed(MouseEvent e) {
		if (paused)
			pauseOverlay.mousePressed(e);

	}

    @Override
	public void mouseReleased(MouseEvent e) {
		if (paused)
			pauseOverlay.mouseReleased(e);

	}

    @Override
	public void mouseMoved(MouseEvent e) {
		if (paused)
			pauseOverlay.mouseMoved(e);

	}

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
                case KeyEvent.VK_W:
                    player.setUp(true);
                    break;
                case KeyEvent.VK_A:
                    player.setLeft(true);
                    break;
                case KeyEvent.VK_S:
                    player.setDown(true);
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
        switch(e.getKeyCode()){
                case KeyEvent.VK_W:
                    player.setUp(false);
                    break;
                case KeyEvent.VK_A:
                    player.setLeft(false);
                    break;
                case KeyEvent.VK_S:
                    player.setDown(false);
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
		if (paused)
			pauseOverlay.mouseDragged(e);
	}
}
