package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import com.chat.hellbound.entities.Player;
import com.chat.hellbound.entities.EnemyManager;
import com.chat.hellbound.entities.EnemyShared;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.objects.InteractiveObject;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.Constants;
import com.chat.hellbound.utilz.CameraController;
import com.chat.hellbound.ui.TouchControls;
import com.chat.hellbound.input.InputController;
import com.chat.hellbound.utilz.LoadSave;

public class GameScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private LevelManager levelManager;
    private EnemyManager enemyManager;
    private InteractiveObject interactiveObject;
    private CameraController camController;
    private TouchControls touchControls;
    private Player player;

    private final ShapeRenderer debugSR = new ShapeRenderer();
    private final ShapeRenderer uiSR = new ShapeRenderer();
    private final boolean DEBUG = false;

    private boolean paused = false;
    private Rectangle pauseBtnAndroid = new Rectangle();
    private Rectangle exitBtnRect = new Rectangle();


    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    private Texture darknessMask;
    private float darknessAlpha = 1.0f;
    private int maskDiameterPx;

    private int level;

    public GameScreen(Main game, int level) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.level = level;

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        this.viewport.apply();

        this.touchControls = new TouchControls();

        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0f);


        Assets.load();

        Texture atlas = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);
        if (level == 2) {
            atlas = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_TWO_ATLAS);
        }

        levelManager = new LevelManager(atlas);
        enemyManager = new EnemyManager(levelManager);
        interactiveObject = new InteractiveObject(levelManager, level);


        this.player = new Player(700, 5800, levelManager);
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
        rebuildDarknessMask(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }


    private void update(float dt) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }

        if (InputController.isAndroid() && Gdx.input.justTouched()) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            int tx = Gdx.input.getX();
            int ty = sh - Gdx.input.getY();
            if (pauseBtnAndroid.contains(tx, ty)) {
                paused = !paused;
            }
        }

        if (!paused) {
            InputController.update();
            player.update(dt);
            enemyManager.update(dt, player);
            interactiveObject.update(dt);
            if(player.isDead()){
                game.setScreen(new DeathScreen(game));
            }
            if (interactiveObject.allCollected()){
                System.out.println("ya");
                game.setScreen(new WinScreen(game));
            }
            camController.update(dt);
        } else {
            handlePauseOverlayInput();
        }
    }

    private void handlePauseOverlayInput() {
        if (Gdx.input.justTouched()) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            int tx = Gdx.input.getX();
            int ty = sh - Gdx.input.getY();
            if (exitBtnRect.contains(tx, ty)) {
                game.setScreen(new MenuScreen(game));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        levelManager.draw(batch, 0f);
        enemyManager.render(batch);
        interactiveObject.render(batch);
        player.render(batch);
        batch.end();

        if (DEBUG) {
            debugSR.setProjectionMatrix(camera.combined);
            debugSR.begin(ShapeType.Line);
            player.renderDebug(debugSR);
            enemyManager.renderDebug(debugSR);
            debugSR.end();
        }

        renderDarkness();

        renderUiAndPauseOverlay();

        touchControls.render();
    }

    private void renderDarkness() {
        if (darknessMask == null) return;

        float px = getPlayerCenterX();
        float py = getPlayerCenterY();

        float sx = px - camera.position.x + viewport.getWorldWidth() * 0.5f;
        float sy = py - camera.position.y + viewport.getWorldHeight() * 0.5f;

        batch.setProjectionMatrix(camera.combined.cpy().setToOrtho2D(
            0, 0, viewport.getWorldWidth(), viewport.getWorldHeight()));
        batch.begin();
        float drawX = sx - maskDiameterPx / 2f;
        float drawY = sy - maskDiameterPx / 2f;
        batch.draw(darknessMask, drawX, drawY, maskDiameterPx, maskDiameterPx);
        batch.end();
    }

    private float getPlayerCenterX() {
        com.badlogic.gdx.math.Rectangle hb = player.getHitbox();
        return hb.x + hb.width * 0.5f;
    }

    private float getPlayerCenterY() {
        com.badlogic.gdx.math.Rectangle hb = player.getHitbox();
        return hb.y + hb.height * 0.5f;
    }

    private void rebuildDarknessMask(int screenW, int screenH) {
        maskDiameterPx = Math.max(screenW, screenH) * 2;
        if (darknessMask != null) darknessMask.dispose();
        darknessMask = buildRadialDarkTexture(maskDiameterPx, darknessAlpha);
    }

    /**
     * Genera una textura con centro transparente y bordes negros.
     * El alpha sube desde 0 (centro) a darknessAlpha (borde).
     */
    private Texture buildRadialDarkTexture(int size, float alpha) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        float cx = size / 2f;
        float cy = size / 2f;
        float maxR = size / 2f;

        int aMax = (int) (alpha * 255f);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float t = Math.min(1f, d / maxR); // 0 en centro, 1 en borde
                // curva suave (cuadrática) para transición
                float a = t * t;
                int ia = (int) (a * aMax);
                pm.setColor(0f, 0f, 0f, ia / 255f);
                pm.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }


    private void renderUiAndPauseOverlay() {
        if (InputController.isAndroid()) {
            uiSR.setProjectionMatrix(viewport.getCamera().combined.cpy().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
            uiSR.begin(ShapeType.Filled);
            uiSR.setColor(0, 0, 0, 0.35f);
            uiSR.rect(pauseBtnAndroid.x, pauseBtnAndroid.y, pauseBtnAndroid.width, pauseBtnAndroid.height);
            uiSR.end();

            batch.setProjectionMatrix(viewport.getCamera().combined.cpy().setToOrtho2D(
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
            batch.begin();
            layout.setText(font, "||");
            float tx = pauseBtnAndroid.x + (pauseBtnAndroid.width - layout.width) / 2f;
            float ty = pauseBtnAndroid.y + (pauseBtnAndroid.height + layout.height) / 2f;
            font.draw(batch, layout, tx, ty);
            batch.end();
        }

        if (paused) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();

            uiSR.setProjectionMatrix(viewport.getCamera().combined.cpy().setToOrtho2D(0, 0, sw, sh));
            uiSR.begin(ShapeType.Filled);
            uiSR.setColor(0f, 0f, 0f, 0.55f);
            uiSR.rect(0, 0, sw, sh);

            float panelW = Math.min(420, (int) (sw * 0.8f));
            float panelH = 220;
            float px = (sw - panelW) / 2f;
            float py = (sh - panelH) / 2f;
            uiSR.setColor(0.1f, 0.1f, 0.12f, 0.92f);
            uiSR.rect(px, py, panelW, panelH);

            float btnW = panelW * 0.6f;
            float btnH = 50f;
            float bx = px + (panelW - btnW) / 2f;
            float by = py + 30f;
            exitBtnRect.set(bx, by, btnW, btnH);

            uiSR.setColor(0.2f, 0.2f, 0.25f, 1f);
            uiSR.rect(exitBtnRect.x, exitBtnRect.y, exitBtnRect.width, exitBtnRect.height);
            uiSR.end();

            batch.setProjectionMatrix(viewport.getCamera().combined.cpy().setToOrtho2D(0, 0, sw, sh));
            batch.begin();
            layout.setText(font, "Pausa");
            font.draw(batch, layout, px + (panelW - layout.width) / 2f, py + panelH - 40);

            layout.setText(font, "Salir");
            font.draw(batch, layout,
                exitBtnRect.x + (exitBtnRect.width - layout.width) / 2f,
                exitBtnRect.y + (exitBtnRect.height + layout.height) / 2f);
            batch.end();
        }
    }

    private void computeUiRects(int sw, int sh) {
        float sz = Math.max(40, Math.min(sw, sh) * 0.07f);
        pauseBtnAndroid.set(12, sh - sz - 12, sz, sz);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        InputController.invalidateLayout();
        computeUiRects(width, height);
        rebuildDarknessMask(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        levelManager.dispose();
        Assets.dispose();
        touchControls.dispose();
        debugSR.dispose();
        uiSR.dispose();
        if (darknessMask != null) darknessMask.dispose();
        font.dispose();
    }
}
