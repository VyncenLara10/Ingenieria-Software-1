/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package multiplayer;

/**
 *
 * @author USUARIO
 */
public class PlayerData {
    public int x, y;

    public PlayerData(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toJSON() {
        return x + "," + y; // formato simple
    }

    public static PlayerData fromJSON(String s) {
        String[] parts = s.split(",");
        return new PlayerData(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
