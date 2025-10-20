
package levels;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import main.Juego;
import utilz.LoadSave;


public class LevelManager {
    
    private Juego juego;
    private BufferedImage[] levelSprite;
    private Level levelOne;
    
    public LevelManager(Juego juego){
        this.juego = juego;
        importOutsideSprites();
        levelOne = new Level(LoadSave.GetLevelData());
    }
    
    private void importOutsideSprites() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);
        levelSprite = new BufferedImage[4];
        
        for(int i = 0; i < 4; i++){
            levelSprite[i] = img.getSubimage(i*32, 0, 32, 32);
        }
    }
    
    public void draw(Graphics g, int lvlOffset) {
		for (int j = 0; j < Juego.TILES_IN_HEIGHT; j++)
			for (int i = 0; i < levelOne.getLevelData()[0].length; i++) {
				int index = levelOne.getSpriteIndex(i, j);
				g.drawImage(levelSprite[index], Juego.TILES_SIZE * i - lvlOffset, Juego.TILES_SIZE * j, Juego.TILES_SIZE, Juego.TILES_SIZE, null);
			}
	}
    
    public void update(){
        
    }
    
    public Level getCurrentLevel(){
        return levelOne;
    }

}
