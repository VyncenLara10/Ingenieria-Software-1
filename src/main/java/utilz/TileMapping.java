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
    public static final int GRASS = 0;   // 0–10
    public static final int TREE = 1;    // 11–20
    public static final int ROCK = 2;    // 21–30
    public static final int BUSH = 3;    // 31–40
    public static final int HOLE = 4;

    public static int getTileTypeFromRed(int red) {
        if (red <= 10) return GRASS;
        if (red <= 20) return TREE;
        if (red <= 30) return ROCK;
        if (red <= 40) return BUSH;
        if (red <= 48) return HOLE;
        return GRASS;
    }
}
