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
    private BitmapFont settingsFont;
    private GlyphLayout layout;

    // Кнопки главного меню
    private Rectangle playButton;
    private Rectangle customsButton;
    private Rectangle settingsButton;
    private Rectangle exitButton;

    private boolean showSettings;
    private boolean showCustomization;
    private boolean soundEnabled;
    private boolean musicEnabled;
    private float volume;
    private int selectedCharacter;

    // Кнопки настроек
    private Rectangle soundButton;
    private Rectangle musicButton;
    private Rectangle volumeUpButton;
    private Rectangle volumeDownButton;
    private Rectangle backButton;

    // Кнопки выбора персонажа
    private Rectangle[] characterButtons;
    private String[] characterNames = {"GREEN KNIGHT", "BLUE MAGE", "RED WARRIOR", "GOLD ARCHER"};
    private Color[] characterColors = {Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW};

    private float time;
    private float pulseScale;

    public MainMenuScreen(Main game) {
        this.game = game;
        this.showSettings = false;
        this.showCustomization = false;
        this.soundEnabled = true;
        this.musicEnabled = true;
        this.volume = 0.7f;
        this.selectedCharacter = 0;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.5f);

        settingsFont = new BitmapFont();
        settingsFont.getData().setScale(1.3f);

        float buttonWidth = 250;
        float buttonHeight = 65;
        float centerX = Gdx.graphics.getWidth() / 2 - buttonWidth / 2;
        float startY = Gdx.graphics.getHeight() / 2 + 80;

        // 4 кнопки в главном меню: PLAY, CUSTOMS, SETTINGS, EXIT
        playButton = new Rectangle(centerX, startY, buttonWidth, buttonHeight);
        customsButton = new Rectangle(centerX, startY - 85, buttonWidth, buttonHeight);
        settingsButton = new Rectangle(centerX, startY - 170, buttonWidth, buttonHeight);
        exitButton = new Rectangle(centerX, startY - 255, buttonWidth, buttonHeight);

        // Кнопки настроек
        float settingsWidth = 350;
        float settingsHeight = 55;
        float settingsCenterX = Gdx.graphics.getWidth() / 2 - settingsWidth / 2;
        float settingsStartY = Gdx.graphics.getHeight() / 2 + 120;

        soundButton = new Rectangle(settingsCenterX, settingsStartY, settingsWidth, settingsHeight);
        musicButton = new Rectangle(settingsCenterX, settingsStartY - 75, settingsWidth, settingsHeight);
        volumeUpButton = new Rectangle(settingsCenterX + 230, settingsStartY - 150, 100, 55);
        volumeDownButton = new Rectangle(settingsCenterX + 20, settingsStartY - 150, 100, 55);
        backButton = new Rectangle(settingsCenterX, settingsStartY - 240, settingsWidth, settingsHeight);

        // Кнопки выбора персонажа
        characterButtons = new Rectangle[4];
        float charButtonWidth = 300;
        float charButtonHeight = 55;
        float charStartY = Gdx.graphics.getHeight() / 2 + 100;
        for (int i = 0; i < 4; i++) {
            characterButtons[i] = new Rectangle(Gdx.graphics.getWidth() / 2 - charButtonWidth / 2,
                charStartY - i * 75,
                charButtonWidth,
                charButtonHeight);
        }
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

        drawGradientBackground();
        drawDecorations();

        if (!showSettings && !showCustomization) {
            drawMainMenu();
            handleMainMenuInput();
        } else if (showSettings) {
            drawSettingsMenu();
            handleSettingsInput();
        } else if (showCustomization) {
            drawCustomizationMenu();
            handleCustomizationInput();
        }

        drawTitle();
    }

    private void drawMainMenu() {
        drawButton(playButton, "▶ PLAY GAME", new Color(0.2f, 0.6f, 0.2f, 1), new Color(0.3f, 0.8f, 0.3f, 1));
        drawButton(customsButton, "🎨 CUSTOMS", new Color(0.5f, 0.2f, 0.6f, 1), new Color(0.7f, 0.3f, 0.8f, 1));
        drawButton(settingsButton, "⚙ SETTINGS", new Color(0.3f, 0.3f, 0.6f, 1), new Color(0.4f, 0.4f, 0.8f, 1));
        drawButton(exitButton, "✖ EXIT", new Color(0.6f, 0.2f, 0.2f, 1), new Color(0.8f, 0.3f, 0.3f, 1));
    }

    private void drawSettingsMenu() {
        // Затемнение фона
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.85f));
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        // Панель настроек
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.1f, 0.1f, 0.2f, 0.98f));
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 220, Gdx.graphics.getHeight() / 2 - 220, 440, 440);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 218, Gdx.graphics.getHeight() / 2 - 218, 436, 436);
        shapeRenderer.end();

        // Заголовок настроек
        batch.begin();
        buttonFont.setColor(Color.BLACK);
        layout.setText(buttonFont, "SETTINGS");
        buttonFont.draw(batch, "SETTINGS", Gdx.graphics.getWidth() / 2 - layout.width / 2 + 3, Gdx.graphics.getHeight() / 2 + 180);
        buttonFont.setColor(Color.YELLOW);
        buttonFont.draw(batch, "SETTINGS", Gdx.graphics.getWidth() / 2 - layout.width / 2, Gdx.graphics.getHeight() / 2 + 178);
        batch.end();

        // Кнопка Sound
        String soundText = "SOUND: " + (soundEnabled ? "ON" : "OFF");
        drawSettingsButton(soundButton, soundText, soundEnabled ? new Color(0.2f, 0.6f, 0.2f, 1) : new Color(0.6f, 0.2f, 0.2f, 1));

        // Кнопка Music
        String musicText = "MUSIC: " + (musicEnabled ? "ON" : "OFF");
        drawSettingsButton(musicButton, musicText, musicEnabled ? new Color(0.2f, 0.6f, 0.2f, 1) : new Color(0.6f, 0.2f, 0.2f, 1));

        // Текст громкости
        batch.begin();
        settingsFont.setColor(Color.WHITE);
        String volumeText = "VOLUME: " + (int)(volume * 100) + "%";
        layout.setText(settingsFont, volumeText);
        settingsFont.draw(batch, volumeText, Gdx.graphics.getWidth() / 2 - layout.width / 2, Gdx.graphics.getHeight() / 2 - 40);
        batch.end();

        // Полоса громкости
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 80, 300, 20);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 80, 300 * volume, 20);
        shapeRenderer.end();

        // Кнопки + и -
        drawSettingsButton(volumeDownButton, "-", new Color(0.4f, 0.4f, 0.6f, 1));
        drawSettingsButton(volumeUpButton, "+", new Color(0.4f, 0.4f, 0.6f, 1));

        // Кнопка Back
        drawSettingsButton(backButton, "BACK", new Color(0.6f, 0.4f, 0.1f, 1));
    }

    private void drawCustomizationMenu() {
        // Затемнение фона
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.85f));
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        // Панель выбора персонажа
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.1f, 0.1f, 0.2f, 0.98f));
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 240, Gdx.graphics.getHeight() / 2 - 280, 480, 560);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 238, Gdx.graphics.getHeight() / 2 - 278, 476, 556);
        shapeRenderer.end();

        // Заголовок
        batch.begin();
        buttonFont.setColor(Color.BLACK);
        layout.setText(buttonFont, "SELECT YOUR CHARACTER");
        buttonFont.draw(batch, "SELECT YOUR CHARACTER", Gdx.graphics.getWidth() / 2 - layout.width / 2 + 3, Gdx.graphics.getHeight() / 2 + 230);
        buttonFont.setColor(Color.YELLOW);
        buttonFont.draw(batch, "SELECT YOUR CHARACTER", Gdx.graphics.getWidth() / 2 - layout.width / 2, Gdx.graphics.getHeight() / 2 + 228);
        batch.end();

        // Кнопки выбора персонажа
        for (int i = 0; i < 4; i++) {
            Color buttonColor = (selectedCharacter == i) ? characterColors[i] : new Color(0.3f, 0.3f, 0.5f, 1);
            String buttonText = (selectedCharacter == i) ? "★ " + characterNames[i] + " ★" : characterNames[i];
            drawCharacterButton(characterButtons[i], buttonText, buttonColor, characterColors[i]);
        }

        // Превью персонажа
        float previewSize = 100;
        float previewX = Gdx.graphics.getWidth() / 2 - previewSize / 2;
        float previewY = Gdx.graphics.getHeight() / 2 - 200;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(characterColors[selectedCharacter]);
        shapeRenderer.rect(previewX, previewY, previewSize, previewSize);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(previewX + 3, previewY + 3, previewSize - 6, previewSize - 6);
        // Рисуем глаза
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(previewX + previewSize * 0.35f, previewY + previewSize * 0.65f, previewSize * 0.1f);
        shapeRenderer.circle(previewX + previewSize * 0.65f, previewY + previewSize * 0.65f, previewSize * 0.1f);
        // Рисуем улыбку
        shapeRenderer.rectLine(previewX + previewSize * 0.35f, previewY + previewSize * 0.35f,
            previewX + previewSize * 0.65f, previewY + previewSize * 0.35f, 4);
        shapeRenderer.end();

        // Кнопка Back
        Rectangle customsBackButton = new Rectangle(Gdx.graphics.getWidth() / 2 - 120, Gdx.graphics.getHeight() / 2 - 290, 240, 50);
        drawSettingsButton(customsBackButton, "BACK", new Color(0.6f, 0.4f, 0.1f, 1));
    }

    private void drawTitle() {
        batch.begin();

        titleFont.setColor(new Color(0, 0, 0, 0.5f));
        titleFont.getData().setScale(3f * pulseScale);

        layout.setText(titleFont, "LAB RUNS");
        float titleX = Gdx.graphics.getWidth() / 2 - layout.width / 2;
        float titleY = Gdx.graphics.getHeight() - 100;
        titleFont.draw(batch, "LAB RUNS", titleX + 5, titleY - 5);

        titleFont.setColor(new Color(1, 0.8f, 0.2f, 1));
        titleFont.draw(batch, "LAB RUNS", titleX, titleY);

        titleFont.getData().setScale(1.8f);
        layout.setText(titleFont, "MONSTERS");
        float subX = Gdx.graphics.getWidth() / 2 - layout.width / 2;
        titleFont.setColor(new Color(0.8f, 0.5f, 0.2f, 1));
        titleFont.draw(batch, "MONSTERS", subX, titleY - 55);

        titleFont.getData().setScale(0.9f);
        titleFont.setColor(new Color(0.8f, 0.8f, 0.8f, 1));
        layout.setText(titleFont, "WASD or Arrows to move | Find GREEN EXIT | Collect STARS");
        float instX = Gdx.graphics.getWidth() / 2 - layout.width / 2;
        titleFont.draw(batch, "WASD or Arrows to move | Find GREEN EXIT | Collect STARS", instX, 45);

        batch.end();
    }

    private void handleMainMenuInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            float touchX = touchPos.x;
            float touchY = touchPos.y;

            if (playButton.contains(touchX, touchY)) {
                game.setScreen(new GameScreen(game, 1, selectedCharacter, characterColors[selectedCharacter]));
            } else if (customsButton.contains(touchX, touchY)) {
                showCustomization = true;
            } else if (settingsButton.contains(touchX, touchY)) {
                showSettings = true;
            } else if (exitButton.contains(touchX, touchY)) {
                Gdx.app.exit();
            }
        }
    }

    private void handleSettingsInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            float touchX = touchPos.x;
            float touchY = touchPos.y;

            if (soundButton.contains(touchX, touchY)) {
                soundEnabled = !soundEnabled;
            } else if (musicButton.contains(touchX, touchY)) {
                musicEnabled = !musicEnabled;
            } else if (volumeUpButton.contains(touchX, touchY) && volume < 1.0f) {
                volume += 0.1f;
                if (volume > 1.0f) volume = 1.0f;
            } else if (volumeDownButton.contains(touchX, touchY) && volume > 0.0f) {
                volume -= 0.1f;
                if (volume < 0.0f) volume = 0.0f;
            } else if (backButton.contains(touchX, touchY)) {
                showSettings = false;
            }
        }
    }

    private void handleCustomizationInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            float touchX = touchPos.x;
            float touchY = touchPos.y;

            // Кнопки выбора персонажа
            for (int i = 0; i < 4; i++) {
                if (characterButtons[i].contains(touchX, touchY)) {
                    selectedCharacter = i;
                }
            }

            // Кнопка Back
            Rectangle customsBackButton = new Rectangle(Gdx.graphics.getWidth() / 2 - 120, Gdx.graphics.getHeight() / 2 - 290, 240, 50);
            if (customsBackButton.contains(touchX, touchY)) {
                showCustomization = false;
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

        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < 150; i++) {
            float x = (float)(Math.sin(time + i) * 100 + i * 15) % Gdx.graphics.getWidth();
            float y = (float)(Math.cos(time * 0.3f + i) * 80 + i * 8) % Gdx.graphics.getHeight();
            float size = (float)(Math.sin(time * 2 + i) * 1 + 2);
            shapeRenderer.circle(x, y, size);
        }

        shapeRenderer.setColor(new Color(0.5f, 0.2f, 0.5f, 0.3f));
        for (int i = 0; i < 8; i++) {
            float offset = time * 30 + i * 150;
            shapeRenderer.rectLine(offset % Gdx.graphics.getWidth(), 0,
                (offset + 200) % Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 4);
        }

        shapeRenderer.end();
    }

    private void drawButton(Rectangle button, String text, Color darkColor, Color lightColor) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.6f));
        shapeRenderer.rect(button.x + 5, button.y - 5, button.width, button.height);

        for (int i = 0; i <= button.height; i += 5) {
            float t = (float)i / button.height;
            Color color = darkColor.cpy().lerp(lightColor, t);
            shapeRenderer.setColor(color);
            shapeRenderer.rect(button.x, button.y + i, button.width, 5);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.setColor(new Color(1, 1, 1, 0.8f));
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.rect(button.x + 2, button.y + 2, button.width - 4, button.height - 4);
        shapeRenderer.end();

        batch.begin();
        buttonFont.setColor(new Color(0, 0, 0, 0.8f));
        layout.setText(buttonFont, text);
        float textX = button.x + button.width / 2 - layout.width / 2;
        float textY = button.y + button.height / 2 + layout.height / 2;
        buttonFont.draw(batch, text, textX + 2, textY - 2);

        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(batch, text, textX, textY);
        batch.end();
    }

    private void drawSettingsButton(Rectangle button, String text, Color color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(button.x + 2, button.y + 2, button.width - 4, button.height - 4);
        shapeRenderer.end();

        batch.begin();
        settingsFont.setColor(new Color(0, 0, 0, 0.8f));
        layout.setText(settingsFont, text);
        float textX = button.x + button.width / 2 - layout.width / 2;
        float textY = button.y + button.height / 2 + layout.height / 2;
        settingsFont.draw(batch, text, textX + 2, textY - 2);

        settingsFont.setColor(Color.WHITE);
        settingsFont.draw(batch, text, textX, textY);
        batch.end();
    }

    private void drawCharacterButton(Rectangle button, String text, Color color, Color previewColor) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(button.x + 2, button.y + 2, button.width - 4, button.height - 4);
        shapeRenderer.end();

        // Маленький квадратик с цветом персонажа
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(previewColor);
        shapeRenderer.rect(button.x + 15, button.y + 12, 30, 30);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(button.x + 25, button.y + 27, 5);
        shapeRenderer.circle(button.x + 35, button.y + 27, 5);
        shapeRenderer.end();

        batch.begin();
        settingsFont.setColor(new Color(0, 0, 0, 0.8f));
        layout.setText(settingsFont, text);
        float textX = button.x + 60;
        float textY = button.y + button.height / 2 + layout.height / 2;
        settingsFont.draw(batch, text, textX + 2, textY - 2);

        settingsFont.setColor(Color.WHITE);
        settingsFont.draw(batch, text, textX, textY);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);

        float buttonWidth = 250;
        float buttonHeight = 65;
        float centerX = width / 2 - buttonWidth / 2;
        float startY = height / 2 + 80;

        playButton.set(centerX, startY, buttonWidth, buttonHeight);
        customsButton.set(centerX, startY - 85, buttonWidth, buttonHeight);
        settingsButton.set(centerX, startY - 170, buttonWidth, buttonHeight);
        exitButton.set(centerX, startY - 255, buttonWidth, buttonHeight);

        float settingsWidth = 350;
        float settingsHeight = 55;
        float settingsCenterX = width / 2 - settingsWidth / 2;
        float settingsStartY = height / 2 + 120;

        soundButton.set(settingsCenterX, settingsStartY, settingsWidth, settingsHeight);
        musicButton.set(settingsCenterX, settingsStartY - 75, settingsWidth, settingsHeight);
        volumeUpButton.set(settingsCenterX + 230, settingsStartY - 150, 100, 55);
        volumeDownButton.set(settingsCenterX + 20, settingsStartY - 150, 100, 55);
        backButton.set(settingsCenterX, settingsStartY - 240, settingsWidth, settingsHeight);

        float charButtonWidth = 300;
        float charButtonHeight = 55;
        float charStartY = height / 2 + 100;
        for (int i = 0; i < 4; i++) {
            characterButtons[i].set(width / 2 - charButtonWidth / 2, charStartY - i * 75, charButtonWidth, charButtonHeight);
        }
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
        settingsFont.dispose();
    }
}
