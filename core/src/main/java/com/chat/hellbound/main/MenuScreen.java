package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.chat.hellbound.gamestates.Gamestate;
import com.chat.hellbound.ui.MenuButton;
import com.chat.hellbound.input.InputController;
import com.chat.hellbound.utilz.Constants;

public class MenuScreen implements Screen {

    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final ShapeRenderer sr;
    private final SpriteBatch batch;
    private final BitmapFont font;

    private MenuButton btnPlay, btnMulti, btnOptions, btnExit;

    public MenuScreen(Main game) {
        this.game = game;

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0f);

        sr = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        float bw = 360f;
        float bh = 80f;
        float gap = 22f;
        float startY = Constants.WORLD_HEIGHT * 0.55f;
        float cx = (Constants.WORLD_WIDTH - bw) * 0.5f;

        btnPlay    = new MenuButton(viewport, cx, startY,                 bw, bh, "Jugar");
        btnMulti   = new MenuButton(viewport, cx, startY - (bh + gap),    bw, bh, "Multijugador");
        btnOptions = new MenuButton(viewport, cx, startY - (bh + gap) * 2, bw, bh, "Opciones");
        btnExit    = new MenuButton(viewport, cx, startY - (bh + gap) * 3, bw, bh, "Salir");
    }

    @Override public void show() {}

    private void update(float dt) {
        InputController.update();

        if (btnPlay.isClicked()) {
            game.setState(Gamestate.PLAYING);
        }
        if (btnExit.isClicked()) {
            game.setState(Gamestate.EXIT);
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.10f, 0.10f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.07f, 0.07f, 0.09f, 1f);
        sr.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        btnPlay.render(sr);
        btnMulti.render(sr);
        btnOptions.render(sr);
        btnExit.render(sr);
        sr.end();

        batch.begin();
        font.getData().setScale(1.8f);
        font.draw(batch, "Hellbound", Constants.WORLD_WIDTH * 0.5f - 96f, Constants.WORLD_HEIGHT - 48f);

        font.getData().setScale(1.2f);
        drawButtonLabel(batch, btnPlay);
        drawButtonLabel(batch, btnMulti);
        drawButtonLabel(batch, btnOptions);
        drawButtonLabel(batch, btnExit);

        batch.end();
    }

    private void drawButtonLabel(SpriteBatch batch, MenuButton b) {
        float x = b.getBounds().x + 24f;
        float y = b.getBounds().y + 52f;
        font.draw(batch, b.getText(), x, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        InputController.invalidateLayout();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        sr.dispose();
        batch.dispose();
        font.dispose();
    }
}
