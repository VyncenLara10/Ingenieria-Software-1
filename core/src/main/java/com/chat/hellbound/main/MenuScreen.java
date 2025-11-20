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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.chat.hellbound.utilz.Constants;

public class MenuScreen implements Screen {

    private final Main game;

    private OrthographicCamera cam;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer sr;
    private BitmapFont font;
    private GlyphLayout layout;
    private final Vector3 touch = new Vector3();

    // Estado: ¿mostrando selección de nivel?
    private boolean showLevelSelect = false;

    // Botones del menú principal (sin "Opciones")
    private Rectangle btnPlay;
    private Rectangle btnMultiplayer;
    private Rectangle btnExit;

    // Botones de selección de nivel
    private Rectangle btnBosque;
    private Rectangle btnTeatro;
    private Rectangle btnAtras;

    // Colores de estilo
    private final Color bgTop = new Color(0.07f, 0.07f, 0.09f, 1f);
    private final Color bgBottom = new Color(0.10f, 0.10f, 0.14f, 1f);
    private final Color btnBorder = new Color(0.95f, 0.15f, 0.15f, 1f); // rojo
    private final Color btnFill = new Color(0.14f, 0.14f, 0.18f, 1f);
    private final Color btnFillHover = new Color(0.18f, 0.18f, 0.23f, 1f);
    private final Color titleColor = new Color(0.95f, 0.95f, 0.98f, 1f);
    private final Color textColor = new Color(0.92f, 0.92f, 0.95f, 1f);

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        cam = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, cam);
        viewport.apply(true);
        cam.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0);
        cam.update();

        batch = new SpriteBatch();
        sr = new ShapeRenderer();
        font = new BitmapFont(); // usa default; reemplaza por tu propia fuente si la tienes
        layout = new GlyphLayout();

        // Layout de botones del menú principal
        float bw = 380f;
        float bh = 80f;
        float gap = 22f;
        float startY = Constants.WORLD_HEIGHT * 0.55f;

        btnPlay        = new Rectangle((Constants.WORLD_WIDTH - bw)/2f, startY, bw, bh);
        btnMultiplayer = new Rectangle((Constants.WORLD_WIDTH - bw)/2f, startY - (bh+gap)*1, bw, bh);
        // btnOptions eliminado
        btnExit        = new Rectangle((Constants.WORLD_WIDTH - bw)/2f, startY - (bh+gap)*2, bw, bh);

        // Layout de botones de selección de nivel
        float selWidth = 360f;
        float selHeight = 90f;
        float selGap = 26f;
        float selStartY = Constants.WORLD_HEIGHT * 0.55f;

        btnBosque = new Rectangle((Constants.WORLD_WIDTH - selWidth)/2f, selStartY, selWidth, selHeight);
        btnTeatro = new Rectangle((Constants.WORLD_WIDTH - selWidth)/2f, selStartY - (selHeight + selGap), selWidth, selHeight);
        btnAtras  = new Rectangle((Constants.WORLD_WIDTH - 220f)/2f, Constants.WORLD_HEIGHT * 0.20f, 220f, 70f);
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(bgTop.r, bgTop.g, bgTop.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Fondo con “vignette” simple (dos capas rectangulares)
        sr.setProjectionMatrix(cam.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        // base
        sr.setColor(bgBottom);
        sr.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        // banda superior ligeramente más oscura
        sr.setColor(bgTop);
        sr.rect(0, Constants.WORLD_HEIGHT * 0.75f, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT * 0.25f);
        // banda inferior
        sr.setColor(new Color(bgTop.r, bgTop.g, bgTop.b, 1f));
        sr.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT * 0.12f);
        sr.end();

        // Título
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        String title = showLevelSelect ? "Selecciona un nivel" : "Hellbound";
        font.setColor(titleColor);
        layout.setText(font, title);
        font.draw(batch, title, (Constants.WORLD_WIDTH - layout.width)/2f, Constants.WORLD_HEIGHT * 0.82f);
        batch.end();

        // Dibujo de botones según estado
        if (!showLevelSelect) {
            drawButton(btnPlay, "Jugar");
            drawButton(btnMultiplayer, "Multiplayer");
            drawButton(btnExit, "Salir");
        } else {
            drawButton(btnBosque, "Bosque");
            drawButton(btnTeatro, "Teatro");
            drawButton(btnAtras, "Atrás");
        }
    }

    private void drawButton(Rectangle r, String text) {
        Vector3 mp = getMouseWorld();
        boolean hovered = r.contains(mp.x, mp.y);

        // Sombra
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, hovered ? 0.35f : 0.25f);
        drawRoundedRect(r.x + 4f, r.y - 4f, r.width, r.height, 16f);
        sr.end();

        // Borde (rojo) + relleno (hover cambia)
        sr.begin(ShapeRenderer.ShapeType.Filled);
        // borde
        sr.setColor(btnBorder);
        drawRoundedRect(r.x, r.y, r.width, r.height, 16f);
        // relleno interior (encoger para “grosor” de borde)
        float pad = 3.5f;
        sr.setColor(hovered ? btnFillHover : btnFill);
        drawRoundedRect(r.x + pad, r.y + pad, r.width - pad*2f, r.height - pad*2f, 13f);
        sr.end();

        // Texto centrado
        batch.begin();
        font.setColor(textColor);
        layout.setText(font, text);
        float tx = r.x + (r.width - layout.width)/2f;
        float ty = r.y + (r.height + layout.height)/2f;
        font.draw(batch, text, tx, ty, 0, Align.left, false);
        batch.end();
    }

    // Dibuja un rectángulo con esquinas redondeadas usando ShapeRenderer (Filled)
    private void drawRoundedRect(float x, float y, float w, float h, float radius) {
        float r = Math.min(radius, Math.min(w, h) / 2f);

        // centro
        sr.rect(x + r, y, w - 2*r, h);
        sr.rect(x, y + r, w, h - 2*r);

        // esquinas (cuartos de círculo)
        sr.circle(x + r, y + r, r, 16);                 // inferior izquierda
        sr.circle(x + w - r, y + r, r, 16);             // inferior derecha
        sr.circle(x + r, y + h - r, r, 16);             // superior izquierda
        sr.circle(x + w - r, y + h - r, r, 16);         // superior derecha
    }

    private Vector3 getMouseWorld() {
        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(touch);
        return touch;
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        Vector3 wp = getMouseWorld();
        float x = wp.x, y = wp.y;

        if (!showLevelSelect) {
            if (btnPlay.contains(x, y)) {
                showLevelSelect = true;
                return;
            }
            if (btnMultiplayer.contains(x, y)) {
                // aquí podrías abrir otra pantalla de multiplayer
                return;
            }
            if (btnExit.contains(x, y)) {
                Gdx.app.exit();
                return;
            }
        } else {
            if (btnBosque.contains(x, y)) {
                game.setScreen(new GameScreen(game, 1));
                return;
            }
            if (btnTeatro.contains(x, y)) {
                game.setScreen(new GameScreen(game, 2));
                return;
            }
            if (btnAtras.contains(x, y)) {
                showLevelSelect = false;
                return;
            }
        }
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
