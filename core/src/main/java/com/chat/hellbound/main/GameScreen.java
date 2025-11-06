package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.chat.hellbound.entities.Player;
import com.chat.hellbound.input.InputController;
import com.chat.hellbound.levels.LevelManager;
import com.chat.hellbound.utilz.Assets;
import com.chat.hellbound.utilz.Constants;
import com.chat.hellbound.utilz.CameraController;

public class GameScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private LevelManager levelManager;
    private CameraController camController;

    private final Player player;

    public GameScreen(Main game) {
        this.game = game;
        this.batch = new SpriteBatch();

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        this.viewport.apply();
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0f);

        Assets.load();

        levelManager = new LevelManager();
        this.player = new Player(140, 210, levelManager);

        float worldW = levelManager.getWorldWidthPx();
        float worldH = levelManager.getWorldHeightPx();

        camController = new CameraController(
            camera,
            worldW, worldH,
            Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT
        );
        camController.setLerp(0.22f);
        camController.setPrimaryTarget(player);
    }

    private void update(float dt) {
        InputController.update();
        player.update(dt);
        camController.update(dt);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        levelManager.draw(batch, 0f);
        player.render(batch);
        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        levelManager.dispose();
        Assets.dispose();
    }
}
