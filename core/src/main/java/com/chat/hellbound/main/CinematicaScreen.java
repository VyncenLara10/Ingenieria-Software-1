package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

import java.util.ArrayList;
import java.util.List;

public class CinematicScreen implements Screen {
    
    private final Main game;
    private final int targetLevel;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    
    private List<CinematicFrame> frames;
    private int currentFrameIndex;
    private float frameTimer;
    private float fadeAlpha;
    private boolean fadingIn;
    private boolean fadingOut;
    
    private static final float FADE_DURATION = 1.0f;
    
    public CinematicScreen(Main game, int level) {
        this.game = game;
        this.targetLevel = level;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        
        this.currentFrameIndex = 0;
        this.frameTimer = 0f;
        this.fadeAlpha = 0f;
        this.fadingIn = true;
        this.fadingOut = false;
        
        font.getData().setScale(1.8f);
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0f);
        viewport.apply();
        
        setupCinematic(level);
    }
    
    private void setupCinematic(int level) {
        frames = new ArrayList<>();
        
        if (level == 1) {
            // Cinemática del nivel 1
            frames.add(new CinematicFrame(
                "En las profundidades del bosque maldito...",
                new Color(0.1f, 0.15f, 0.1f, 1f),
                3.0f
            ));
            frames.add(new CinematicFrame(
                "Criaturas oscuras acechan entre las sombras.",
                new Color(0.08f, 0.08f, 0.12f, 1f),
                3.0f
            ));
            frames.add(new CinematicFrame(
                "Recolecta los artefactos sagrados\npara enfrentar al guardián de las sombras.",
                new Color(0.12f, 0.08f, 0.15f, 1f),
                4.0f
            ));
        } else if (level == 2) {
            // Cinemática del nivel 2
            frames.add(new CinematicFrame(
                "El teatro abandonado guarda secretos terribles...",
                new Color(0.12f, 0.08f, 0.08f, 1f),
                3.0f
            ));
            frames.add(new CinematicFrame(
                "Las llamas del infierno danzan en sus pasillos.",
                new Color(0.15f, 0.05f, 0.05f, 1f),
                3.0f
            ));
            frames.add(new CinematicFrame(
                "Encuentra los objetos malditos\npara desafiar al Guardián del Infierno.",
                new Color(0.18f, 0.06f, 0.06f, 1f),
                4.0f
            ));
        }
        
        // Frame final genérico
        frames.add(new CinematicFrame(
            "Presiona cualquier tecla para continuar...",
            new Color(0.05f, 0.05f, 0.08f, 1f),
            60.0f // Tiempo largo, se saltará con input
        ));
    }
    
    @Override
    public void render(float delta) {
        // Permitir saltar cinemática
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || 
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.justTouched()) {
            
            // Si está en el último frame, ir al juego
            if (currentFrameIndex >= frames.size() - 1) {
                goToGame();
                return;
            }
            
            // Saltar al siguiente frame
            nextFrame();
        }
        
        frameTimer += delta;
        
        CinematicFrame currentFrame = frames.get(currentFrameIndex);
        
        // Manejar fade in
        if (fadingIn) {
            fadeAlpha += delta / FADE_DURATION;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                fadingIn = false;
            }
        }
        
        // Manejar transición automática al siguiente frame
        if (frameTimer >= currentFrame.duration && !fadingOut) {
            if (currentFrameIndex < frames.size() - 1) {
                nextFrame();
            } else {
                // Último frame alcanzado
                fadingOut = true;
            }
        }
        
        // Fade out final
        if (fadingOut) {
            fadeAlpha -= delta / FADE_DURATION;
            if (fadeAlpha <= 0f) {
                goToGame();
                return;
            }
        }
        
        // Renderizar
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        
        // Fondo de color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Color bgColor = currentFrame.backgroundColor;
        shapeRenderer.setColor(bgColor.r, bgColor.g, bgColor.b, fadeAlpha);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // Texto
        batch.begin();
        font.setColor(1f, 1f, 1f, fadeAlpha);
        
        // Texto centrado con wrapping
        layout.setText(font, currentFrame.text, Color.WHITE, screenWidth * 0.8f, Align.center, true);
        float textX = (screenWidth - layout.width) / 2f;
        float textY = screenHeight / 2f + layout.height / 2f;
        
        font.draw(batch, layout, textX, textY);
        batch.end();
    }
    
    private void nextFrame() {
        currentFrameIndex++;
        frameTimer = 0f;
        fadeAlpha = 0f;
        fadingIn = true;
        fadingOut = false;
    }
    
    private void goToGame() {
        game.setScreen(new LoadingScreen(game, targetLevel));
        dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    
    @Override
    public void show() {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
    
    private static class CinematicFrame {
        String text;
        Color backgroundColor;
        float duration;
        
        CinematicFrame(String text, Color backgroundColor, float duration) {
            this.text = text;
            this.backgroundColor = backgroundColor;
            this.duration = duration;
        }
    }
}
