public class PlayerSettings {
    private float visionRange;
    private float speed;
    
    public PlayerSettings() {
        this.visionRange = 10.0f;
        this.speed = 5.0f;
    }
    
    public PlayerSettings(float visionRange, float speed) {
        this.visionRange = visionRange;
        this.speed = speed;
    }
    
    public float getVisionRange() {
        return visionRange;
    }
    
    public void setVisionRange(float visionRange) {
        this.visionRange = visionRange;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    @Override
    public String toString() {
        return "PlayerSettings{" +
                "visionRange=" + visionRange +
                ", speed=" + speed +
                '}';
    }
}