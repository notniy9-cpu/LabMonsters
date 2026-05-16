package ru.itschool.satghosts.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import ru.itschool.satghosts.Main;

public class MainMenuScreen implements Screen {
    private Main game;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private GlyphLayout layout;

    private Rectangle playButton;
    private Rectangle exitButton;

    private float time;
    private float pulseScale;

    public MainMenuScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        // Создаем шрифты с большим размером
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(2f);

        float buttonWidth = 280;
        float buttonHeight = 90;
        float centerX = Gdx.graphics.getWidth() / 2 - buttonWidth / 2;

        playButton = new Rectangle(centerX, Gdx.graphics.getHeight() / 2 + 30, buttonWidth, buttonHeight);
        exitButton = new Rectangle(centerX, Gdx.graphics.getHeight() / 2 - 100, buttonWidth, buttonHeight);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        time += delta;
        pulseScale = 1 + (float)Math.sin(time * 3) * 0.05f;

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Рисуем градиентный фон
        drawGradientBackground();

        // Рисуем декоративные элементы
        drawDecorations();

        // Рисуем кнопки
        drawButton(playButton, "PLAY GAME", new Color(0.2f, 0.6f, 0.2f, 1), new Color(0.3f, 0.8f, 0.3f, 1));
        drawButton(exitButton, "EXIT", new Color(0.6f, 0.2f, 0.2f, 1), new Color(0.8f, 0.3f, 0.3f, 1));

        // Рисуем заголовок с эффектом тени
        batch.begin();

        // Тень заголовка
        titleFont.setColor(new Color(0, 0, 0, 0.5f));
        titleFont.getData().setScale(3f * pulseScale);

        layout.setText(titleFont, "LAB RUNS");
        float titleX = Gdx.graphics.getWidth() / 2 - layout.width / 2;
        float titleY = Gdx.graphics.getHeight() - 150;
        titleFont.draw(batch, "LAB RUNS", titleX + 5, titleY - 5);

        // Основной заголовок
        titleFont.setColor(new Color(1, 0.8f, 0.2f, 1));
        titleFont.draw(batch, "LAB RUNS", titleX, titleY);

        // Подзаголовок
        titleFont.getData().setScale(1.8f);
        layout.setText(titleFont, "MONSTERS");
        float subX = Gdx.graphics.getWidth() / 2 - layout.width / 2;
        titleFont.setColor(new Color(0.8f, 0.5f, 0.2f, 1));
        titleFont.draw(batch, "MONSTERS", subX, titleY - 70);

        // Инструкции
        titleFont.getData().setScale(1.2f);
        titleFont.setColor(new Color(0.8f, 0.8f, 0.8f, 1));
        layout.setText(titleFont, "Use WASD or Arrow Keys to move");
        float instX = Gdx.graphics.getWidth() / 2 - layout.width / 2;
        titleFont.draw(batch, "Use WASD or Arrow Keys to move", instX, 120);

        layout.setText(titleFont, "Find the GREEN EXIT and avoid RED traps!");
        instX = Gdx.graphics.getWidth() / 2 - layout.width / 2;
        titleFont.draw(batch, "Find the GREEN EXIT and avoid RED traps!", instX, 80);

        batch.end();

        // Обработка касаний
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            float touchX = touchPos.x;
            float touchY = touchPos.y;

            if (playButton.contains(touchX, touchY)) {
                game.setScreen(new GameScreen(game, 1));
            } else if (exitButton.contains(touchX, touchY)) {
                Gdx.app.exit();
            }
        }
    }

    private void drawGradientBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i <= Gdx.graphics.getHeight(); i += 5) {
            float t = (float)i / Gdx.graphics.getHeight();
            Color color = new Color(0.05f, 0.05f, 0.1f + t * 0.2f, 1);
            shapeRenderer.setColor(color);
            shapeRenderer.rect(0, i, Gdx.graphics.getWidth(), 5);
        }
        shapeRenderer.end();
    }

    private void drawDecorations() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Рисуем звезды
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < 150; i++) {
            float x = (float)(Math.sin(time + i) * 100 + i * 15) % Gdx.graphics.getWidth();
            float y = (float)(Math.cos(time * 0.3f + i) * 80 + i * 8) % Gdx.graphics.getHeight();
            float size = (float)(Math.sin(time * 2 + i) * 1 + 2);
            shapeRenderer.circle(x, y, size);
        }

        // Рисуем декоративные линии
        shapeRenderer.setColor(new Color(0.5f, 0.2f, 0.5f, 0.3f));
        for (int i = 0; i < 8; i++) {
            float offset = time * 30 + i * 150;
            shapeRenderer.rectLine(offset % Gdx.graphics.getWidth(), 0,
                (offset + 200) % Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 4);
        }

        shapeRenderer.end();
    }

    private void drawButton(Rectangle button, String text, Color darkColor, Color lightColor) {
        // Тень кнопки
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.6f));
        shapeRenderer.rect(button.x + 8, button.y - 8, button.width, button.height);

        // Градиент кнопки
        for (int i = 0; i <= button.height; i += 5) {
            float t = (float)i / button.height;
            Color color = darkColor.cpy().lerp(lightColor, t);
            shapeRenderer.setColor(color);
            shapeRenderer.rect(button.x, button.y + i, button.width, 5);
        }
        shapeRenderer.end();

        // Рамка кнопки
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.setColor(new Color(1, 1, 1, 0.8f));
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.rect(button.x + 2, button.y + 2, button.width - 4, button.height - 4);
        shapeRenderer.end();

        // Текст кнопки с тенью
        batch.begin();
        // Тень текста
        buttonFont.setColor(new Color(0, 0, 0, 0.8f));
        layout.setText(buttonFont, text);
        float textX = button.x + button.width / 2 - layout.width / 2;
        float textY = button.y + button.height / 2 + layout.height / 2;
        buttonFont.draw(batch, text, textX + 3, textY - 3);

        // Основной текст
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(batch, text, textX, textY);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        float buttonWidth = 280;
        float buttonHeight = 90;
        float centerX = width / 2 - buttonWidth / 2;
        playButton.set(centerX, height / 2 + 30, buttonWidth, buttonHeight);
        exitButton.set(centerX, height / 2 - 100, buttonWidth, buttonHeight);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        buttonFont.dispose();
    }
}
