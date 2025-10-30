/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilz;

/**
 *
 * @author USUARIO
 */
public class TileMapping {
    public static final int TREE = 0;
    public static final int BUSH1 = 1;
    public static final int BUSH2 = 2;
    public static final int BUSH3 = 3;
    public static final int ROCK1 = 4;
    public static final int ROCK2 = 5;
    public static final int GRASS = 6;
    public static final int TRUNK1 = 7;
    public static final int TRUNK2 = 8;
    public static final int TRUNK3 = 9;

    public static int getTileTypeFromRed(int red) {
        if (red <= 5) return TREE;
        if (red <= 10) return BUSH1;
        if (red <= 15) return BUSH2;
        if (red <= 20) return BUSH3;
        if (red <= 25) return ROCK1;
        if (red <= 30) return ROCK2;
        if (red <= 35) return GRASS;
        if (red <= 40) return TRUNK1;
        if (red <= 45) return TRUNK2;
        if (red <= 48) return TRUNK3;
        return GRASS;
    }
}
