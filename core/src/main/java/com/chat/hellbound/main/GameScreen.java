package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.chat.hellbound.entities.Player;
import com.chat.hellbound.entities.EnemyManager;
import com.chat.hellbound.entities.EnemyShared;
import com.chat.hellbound.entities.BossManager;
import com.chat.hellbound.entities.HellGuardian;
import com.chat.hellbound.entities.TreeBoss;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.objects.InteractiveObject;
import com.chat.hellbound.utilz.*;
import com.chat.hellbound.ui.TouchControls;
import com.chat.hellbound.input.InputController;
import com.chat.hellbound.multiplayer.MultiplayerMode;
import com.chat.hellbound.multiplayer.HostSession;
import com.chat.hellbound.multiplayer.ClientSession;
import com.chat.hellbound.multiplayer.PlayerSnapshot;

public class GameScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private LevelManager levelManager;
    private EnemyManager enemyManager;
    private BossManager bossManager;
    private InteractiveObject interactiveObject;
    private CameraController camController;
    private TouchControls touchControls;
    private Player player;
    private Player remotePlayer;

    private final ShapeRenderer debugSR = new ShapeRenderer();
    private final ShapeRenderer uiSR = new ShapeRenderer();
    private final boolean DEBUG = false;

    private boolean paused = false;
    private Rectangle pauseBtnAndroid = new Rectangle();
    private Rectangle exitBtnRect = new Rectangle();

    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    private DarknessHandler darknessHandler;

    // -------- MULTIPLAYER --------
    private MultiplayerMode multiplayerMode = MultiplayerMode.OFFLINE;
    private HostSession hostSession;
    private ClientSession clientSession;
    private boolean remoteActive = false;

    private int level;

    // ----------------------------
    //      CONSTRUCTOR SP
    // ----------------------------
    public GameScreen(Main game, int level) {

        this.game = game;
        this.batch = new SpriteBatch();
        this.level = level;

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        this.viewport.apply();

        InputController.setViewport(viewport);
        this.touchControls = new TouchControls();

        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0f);

        Assets.load();

        Texture atlas = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);
        if (level == 2) atlas = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_TWO_ATLAS);

        levelManager = new LevelManager(atlas, level);
        enemyManager = new EnemyManager(levelManager, level);
        bossManager = new BossManager();
        interactiveObject = new InteractiveObject(levelManager, level);

        this.player = new Player(700, 5800, levelManager, level);
        EnemyShared.hookPlayer(player);
        player.SetObject("SprintBurst");

        float worldW = levelManager.getWorldWidthPx();
        float worldH = levelManager.getWorldHeightPx();

        camController = new CameraController(
            camera,
            worldW, worldH,
            Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT
        );
        camController.setLerp(0.22f);
        camController.setPrimaryTarget(player);

        computeUiRects(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        darknessHandler = new DarknessHandler(Gdx.graphics.getWidth() + 500, Gdx.graphics.getHeight() + 500, 2.0f);
    }

    // ----------------------------
    //      CONSTRUCTOR MP
    // ----------------------------
    public GameScreen(Main game, int level, MultiplayerMode mode,
                      HostSession hostSession, ClientSession clientSession) {

        // --------- COSAS BÁSICAS ---------
        this.game = game;
        this.batch = new SpriteBatch();
        this.level = level;
        this.multiplayerMode = (mode != null) ? mode : MultiplayerMode.OFFLINE;

        this.hostSession = hostSession;
        this.clientSession = clientSession;

        // --------- CÁMARA + VIEWPORT ---------
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        this.viewport.apply();
        InputController.setViewport(viewport);

        this.touchControls = new TouchControls();

        // MUY IMPORTANTE: centrar la cámara
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0f);

        // --------- ASSETS ---------
        Assets.load();

        Texture atlas = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);
        if (level == 2) {
            atlas = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_TWO_ATLAS);
        }

        // --------- NIVEL + SISTEMAS ---------
        levelManager = new LevelManager(atlas, level);

        // 🔥 HOST: mandar mapa al cliente apenas se crea
        if (multiplayerMode == MultiplayerMode.HOST && hostSession != null) {
            hostSession.setLevelData(levelManager.getLevelData());
        }

        enemyManager = new EnemyManager(levelManager, level);
        bossManager = new BossManager();
        interactiveObject = new InteractiveObject(levelManager, level);

        // --------- JUGADOR LOCAL ---------
        player = new Player(700, 5800, levelManager, level);
        EnemyShared.hookPlayer(player);
        player.SetObject("SprintBurst");

        // --------- JUGADOR REMOTO ---------
        if (multiplayerMode != MultiplayerMode.OFFLINE) {
            remotePlayer = new Player(700, 5800, levelManager, level);
        }

        // --------- CÁMARA FOLLOW ---------
        float worldW = levelManager.getWorldWidthPx();
        float worldH = levelManager.getWorldHeightPx();

        camController = new CameraController(
            camera,
            worldW, worldH,
            Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT
        );
        camController.setLerp(0.22f);
        camController.setPrimaryTarget(player);

        // --------- UI / PAUSA / OSCURIDAD ---------
        computeUiRects(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        darknessHandler = new DarknessHandler(
            Gdx.graphics.getWidth() + 500,
            Gdx.graphics.getHeight() + 500,
            2.0f
        );
    }

    private void update(float dt) {

        // Pausa
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }
        if (paused) return;

        InputController.update();
        player.update(dt);

        // ============================
        //   CLIENTE — Cargar mapa UNA VEZ
        // ============================
        if (multiplayerMode == MultiplayerMode.CLIENT && clientSession != null) {
            int[][] map = clientSession.getReceivedMap();
            if (map != null && !clientSession.isMapApplied()) {
                levelManager.setLevelData(map);
                enemyManager = new EnemyManager(levelManager, level);
                interactiveObject = new InteractiveObject(levelManager, level);
                clientSession.setMapApplied();
                Gdx.app.log("NET", "Mapa aplicado al cliente");
            }
        }


        // ============================
        //   SNAPSHOTS
        // ============================
        if (multiplayerMode == MultiplayerMode.HOST && hostSession != null && hostSession.isConnected()) {

            hostSession.sendLocalSnapshot(player);

            PlayerSnapshot snap = hostSession.getRemoteSnapshot();
            if (snap != null && remotePlayer != null) {
                snap.applyTo(remotePlayer);
                remoteActive = true;
            }

            if (remoteActive) remotePlayer.update(dt);

        } else if (multiplayerMode == MultiplayerMode.CLIENT && clientSession != null) {

            clientSession.sendLocalSnapshot(player);

            PlayerSnapshot snap = clientSession.getRemoteSnapshot();
            if (snap != null && remotePlayer != null) {
                snap.applyTo(remotePlayer);
                remoteActive = true;
            }

            if (remoteActive) remotePlayer.update(dt);
        }

        // ============================
        //   ENEMIGOS Y OBJETOS
        // ============================
        enemyManager.update(dt, player);
        bossManager.update(dt, player);
        interactiveObject.update(dt);

        // Boss 1
        if (!bossManager.isBossEncounterActive()
            && interactiveObject.getCollectedCount() == 2) {

            float px = player.getHitbox().x;
            float py = player.getHitbox().y;

            HellGuardian boss = new HellGuardian(px + 200f, py, levelManager);
            bossManager.addBoss(boss);
            bossManager.triggerBossEncounter(0);
        }

        // Boss 2
        if (!bossManager.isBossEncounterActive()
            && interactiveObject.allCollected()) {

            float px = player.getHitbox().x;
            float py = player.getHitbox().y;

            TreeBoss boss = new TreeBoss(px + 200f, py, levelManager);
            bossManager.addBoss(boss);
            bossManager.triggerBossEncounter(0);
        }

        // Muerte / Victoria
        if (player.isDead()) {
            game.setScreen(new DeathScreen(game));
        }

        boolean bossDead = (bossManager.getActiveBoss() == null
            || bossManager.getActiveBoss().isDefeated());

        if (interactiveObject.allCollected() && bossDead) {
            game.setScreen(new WinScreen(game));
        }

        camController.update(dt);
    }

    @Override
    public void render(float delta) {

        update(delta);

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // WORLD
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        levelManager.draw(batch, 0f);
        enemyManager.render(batch);
        bossManager.render(batch);
        interactiveObject.render(batch);
        player.render(batch);

        if (remoteActive && remotePlayer != null && multiplayerMode != MultiplayerMode.OFFLINE)
            remotePlayer.render(batch);

        batch.end();

        // Luz / Oscuridad
        darknessHandler.render(
            batch,
            getPlayerCenterX(),
            getPlayerCenterY(),
            camera.position.x,
            camera.position.y,
            viewport.getWorldWidth(),
            viewport.getWorldHeight()
        );

        // UI
        renderUiAndPauseOverlay();

        // Controles Android
        touchControls.render();
    }

    private float getPlayerCenterX() {
        Rectangle hb = player.getHitbox();
        return hb.x + hb.width * 0.5f;
    }

    private float getPlayerCenterY() {
        Rectangle hb = player.getHitbox();
        return hb.y + hb.height * 0.5f;
    }

    private void renderUiAndPauseOverlay() {

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        batch.setProjectionMatrix(viewport.getCamera().combined.cpy().setToOrtho2D(0, 0, sw, sh));
        batch.begin();

        String txt = "Objetos: " + interactiveObject.getCollectedCount() + "/" + interactiveObject.getTotalObjects();
        layout.setText(font, txt);
        font.draw(batch, layout, 20f, sh - 20f);

        if (bossManager.isBossEncounterActive()) {
            layout.setText(font, "¡BOSS!");
            font.setColor(Color.RED);
            font.draw(batch, layout, (sw - layout.width) / 2f, sh - 20f);
        }

        batch.end();
    }

    private void computeUiRects(int sw, int sh) {
        float sz = Math.max(40, Math.min(sw, sh) * 0.07f);
        pauseBtnAndroid.set(12, sh - sz - 12, sz, sz);
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void resize(int w, int h) {
        viewport.update(w, h, true);
        computeUiRects(w, h);
    }

    @Override
    public void dispose() {
        batch.dispose();
        levelManager.dispose();
        Assets.dispose();
        touchControls.dispose();
        debugSR.dispose();
        uiSR.dispose();
        darknessHandler.dispose();
        font.dispose();
    }
}
