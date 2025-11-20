package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputAdapter;
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

    private Color bgTop = new Color(0.06f, 0.06f, 0.10f, 1f);
    private Color bgBottom = new Color(0.02f, 0.02f, 0.05f, 1f);
    private Color titleColor = new Color(0.9f, 0.9f, 0.95f, 1f);
    private Color btnColor = new Color(0.18f, 0.18f, 0.22f, 1f);
    private Color btnHoverColor = new Color(0.30f, 0.30f, 0.40f, 1f);
    private Color btnTextColor = new Color(0.95f, 0.95f, 0.98f, 1f);
    private Color nameBoxColor = new Color(0.12f, 0.12f, 0.16f, 1f);
    private Color nameBoxActiveColor = new Color(0.20f, 0.20f, 0.30f, 1f);

    private Rectangle btnPlay = new Rectangle();
    private Rectangle btnHost = new Rectangle();
    private Rectangle btnJoin = new Rectangle();
    private Rectangle btnExit = new Rectangle();
    private Rectangle nameRect = new Rectangle();

    private Vector3 touchVec = new Vector3();

    private String playerName = "Jugador";
    private boolean nameActive = false;

    public MenuScreen(Main game) {
        this.game = game;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, cam);
        viewport.apply();

        batch = new SpriteBatch();
        sr = new ShapeRenderer();
        font = new BitmapFont();
        layout = new GlyphLayout();

        computeLayout();
        setupInput();
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (!nameActive) return false;
                if (character == '\b') {
                    if (playerName.length() > 0) {
                        playerName = playerName.substring(0, playerName.length() - 1);
                    }
                    return true;
                }
                if (character == '\r' || character == '\n') {
                    nameActive = false;
                    return true;
                }
                if (playerName.length() >= 16) return false;
                if (character >= 32 && character <= 126) {
                    playerName += character;
                    return true;
                }
                return false;
            }
        });
    }

    private void computeLayout() {
        float w = Constants.WORLD_WIDTH;
        float h = Constants.WORLD_HEIGHT;

        float nameWidth = w * 0.5f;
        float nameHeight = 60f;
        float nameX = (w - nameWidth) / 2f;
        float nameY = h * 0.62f;
        nameRect.set(nameX, nameY, nameWidth, nameHeight);

        float btnWidth = w * 0.45f;
        float btnHeight = 75f;
        float spacing = 25f;

        float totalHeight = btnHeight * 4f + spacing * 3f;
        float startY = h / 2f - totalHeight / 2f - 60f;

        float x = (w - btnWidth) / 2f;

        btnPlay.set(x, startY + (btnHeight + spacing) * 3, btnWidth, btnHeight);
        btnHost.set(x, startY + (btnHeight + spacing) * 2, btnWidth, btnHeight);
        btnJoin.set(x, startY + (btnHeight + spacing) * 1, btnWidth, btnHeight);
        btnExit.set(x, startY, btnWidth, btnHeight);
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);
            float tx = touchVec.x;
            float ty = touchVec.y;

            if (nameRect.contains(tx, ty)) {
                nameActive = true;
                if (Gdx.app.getType() == Application.ApplicationType.Android) {
                    Gdx.input.setOnscreenKeyboardVisible(true);
                }
                return;
            } else {
                nameActive = false;
            }

            if (btnPlay.contains(tx, ty)) {
                game.setScreen(new GameScreen(game, 1));
                return;
            }
            if (btnHost.contains(tx, ty)) {
                String name = playerName.trim().isEmpty() ? "Host" : playerName.trim();
                game.setScreen(new LobbyScreen(game, true,name));
                return;
            }
            if (btnJoin.contains(tx, ty)) {
                String name = playerName.trim().isEmpty() ? "Client" : playerName.trim();
                game.setScreen(new LobbyScreen(game, false,name));
                return;
            }
            if (btnExit.contains(tx, ty)) {
                Gdx.app.exit();
            }
        }
    }

    private boolean isHovered(Rectangle r) {
        if (!Gdx.input.isTouched()) return false;
        touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchVec);
        return r.contains(touchVec.x, touchVec.y);
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(bgTop.r, bgTop.g, bgTop.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(cam.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(bgBottom);
        sr.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        sr.setColor(bgTop);
        sr.rect(0, Constants.WORLD_HEIGHT / 3f, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT * 2f / 3f);

        Color playColor = isHovered(btnPlay) ? btnHoverColor : btnColor;
        Color hostColor = isHovered(btnHost) ? btnHoverColor : btnColor;
        Color joinColor = isHovered(btnJoin) ? btnHoverColor : btnColor;
        Color exitColor = isHovered(btnExit) ? btnHoverColor : btnColor;

        sr.setColor(playColor);
        sr.rect(btnPlay.x, btnPlay.y, btnPlay.width, btnPlay.height);
        sr.setColor(hostColor);
        sr.rect(btnHost.x, btnHost.y, btnHost.width, btnHost.height);
        sr.setColor(joinColor);
        sr.rect(btnJoin.x, btnJoin.y, btnJoin.width, btnJoin.height);
        sr.setColor(exitColor);
        sr.rect(btnExit.x, btnExit.y, btnExit.width, btnExit.height);

        sr.setColor(nameActive ? nameBoxActiveColor : nameBoxColor);
        sr.rect(nameRect.x, nameRect.y, nameRect.width, nameRect.height);
        sr.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        font.setColor(titleColor);
        font.getData().setScale(2.0f);
        String title = "HELLBOUND";
        layout.setText(font, title, titleColor, 0, Align.left, false);
        float titleX = (Constants.WORLD_WIDTH - layout.width) / 2f;
        float titleY = Constants.WORLD_HEIGHT * 0.85f;
        font.draw(batch, layout, titleX, titleY);

        font.setColor(btnTextColor);
        font.getData().setScale(1.2f);
        String label = "Nombre:";
        layout.setText(font, label);
        float labelX = nameRect.x;
        float labelY = nameRect.y + nameRect.height + 30f;
        font.draw(batch, layout, labelX, labelY);

        String shownName = playerName.isEmpty() ? "" : playerName;
        layout.setText(font, shownName);
        float nameTextX = nameRect.x + 16f;
        float nameTextY = nameRect.y + nameRect.height / 2f + layout.height / 2f;
        font.draw(batch, layout, nameTextX, nameTextY);

        font.setColor(btnTextColor);
        font.getData().setScale(1.4f);
        drawCenteredText("Jugar", btnPlay);
        drawCenteredText("Host Online", btnHost);
        drawCenteredText("Join Online", btnJoin);
        drawCenteredText("Salir", btnExit);

        batch.end();
    }

    private void drawCenteredText(String text, Rectangle rect) {
        layout.setText(font, text, btnTextColor, 0, Align.left, false);
        float x = rect.x + (rect.width - layout.width) / 2f;
        float y = rect.y + rect.height / 2f + layout.height / 2f;
        font.draw(batch, layout, x, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        computeLayout();
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        sr.dispose();
        batch.dispose();
        font.dispose();
    }
}
