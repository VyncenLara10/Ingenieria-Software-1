package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.chat.hellbound.utilz.Constants;

public class WinScreen implements Screen {

    private final Main game;
    private OrthographicCamera cam;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer sr;
    private BitmapFont font;
    private GlyphLayout layout;

    private final Color bg = new Color(0.06f, 0.08f, 0.06f, 1f);
    private final Color title = new Color(0.98f, 0.88f, 0.25f, 1f); // dorado
    private final Color subtitle = new Color(0.90f, 0.92f, 0.97f, 1f);

    public WinScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        cam = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, cam);
        viewport.apply(true);
        cam.position.set(Constants.WORLD_WIDTH/2f, Constants.WORLD_HEIGHT/2f, 0);
        cam.update();

        batch = new SpriteBatch();
        sr = new ShapeRenderer();
        font = new BitmapFont();
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.justTouched()) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(bg.r, bg.g, bg.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // fondo
        sr.setProjectionMatrix(cam.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(bg);
        sr.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        sr.setColor(0, 0, 0, 0.28f);
        sr.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT * 0.15f);
        sr.rect(0, Constants.WORLD_HEIGHT * 0.85f, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT * 0.15f);
        sr.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        String t = "YOU WIN";
        font.setColor(title);
        layout.setText(font, t);
        float ty = Constants.WORLD_HEIGHT * 0.62f;
        font.draw(batch, t, (Constants.WORLD_WIDTH - layout.width)/2f, ty);

        String sub = "Toca la pantalla para volver al menú";
        font.setColor(subtitle);
        layout.setText(font, sub);
        font.draw(batch, sub, (Constants.WORLD_WIDTH - layout.width)/2f, ty - 80f, 0, Align.left, false);

        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        sr.dispose();
        batch.dispose();
        font.dispose();
    }
}
