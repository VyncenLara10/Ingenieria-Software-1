package com.chat.hellbound.utilz;

public class Constants {

    public static final float WORLD_WIDTH  = 1600f;
    public static final float WORLD_HEIGHT = 900f;

    public static class PlayerConstants {
        public static final int IDLE = 0;
        public static final int RUNNING = 1;
        public static final int JUMP = 2;
        public static final int FALLING = 3;
        public static final int GROUND = 4;
        public static final int HIT = 5;
        public static final int ATTACK_1 = 6;
        public static final int ATTACK_JUMP_1 = 7;
        public static final int ATTACK_JUMP_2 = 8;

        public static final int FRAME_W = 81;
        public static final int FRAME_H = 75;

        public static final int ANI_SPEED = 5;
        public static final float MOVE_SPEED = 240f;
        public static final int MAX_HP = 5;
        public static final float ATTACK_COOLDOWN = 0.35f;
        public static final int DAMAGE = 1;

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
    }

    public static class EnemyConstants {
        public static final int CRABBY = 0;

        public static final int FRAME_W = 72;
        public static final int FRAME_H = 33;

        public static final int IDLE_ROW   = 0; public static final int IDLE_COUNT   = 9;
        public static final int RUN_ROW    = 1; public static final int RUN_COUNT    = 6;

        public static final int ATTACK_ROW = 2; public static final int ATTACK_COUNT = 7;

        public static final int HIT_ROW    = 3; public static final int HIT_COUNT    = 4;
        public static final int DEAD_ROW   = 4; public static final int DEAD_COUNT   = 5;

        public static final int IDLE = 0;
        public static final int RUN = 1;
        public static final int ATTACK = 2;
        public static final int HIT = 3;
        public static final int DEAD = 4;

        public static final int   ANI_SPEED        = 6;
        public static final int   MAX_HP           = 20;
        public static final int   DAMAGE           = 1;
        public static final float MOVE_SPEED       = 120f;
        public static final float AGGRO_RANGE      = 260f;
        public static final float ATTACK_RANGE     = 60f;
        public static final float ATTACK_COOLDOWN  = 0.8f;
        public static final float ATTACK_LOCK_TIME = 0.45f;
        public static final float KNOCKBACK        = 120f;

        public static final float FACE_EPS = 6f;
    }
    public static class HellGuardianConstants {
        public static final int HELL_GUARDIAN = 0;

        public static final int MAX_HP = 5;
        public static final int DAMAGE = 2;
        public static final float MOVE_SPEED = 3f;
        public static final float ATTACK_COOLDOWN = 3.0f;

        // Phases
        public static final int PHASE_1 = 1;
        public static final int PHASE_2 = 2;
        public static final int PHASE_3 = 3;

        // Attack patterns
        public static final int ATTACK_FIREBALL = 0;
        public static final int ATTACK_SPREAD = 1;
        public static final int ATTACK_CIRCULAR = 2;
        public static final int ATTACK_DASH = 3;
        public static final int ATTACK_SLAM = 4;
    }

    public static class TreeBossConstants {
        // Tree Boss - Jefe del juego que aparece al recolectar los 3 objetos
        public static final int FRAME_W = 72;
        public static final int FRAME_H = 33;

        // Animaciones (usando el mismo layout que Crabby)
        public static final int IDLE_ROW   = 0; public static final int IDLE_COUNT   = 9;
        public static final int RUN_ROW    = 1; public static final int RUN_COUNT    = 6;
        public static final int ATTACK_ROW = 2; public static final int ATTACK_COUNT = 7;
        public static final int HIT_ROW    = 3; public static final int HIT_COUNT    = 4;
        public static final int DEAD_ROW   = 4; public static final int DEAD_COUNT   = 5;

        public static final int IDLE = 0;
        public static final int RUN = 1;
        public static final int ATTACK = 2;
        public static final int HIT = 3;
        public static final int DEAD = 4;

        // Estadísticas del jefe
        public static final int   ANI_SPEED        = 6;
        public static final int   MAX_HP           = 10;  // Mucha más vida
        public static final int   DAMAGE           = 3;    // Más daño
        public static final float MOVE_SPEED       = 10f; // Un poco más rápido
        public static final float AGGRO_RANGE      = 400f; // Rango de agro más grande
        public static final float ATTACK_RANGE     = 80f;  // Rango de ataque más grande
        public static final float ATTACK_COOLDOWN  = 0.6f; // Ataca más seguido
        public static final float ATTACK_LOCK_TIME = 0.45f;
        public static final float KNOCKBACK        = 150f; // Más knockback
        public static final float BOSS_SCALE       = 3.5f; // Escala más grande (3.5x vs 2.5x de Crabby)

        public static final float FACE_EPS = 6f;
    }

    public static final float GAME_WIDTH = WORLD_WIDTH;
    public static final float GAME_HEIGHT = WORLD_HEIGHT;

}
