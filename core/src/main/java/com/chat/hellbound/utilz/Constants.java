package com.chat.hellbound.utilz;

public class Constants {
    public static final float WORLD_WIDTH  = 1600f;
    public static final float WORLD_HEIGHT = 900f;

    public static class PlayerConstants {
        public static final int IDLE = 0, RUNNING = 1, JUMP = 2, FALLING = 3, GROUND = 4, HIT = 5, ATTACK_1 = 6, ATTACK_JUMP_1 = 7, ATTACK_JUMP_2 = 8;
        public static final int FRAME_W = 81, FRAME_H = 75;
        public static int getSpriteAmount(int action) {
            switch (action) {
                case IDLE: return 9;
                case RUNNING: return 6;
                case JUMP:
                case ATTACK_1:
                case ATTACK_JUMP_1:
                case ATTACK_JUMP_2: return 3;
                case GROUND: return 2;
                case FALLING:
                case HIT:
                default: return 1;
            }
        }
        public static final int ANI_SPEED = 5;
        public static final float MOVE_SPEED = 240f;
    }
}
