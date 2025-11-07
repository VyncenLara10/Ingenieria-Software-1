package database;
public class Settings {
    private String playerName;
    private float brightness;
    private float speed;

    public Settings(String playerName, float brightness, float speed) {
        this.playerName = playerName;
        this.brightness = brightness;
        this.speed = speed;
    }

    // Getters y setters
    public String getPlayerName() { return playerName; }
    public float getBrightness() { return brightness; }
    public float getSpeed() { return speed; }

    public void setBrightness(float brightness) { this.brightness = brightness; }
    public void setSpeed(float speed) { this.speed = speed; }
}