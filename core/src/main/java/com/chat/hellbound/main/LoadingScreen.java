package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.chat.hellbound.utilz.Constants;

public class LoadingScreen implements Screen {
    
    private final Main game;
    private final int targetLevel;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    
    private float loadingProgress;
    private float loadingTimer;
    private float dotTimer;
    private int dotCount;
    private boolean assetsLoaded;
    
    private static final float LOADING_DURATION = 2.0f; // 2 segundos mínimo
    
    public LoadingScreen(Main game, int level) {
        this.game = game;
        this.targetLevel = level;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        
        this.loadingProgress = 0f;
        this.loadingTimer = 0f;
        this.dotTimer = 0f;
        this.dotCount = 0;
        this.assetsLoaded = false;
        
        font.getData().setScale(2f);
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0f);
        viewport.apply();
    }
    
    @Override
    public void render(float delta) {
        loadingTimer += delta;
        dotTimer += delta;
        
        // Actualizar puntos de carga
        if (dotTimer >= 0.5f) {
            dotTimer = 0f;
            dotCount = (dotCount + 1) % 4;
        }
        
        // Simular carga progresiva
        if (loadingProgress < 1f) {
            loadingProgress += delta / LOADING_DURATION;
            if (loadingProgress > 1f) {
                loadingProgress = 1f;
                assetsLoaded = true;
            }
        }
        
        // Cambiar a GameScreen cuando termine
        if (assetsLoaded && loadingTimer >= LOADING_DURATION) {
            game.setScreen(new GameScreen(game, targetLevel));
            dispose();
            return;
        }
        
        // Renderizar pantalla de carga
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        
        // Dibujar barra de progreso
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        float barWidth = screenWidth * 0.6f;
        float barHeight = 40f;
        float barX = (screenWidth - barWidth) / 2f;
        float barY = screenHeight / 2f - barHeight / 2f;
        
        // Fondo de la barra
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        
        // Borde de la barra
        shapeRenderer.setColor(0.4f, 0.4f, 0.5f, 1f);
        float borderSize = 3f;
        shapeRenderer.rect(barX - borderSize, barY - borderSize, barWidth + borderSize * 2, borderSize);
        shapeRenderer.rect(barX - borderSize, barY + barHeight, barWidth + borderSize * 2, borderSize);
        shapeRenderer.rect(barX - borderSize, barY, borderSize, barHeight);
        shapeRenderer.rect(barX + barWidth, barY, borderSize, barHeight);
        
        // Progreso
        float progressWidth = barWidth * loadingProgress;
        Color progressColor = new Color(0.8f, 0.3f, 0.2f, 1f);
        shapeRenderer.setColor(progressColor);
        shapeRenderer.rect(barX, barY, progressWidth, barHeight);
        
        // Efecto de brillo
        shapeRenderer.setColor(1f, 0.5f, 0.3f, 0.3f);
        shapeRenderer.rect(barX, barY + barHeight * 0.7f, progressWidth, barHeight * 0.3f);
        
        shapeRenderer.end();
        
        // Dibujar texto
        batch.begin();
        
        String loadingText = "Cargando";
        for (int i = 0; i < dotCount; i++) {
            loadingText += ".";
        }
        
        layout.setText(font, loadingText);
        float textX = (screenWidth - layout.width) / 2f;
        float textY = barY + barHeight + 60f;
        font.draw(batch, layout, textX, textY);
        
        String levelText = "Nivel " + targetLevel;
        layout.setText(font, levelText);
        textX = (screenWidth - layout.width) / 2f;
        textY = barY - 40f;
        font.setColor(0.7f, 0.7f, 0.8f, 1f);
        font.draw(batch, layout, textX, textY);
        
        String percentText = (int)(loadingProgress * 100) + "%";
        layout.setText(font, percentText);
        textX = barX + barWidth + 20f;
        textY = barY + (barHeight + layout.height) / 2f;
        font.setColor(Color.WHITE);
        font.draw(batch, layout, textX, textY);
        
        batch.end();
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
}
