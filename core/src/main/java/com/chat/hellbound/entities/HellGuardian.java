package com.chat.hellbound.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.HelpMethods;
import com.chat.hellbound.utilz.TextureUtils;

import static com.chat.hellbound.utilz.Constants.HellGuardianConstants.*;

public class HellGuardian extends Enemy {

    private final LevelManager levelManager;
    private final int[][] lvlData;
    private final int tileW, tileH;

    private TextureRegion[][] frames;

    private float attackCooldown = 0f;
    private float attackLock = 0f;
    private float hitLock = 0f;
    private float attackTimer = 0f;
    private boolean attackHitProcessed = false;

    public int level;

    private float scale = 0.15f;

    private final Rectangle attackBox = new Rectangle();

    private static final boolean SPRITE_FACES_RIGHT = true;
    private static final float HIT_LOCK_TIME = 0.25f;
    // Momento en el que inicia la ventana de daño durante la animación (0.0 a 1.0)
    // El ataque hará daño desde este momento hasta +0.3 segundos
    private static final float ATTACK_HIT_TIMING = 0.5f;

    public HellGuardian(float x, float y, LevelManager lm, int level) {
        this.levelManager = lm;
        this.lvlData = lm.getLevelData();
        this.tileW = lm.getTileWidth();
        this.tileH = lm.getTileHeight();
        this.level = level;

        Texture atlas = Assets.getHellGuardianAtlas();
        TextureUtils.prepareTexture(atlas);
        frames = TextureRegion.split(atlas, FRAME_W, FRAME_H);
        TextureUtils.fixBleeding(frames);

        this.maxHp = MAX_HP;
        this.hp = maxHp;
        this.aniSpeed = ANI_SPEED;

        initHitbox(x, y, FRAME_W * scale, FRAME_H * scale);
    }

    @Override
    public void update(float dt) {
        // Si está muerto, solo animar la muerte
        if (dead) {
            action = DEAD;
            updateDeath(dt, DEAD_COUNT);
            return;
        }

        // Actualizar temporizadores
        attackCooldown = Math.max(0f, attackCooldown - dt);
        attackLock = Math.max(0f, attackLock - dt);
        hitLock = Math.max(0f, hitLock - dt);

        // Si está en estado HIT, solo animar el golpe recibido
        if (hitLock > 0f) {
            action = HIT;
            updateAnimation(HIT_COUNT);
            return;
        }

        // Obtener posición del jugador
        Vector2 playerPos = EnemyShared.playerCenter();
        float cx = hitbox.x + hitbox.width * 0.5f;
        float cy = hitbox.y + hitbox.height * 0.5f;
        float dx = playerPos.x - cx;
        float dy = playerPos.y - cy;
        float dist2 = dx*dx + dy*dy;

        float desiredVx = 0f, desiredVy = 0f;

        // Si está en rango de agro
        if (dist2 < AGGRO_RANGE * AGGRO_RANGE) {
            // Actualizar dirección
            if (Math.abs(dx) > FACE_EPS) facingRight = dx > 0;

            float len = (float)Math.sqrt(dist2);

            // Si está en rango de ataque y puede atacar
            if (len < ATTACK_RANGE && attackCooldown <= 0f && attackLock <= 0f) {
                startAttack();
            } else if (attackLock <= 0f) {
                // Moverse hacia el jugador si no está atacando
                if (len > 1e-5f) {
                    float nx = dx / len;
                    float ny = dy / len;
                    desiredVx = nx * MOVE_SPEED;
                    desiredVy = ny * MOVE_SPEED;
                }
            }
        }

        // Manejar estado de ataque
        if (attackLock > 0f) {
            action = ATTACK;
            attackTimer += dt;

            // Procesar el golpe en el momento adecuado de la animación
            float attackProgress = 1f - (attackLock / ATTACK_LOCK_TIME);

            // Activar la ventana de daño en el momento correcto de la animación
            if (attackProgress >= ATTACK_HIT_TIMING && attackProgress <= ATTACK_HIT_TIMING + 0.3f) {
                if (!attackHitProcessed) {
                    attackHitProcessed = true;
                }
                // Encolar el ataque CADA FRAME durante la ventana de daño
                updateAttackBox();
                EnemyShared.queueEnemyAttack(this, attackBox, DAMAGE);
            }

            desiredVx = 0f;
            desiredVy = 0f;
            updateAnimation(ATTACK_COUNT);
        } else {
            // Determinar acción basada en movimiento
            action = (Math.abs(desiredVx) > 0.01f || Math.abs(desiredVy) > 0.01f) ? RUN : IDLE;
            updateAnimation(getActionFrameCount());
        }

        // Aplicar movimiento con colisión
        if (attackLock <= 0f) { // Solo moverse si no está atacando
            float newX = hitbox.x + desiredVx * dt;
            if (HelpMethods.CanMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, lvlData, tileW, tileH, level)) {
                hitbox.x = newX;
            } else {
                hitbox.x = HelpMethods.GetEntityXPosNextToWall(hitbox, desiredVx * dt, lvlData, tileW, tileH, level);
            }

            float newY = hitbox.y + desiredVy * dt;
            if (HelpMethods.CanMoveHere(hitbox.x, newY, hitbox.width, hitbox.height, lvlData, tileW, tileH, level)) {
                hitbox.y = newY;
            } else {
                hitbox.y = HelpMethods.GetEntityYPosUnderRoofOrAboveFloor(hitbox, desiredVy * dt, lvlData, tileW, tileH, level);
            }
        }
    }

    private void startAttack() {
        action = ATTACK;
        aniIndex = 0;
        aniTick = 0;
        attackTimer = 0f;
        attackLock = ATTACK_LOCK_TIME;
        attackCooldown = ATTACK_COOLDOWN;
        attackHitProcessed = false;

        // Configurar attackBox (más grande para mejor alcance)
        updateAttackBox();
    }

    private void updateAttackBox() {
        float w = (FRAME_W * scale) * 0.8f;
        float h = (FRAME_H * scale) * 0.7f;
        float ax = facingRight ? (hitbox.x + hitbox.width * 0.5f) : (hitbox.x - w + hitbox.width * 0.5f);
        float ay = hitbox.y + hitbox.height * 0.2f;
        attackBox.set(ax, ay, w, h);
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion f = currentFrame();
        float w = FRAME_W * scale;
        float h = FRAME_H * scale;
        boolean wantRight = facingRight;
        float scaleX = (SPRITE_FACES_RIGHT ? (wantRight ? 1f : -1f) : (wantRight ? -1f : 1f));

        // Aplicar fade out durante la muerte
        float alpha = 1f;
        if (dying && deathTimer > 0f) {
            // Fade out gradual durante la muerte
            alpha = Math.min(1f, deathTimer / deathDuration);
            // Flash rojo al recibir daño
        } else if (hitLock > 0f) {
            // Pequeño flash cuando recibe daño
            float hitProgress = hitLock / HIT_LOCK_TIME;
            if (hitProgress > 0.7f) { // Flash en los primeros frames
                alpha = 0.7f + (hitProgress - 0.7f);
            }
        }

        // Guardar color anterior y aplicar alpha
        float oldAlpha = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);

        batch.draw(f, hitbox.x, hitbox.y, w * 0.5f, 0f, w, h, scaleX, 1f, 0f);

        // Restaurar color original
        batch.setColor(1f, 1f, 1f, oldAlpha);

        // DEBUG: Descomentar para visualizar hitboxes
        /*
        batch.end();
        ShapeRenderer sr = new ShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.RED);
        sr.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        if (attackLock > 0f) {
            sr.setColor(Color.YELLOW);
            sr.rect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
        }
        sr.end();
        batch.begin();
        */
    }

    @Override
    public TextureRegion currentFrame() {
        int row, count;
        switch (action) {
            case RUN:    row = RUN_ROW;    count = RUN_COUNT;    break;
            case ATTACK: row = ATTACK_ROW; count = ATTACK_COUNT; break;
            case HIT:    row = HIT_ROW;    count = HIT_COUNT;    break;
            case DEAD:   row = DEAD_ROW;   count = DEAD_COUNT;   break;
            case IDLE:
            default:     row = IDLE_ROW;   count = IDLE_COUNT;   break;
        }
        row = Math.max(0, Math.min(row, frames.length - 1));
        int cols = frames[row].length;
        int col = aniIndex % Math.max(1, Math.min(count, cols));
        return frames[row][col];
    }

    private int getActionFrameCount() {
        switch (action) {
            case RUN:    return RUN_COUNT;
            case ATTACK: return ATTACK_COUNT;
            case HIT:    return HIT_COUNT;
            case DEAD:   return DEAD_COUNT;
            case IDLE:
            default:     return IDLE_COUNT;
        }
    }

    @Override
    protected void onHit() {
        if (dead) return; // No procesar hits si ya está muerto

        // Knockback en dirección opuesta a donde mira
        float knockbackDistance = KNOCKBACK * 0.15f;
        float dir = facingRight ? -1f : 1f;

        // Aplicar knockback con verificación de colisión
        float newX = hitbox.x + dir * knockbackDistance;
        if (HelpMethods.CanMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, lvlData, tileW, tileH, level)) {
            hitbox.x = newX;
        }

        // Cambiar a estado HIT
        action = HIT;
        aniIndex = 0;
        aniTick = 0;
        hitLock = HIT_LOCK_TIME;

        // Cancelar ataque actual si estaba atacando
        if (attackLock > 0f) {
            attackLock = 0f;
            attackHitProcessed = true;
        }
    }

    @Override
    protected void startDeath() {
        super.startDeath(); // Llama al método de la clase base

        // Cancelar cualquier ataque en progreso
        attackLock = 0f;
        attackHitProcessed = true;
        hitLock = 0f;

        // Configurar duración de la animación de muerte
        deathDuration = 1.0f; // 1 segundo para la animación de muerte
    }

    public void setScale(float scale) {
        float cx = hitbox.x + hitbox.width * 0.5f;
        float cy = hitbox.y + hitbox.height * 0.5f;
        this.scale = scale;
        hitbox.setSize(FRAME_W * scale, FRAME_H * scale);
        hitbox.setCenter(cx, cy);
    }

    // Método para debugging
    public Rectangle getAttackBox() {
        return attackBox;
    }

    public boolean isAttacking() {
        return attackLock > 0f;
    }
}
