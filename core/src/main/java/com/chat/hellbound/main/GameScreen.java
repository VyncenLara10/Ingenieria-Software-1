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
import com.chat.hellbound.utilz.*;
import com.chat.hellbound.ui.TouchControls;
import com.chat.hellbound.input.InputController;

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

    private DarknessHandler darknessHandler;

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

        levelManager = new LevelManager(atlas, level);
        enemyManager = new EnemyManager(levelManager, level);
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
        darknessHandler = new DarknessHandler(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 2.0f);

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

        darknessHandler.render(batch, getPlayerCenterX(), getPlayerCenterY(), camera.position.x, camera.position.y, viewport.getWorldWidth(), viewport.getWorldHeight());

        renderUiAndPauseOverlay();

        touchControls.render();
    }


    private float getPlayerCenterX() {
        com.badlogic.gdx.math.Rectangle hb = player.getHitbox();
        return hb.x + hb.width * 0.5f;
    }

    private float getPlayerCenterY() {
        com.badlogic.gdx.math.Rectangle hb = player.getHitbox();
        return hb.y + hb.height * 0.5f;
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
        darknessHandler.rebuild(width, height);
        InputController.setViewport(viewport);
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
        darknessHandler.dispose();
        //if (darknessMask != null) darknessMask.dispose();
        font.dispose();
    }
}
