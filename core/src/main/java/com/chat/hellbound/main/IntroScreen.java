package com.chat.hellbound.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;


public class IntroScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private final BitmapFont font;

    private Texture logoTexture;
    private float timer = 0f;
    private float duration = 12f; // Duración en segundos
    private float fadeInDuration = 0.5f;
    private float fadeOutDuration = 0.5f;

    private boolean canSkip = false;

    public IntroScreen(Main game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();

        try {
            // Intentar cargar un logo/imagen de intro
            // El archivo debe estar en: assets/intro_logo.png
            logoTexture = new Texture(Gdx.files.internal("intro_logo.png"));
            System.out.println("Logo de intro cargado");
        } catch (Exception e) {
            System.out.println("No se encontró intro_logo.png, usando texto");
            logoTexture = null;
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        timer += delta;

        // Limpiar pantalla
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calcular alpha para fade in/out
        float alpha = 1f;
        if (timer < fadeInDuration) {
            // Fade in
            alpha = timer / fadeInDuration;
        } else if (timer > duration - fadeOutDuration) {
            // Fade out
            alpha = (duration - timer) / fadeOutDuration;
        } else {
            // Totalmente visible
            alpha = 1f;
            canSkip = true; // Permitir saltar después del fade in
        }

        alpha = Math.max(0f, Math.min(1f, alpha));

        batch.begin();

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        if (logoTexture != null) {
            // Dibujar logo centrado
            float logoWidth = logoTexture.getWidth();
            float logoHeight = logoTexture.getHeight();

            // Escalar si es necesario
            float scale = Math.min(screenWidth / logoWidth, screenHeight / logoHeight) * 0.8f;
            float drawWidth = logoWidth * scale;
            float drawHeight = logoHeight * scale;
            float drawX = (screenWidth - drawWidth) / 2f;
            float drawY = (screenHeight - drawHeight) / 2f;

            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(logoTexture, drawX, drawY, drawWidth, drawHeight);
            batch.setColor(1f, 1f, 1f, 1f);
        } else {
            // Dibujar texto si no hay logo
            font.getData().setScale(3f);
            font.setColor(1f, 1f, 1f, alpha);

            String title = "HELLBOUND";
            String subtitle = "Ingenieria de Software";

            font.draw(batch, title, 0, screenHeight / 2f + 50, screenWidth, Align.center, false);

            font.getData().setScale(1.5f);
            font.draw(batch, subtitle, 0, screenHeight / 2f - 50, screenWidth, Align.center, false);

            font.setColor(1f, 1f, 1f, 1f);
            font.getData().setScale(1f);
        }

        // Indicador de "presiona para continuar" (solo después del fade in)
        if (canSkip) {
            font.setColor(1f, 1f, 1f, alpha * 0.7f);
            String skipText = "Presiona cualquier tecla para continuar...";
            font.draw(batch, skipText, 0, 50, screenWidth, Align.center, false);
            font.setColor(1f, 1f, 1f, 1f);
        }

        batch.end();

        // Permitir saltar después del fade in
        if (canSkip) {
            if (Gdx.input.justTouched() ||
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ANY_KEY)) {
                goToMenu();
            }
        }

        // Ir al menú cuando termine el tiempo
        if (timer >= duration) {
            goToMenu();
        }
    }

    private void goToMenu() {
        game.setScreen(new MenuScreen(game));
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (logoTexture != null) {
            logoTexture.dispose();
        }
        batch.dispose();
        font.dispose();
    }
}
