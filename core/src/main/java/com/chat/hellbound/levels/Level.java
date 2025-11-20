package com.chat.hellbound.levels;

public class Level {
    private final int[][] lvlData;
    public Level(int[][] lvlData) { this.lvlData = lvlData; }
    public int[][] getLevelData(){ return lvlData; }
    public int getWidth(){ return lvlData[0].length; }
    public int getHeight(){ return lvlData.length; }
}
