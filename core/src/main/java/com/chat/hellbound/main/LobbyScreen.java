package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Application;
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

import com.chat.hellbound.multiplayer.MultiplayerMode;
import com.chat.hellbound.multiplayer.HostSession;
import com.chat.hellbound.multiplayer.ClientSession;
import com.chat.hellbound.utilz.Constants;

public class LobbyScreen implements Screen {

    private final Main game;
    private final boolean isHost;
    private final String localName;

    private OrthographicCamera cam;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer sr;
    private BitmapFont font;
    private GlyphLayout layout;
    private Vector3 touchVec = new Vector3();

    private Rectangle btnBosque = new Rectangle();
    private Rectangle btnTeatro = new Rectangle();
    private Rectangle btnBack = new Rectangle();

    private Color bgColor = new Color(0.05f, 0.05f, 0.09f, 1f);
    private Color panelColor = new Color(0.12f, 0.12f, 0.18f, 1f);
    private Color btnColor = new Color(0.18f, 0.18f, 0.24f, 1f);
    private Color btnHoverColor = new Color(0.30f, 0.30f, 0.40f, 1f);
    private Color textColor = new Color(0.95f, 0.95f, 0.98f, 1f);

    private MultiplayerMode mode;
    private HostSession hostSession;
    private ClientSession clientSession;
    private Thread netThread;

    public LobbyScreen(Main game, boolean isHost, String localName) {
        this.game = game;
        this.isHost = isHost;
        this.localName = localName;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, cam);
        viewport.apply();

        batch = new SpriteBatch();
        sr = new ShapeRenderer();
        font = new BitmapFont();
        layout = new GlyphLayout();

        computeLayout();

        if (isHost) {
            mode = MultiplayerMode.HOST;

            // 👉 CORRECCIÓN: El HostSession ahora necesita 3 parámetros
            hostSession = new HostSession(7777, localName); // nivel por defecto

            netThread = new Thread(hostSession, "HostSessionThread");
            netThread.start();

        } else {

            mode = MultiplayerMode.CLIENT;

            String ip;
            if (Gdx.app.getType() == Application.ApplicationType.Android) {
                ip = "10.0.2.2";
            } else {
                ip = "127.0.0.1";
            }

            clientSession = new ClientSession(ip, 7777, localName);
            netThread = new Thread(clientSession, "ClientSessionThread");
            netThread.start();
        }
    }

    private void computeLayout() {
        float w = Constants.WORLD_WIDTH;
        float h = Constants.WORLD_HEIGHT;

        float panelW = w * 0.7f;
        float panelH = h * 0.5f;
        float panelX = (w - panelW) / 2f;
        float panelY = (h - panelH) / 2f;

        float btnWidth = 190f;
        float btnHeight = 60f;
        float spacing = 30f;

        float bx = panelX + 40f;
        float by = panelY + 50f;

        btnBosque.set(bx, by, btnWidth, btnHeight);
        btnTeatro.set(bx + btnWidth + spacing, by, btnWidth, btnHeight);

        float backW = 170f;
        float backH = 55f;
        btnBack.set(panelX + panelW - backW - 40f, by, backW, backH);
    }

    private boolean isHovered(Rectangle r) {
        if (!Gdx.input.isTouched()) return false;
        touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchVec);
        return r.contains(touchVec.x, touchVec.y);
    }

    private void handleInput() {

        if (Gdx.input.justTouched()) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            float tx = touchVec.x;
            float ty = touchVec.y;

            if (btnBack.contains(tx, ty)) {

                if (hostSession != null) hostSession.stop();
                if (clientSession != null) clientSession.stop();

                game.setScreen(new MenuScreen(game));
                return;
            }

            if (isHost) {

                if (btnBosque.contains(tx, ty)) {
                    int level = 1;

                    // 👉 CORRECCIÓN
                    if (hostSession != null) hostSession.sendLevelSelection(level);


                    game.setScreen(new GameScreen(game, level, MultiplayerMode.HOST, hostSession, null));
                    return;
                }

                if (btnTeatro.contains(tx, ty)) {
                    int level = 2;

                    // 👉 CORRECCIÓN
                    if (hostSession != null) hostSession.sendLevelSelection(level);


                    game.setScreen(new GameScreen(game, level, MultiplayerMode.HOST, hostSession, null));
                    return;
                }
            }
        }
    }

    @Override
    public void render(float delta) {

        handleInput();

        // 👉 CLIENTE: si recibe un nivel válido, entra inmediatamente
        if (!isHost && clientSession != null) {
            int lvl = clientSession.getSelectedLevel();
            if (lvl == 1 || lvl == 2) {
                game.setScreen(new GameScreen(game, lvl, MultiplayerMode.CLIENT, null, clientSession));
                return;
            }
        }

        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float w = Constants.WORLD_WIDTH;
        float h = Constants.WORLD_HEIGHT;

        float panelW = w * 0.7f;
        float panelH = h * 0.5f;
        float panelX = (w - panelW) / 2f;
        float panelY = (h - panelH) / 2f;

        sr.setProjectionMatrix(cam.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(panelColor);
        sr.rect(panelX, panelY, panelW, panelH);

        Color bosqueColor = isHovered(btnBosque) && isHost ? btnHoverColor : btnColor;
        Color teatroColor = isHovered(btnTeatro) && isHost ? btnHoverColor : btnColor;
        Color backColor = isHovered(btnBack) ? btnHoverColor : btnColor;

        sr.setColor(bosqueColor);
        sr.rect(btnBosque.x, btnBosque.y, btnBosque.width, btnBosque.height);

        sr.setColor(teatroColor);
        sr.rect(btnTeatro.x, btnTeatro.y, btnTeatro.width, btnTeatro.height);

        sr.setColor(backColor);
        sr.rect(btnBack.x, btnBack.y, btnBack.width, btnBack.height);

        sr.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        font.setColor(textColor);
        font.getData().setScale(1.6f);

        String title = isHost ? "Lobby (Host)" : "Lobby (Cliente)";
        layout.setText(font, title);
        float titleX = panelX + (panelW - layout.width) / 2f;
        float titleY = panelY + panelH - 40f;
        font.draw(batch, layout, titleX, titleY);

        font.getData().setScale(1.1f);
        String labelPlayers = "Jugadores conectados:";
        layout.setText(font, labelPlayers);
        float lpX = panelX + 40f;
        float lpY = panelY + panelH - 85f;
        font.draw(batch, layout, lpX, lpY);

        String line1 = "Tú: " + localName;
        layout.setText(font, line1);
        font.draw(batch, layout, lpX, lpY - 35f);

        String remoteName = null;
        if (hostSession != null) remoteName = hostSession.getRemoteName();
        if (clientSession != null) remoteName = clientSession.getRemoteName();

        String line2;
        if (remoteName == null || remoteName.trim().isEmpty()) {
            line2 = "Esperando jugador...";
        } else {
            line2 = "Conectado: " + remoteName;
        }
        layout.setText(font, line2);
        font.draw(batch, layout, lpX, lpY - 70f);

        font.getData().setScale(1.2f);
        drawCenteredText("Bosque", btnBosque);
        drawCenteredText("Teatro", btnTeatro);
        drawCenteredText("Volver", btnBack);

        batch.end();
    }

    private void drawCenteredText(String text, Rectangle rect) {
        layout.setText(font, text, textColor, 0, Align.left, false);
        float x = rect.x + (rect.width - layout.width) / 2f;
        float y = rect.y + rect.height / 2f + layout.height / 2f;
        font.draw(batch, layout, x, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        computeLayout();
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override
    public void dispose() {
        sr.dispose();
        batch.dispose();
        font.dispose();
    }
}
