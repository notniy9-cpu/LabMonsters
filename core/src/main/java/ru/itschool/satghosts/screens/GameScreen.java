package ru.itschool.satghosts.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
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
import ru.itschool.satghosts.models.Player;
import ru.itschool.satghosts.models.Monster;
import ru.itschool.satghosts.utils.Constants;

public class GameScreen implements Screen {
    private Main game;
    private OrthographicCamera gameCamera;
    private OrthographicCamera uiCamera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont bigFont;
    private GlyphLayout layout;
    private Player player;
    private Monster monster;
    private int currentLevel;
    private int[][] currentMaze;
    private float cellSize;
    private boolean gameOver;
    private boolean levelComplete;
    private boolean isPaused;
    private float gameOverTimer;
    private float screenWidth;
    private float screenHeight;

    // Цвет персонажа из меню кастомизации
    private Color playerColor;
    private int characterIndex;

    private Rectangle exitZone;
    private int exitX, exitY;
    private float pulseTime;
    private int lives;
    private boolean invincible;
    private float invincibleTimer;

    // Бонусы
    private Rectangle[] bonuses;
    private boolean[] bonusActive;
    private int bonusCount;
    private int collectedBonuses;
    private float bonusPulseTime;

    // Таймер
    private float gameTime;
    private String timeString;

    // Стартовая позиция игрока
    private int startX, startY;

    // Кнопки UI
    private Rectangle pauseButton;
    private Rectangle resumeButton;
    private Rectangle menuButton;
    private Rectangle pauseMenuButton;
    private boolean showPauseMenu;

    public GameScreen(Main game, int level, int characterIndex, Color playerColor) {
        this.game = game;
        this.currentLevel = level;
        this.characterIndex = characterIndex;
        this.playerColor = playerColor;
        this.gameOver = false;
        this.levelComplete = false;
        this.isPaused = false;
        this.showPauseMenu = false;
        this.lives = 3;
        this.invincible = false;
        this.invincibleTimer = 0;
        this.gameTime = 0;
        this.timeString = "00:00";
        this.collectedBonuses = 0;

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        float worldWidth = 10;
        float worldHeight = 10;

        cellSize = Math.min(screenWidth / worldWidth, screenHeight / worldHeight);

        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, worldWidth * cellSize, worldHeight * cellSize);
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, screenWidth, screenHeight);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        bigFont = new BitmapFont();
        layout = new GlyphLayout();
        font.getData().setScale(1.2f);
        bigFont.getData().setScale(2.5f);

        // Создаем кнопки
        pauseButton = new Rectangle(screenWidth - 70, 20, 50, 50);
        resumeButton = new Rectangle(screenWidth / 2 - 120, screenHeight / 2 + 80, 240, 60);
        menuButton = new Rectangle(screenWidth / 2 - 120, screenHeight / 2, 240, 60);
        pauseMenuButton = new Rectangle(screenWidth / 2 - 120, screenHeight / 2 - 80, 240, 60);

        loadLevel(level);
    }

    private void loadLevel(int level) {
        currentMaze = Constants.MAZES[level - 1];

        int playerX = -1;
        int playerY = -1;
        int mazeHeight = currentMaze.length;
        int mazeWidth = currentMaze[0].length;

        // Находим позицию игрока и выхода
        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                if (currentMaze[y][x] == 0 && playerX == -1) {
                    playerX = x;
                    playerY = y;
                    startX = x;
                    startY = y;
                }
                if (currentMaze[y][x] == 2) {
                    exitX = x;
                    exitY = y;
                }
            }
        }

        // Создаем игрока с выбранным цветом
        if (playerX != -1 && playerY != -1) {
            player = new Player(playerX * cellSize + (cellSize * 0.1f),
                playerY * cellSize + (cellSize * 0.1f),
                cellSize * 0.8f,
                cellSize * 0.8f);
        }

        // Находим позицию для монстра
        int monsterX = -1;
        int monsterY = -1;

        for (int my = mazeHeight - 1; my >= 0; my--) {
            for (int mx = mazeWidth - 1; mx >= 0; mx--) {
                if (currentMaze[my][mx] == 0 && (mx != playerX || my != playerY) && (mx != exitX || my != exitY)) {
                    monsterX = mx;
                    monsterY = my;
                    break;
                }
            }
            if (monsterX != -1) break;
        }

        if (monsterX == -1) {
            monsterX = 7;
            monsterY = 7;
        }

        float monsterSpeed = Constants.MONSTER_SPEED;
        if (currentLevel >= 10) monsterSpeed = 3.2f;
        else if (currentLevel >= 7) monsterSpeed = 2.9f;
        else if (currentLevel >= 4) monsterSpeed = 2.7f;

        monster = new Monster(monsterX * cellSize + (cellSize * 0.1f),
            monsterY * cellSize + (cellSize * 0.1f),
            cellSize * 0.8f,
            cellSize * 0.8f,
            monsterSpeed);

        exitZone = new Rectangle(exitX * cellSize, exitY * cellSize, cellSize, cellSize);

        // Создаем бонусы
        createBonuses();
    }

    private void createBonuses() {
        int mazeHeight = currentMaze.length;
        int mazeWidth = currentMaze[0].length;

        bonusCount = Math.min(3 + currentLevel / 3, 8);
        bonuses = new Rectangle[bonusCount];
        bonusActive = new boolean[bonusCount];

        int placedBonuses = 0;

        for (int i = 0; i < bonusCount; i++) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 100) {
                int randX = (int)(Math.random() * mazeWidth);
                int randY = (int)(Math.random() * mazeHeight);

                if (currentMaze[randY][randX] == 0 &&
                    (randX != startX || randY != startY) &&
                    (randX != exitX || randY != exitY)) {

                    int monsterCellX = (int)(monster.x / cellSize);
                    int monsterCellY = (int)(monster.y / cellSize);
                    if (randX != monsterCellX || randY != monsterCellY) {
                        bonuses[i] = new Rectangle(randX * cellSize + cellSize * 0.25f,
                            randY * cellSize + cellSize * 0.25f,
                            cellSize * 0.5f, cellSize * 0.5f);
                        bonusActive[i] = true;
                        placed = true;
                        placedBonuses++;
                    }
                }
                attempts++;
            }
        }

        bonusCount = placedBonuses;
    }

    @Override
    public void render(float delta) {
        pulseTime += delta;
        bonusPulseTime += delta;

        if (invincible) {
            invincibleTimer -= delta;
            if (invincibleTimer <= 0) {
                invincible = false;
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();

        if (!gameOver && !levelComplete && !isPaused) {
            updateGame(delta);
            gameTime += delta;
            updateTimeString();
        } else if (gameOverTimer > 0) {
            gameOverTimer -= delta;
            if (gameOverTimer <= 0) {
                if (gameOver) {
                    game.setScreen(new MainMenuScreen(game));
                } else if (levelComplete) {
                    if (currentLevel < 15) {
                        game.setScreen(new GameScreen(game, currentLevel + 1, characterIndex, playerColor));
                    } else {
                        game.setScreen(new MainMenuScreen(game));
                    }
                }
                dispose();
                return;
            }
        }

        drawGame();
        drawUI();

        if (showPauseMenu || isPaused) {
            drawPauseMenu();
        }
    }

    private void updateTimeString() {
        int minutes = (int)(gameTime / 60);
        int seconds = (int)(gameTime % 60);
        timeString = String.format("%02d:%02d", minutes, seconds);
    }

    private void respawnPlayer() {
        player.x = startX * cellSize + (cellSize * 0.1f);
        player.y = startY * cellSize + (cellSize * 0.1f);
        player.update(0);
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = screenHeight - Gdx.input.getY();

            if (pauseButton.contains(touchX, touchY) && !gameOver && !levelComplete) {
                isPaused = !isPaused;
                showPauseMenu = isPaused;
                return;
            }

            if (showPauseMenu) {
                if (resumeButton.contains(touchX, touchY)) {
                    isPaused = false;
                    showPauseMenu = false;
                    return;
                }
                if (menuButton.contains(touchX, touchY)) {
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                    return;
                }
                if (pauseMenuButton.contains(touchX, touchY)) {
                    Gdx.app.exit();
                    return;
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!gameOver && !levelComplete) {
                isPaused = !isPaused;
                showPauseMenu = isPaused;
            }
        }
    }

    private void updateGame(float delta) {
        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX = -Constants.PLAYER_SPEED * delta * cellSize;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX = Constants.PLAYER_SPEED * delta * cellSize;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveY = Constants.PLAYER_SPEED * delta * cellSize;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveY = -Constants.PLAYER_SPEED * delta * cellSize;
        }

        if (Gdx.input.isTouched() && !showPauseMenu) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            gameCamera.unproject(touchPos);

            float centerX = player.x + player.width / 2;
            float centerY = player.y + player.height / 2;

            if (Math.abs(touchPos.x - centerX) > Math.abs(touchPos.y - centerY)) {
                if (touchPos.x > centerX) {
                    moveX = Constants.PLAYER_SPEED * delta * cellSize;
                } else {
                    moveX = -Constants.PLAYER_SPEED * delta * cellSize;
                }
            } else {
                if (touchPos.y > centerY) {
                    moveY = Constants.PLAYER_SPEED * delta * cellSize;
                } else {
                    moveY = -Constants.PLAYER_SPEED * delta * cellSize;
                }
            }
        }

        if (moveX != 0) {
            float newX = player.x + moveX;
            if (canMoveTo(newX, player.y, player.width, player.height)) {
                player.move(moveX, 0);
            }
        }
        if (moveY != 0) {
            float newY = player.y + moveY;
            if (canMoveTo(player.x, newY, player.width, player.height)) {
                player.move(0, moveY);
            }
        }

        player.update(delta);
        checkTraps();
        checkBonuses();

        if (monster != null) {
            monster.update(delta, player, currentMaze, cellSize);
        }

        if (!invincible && monster != null && player.getBounds().overlaps(monster.getBounds())) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
                gameOverTimer = 2f;
            } else {
                invincible = true;
                invincibleTimer = 1.5f;
                respawnPlayer();
            }
        }

        if (player.getBounds().overlaps(exitZone)) {
            levelComplete = true;
            gameOverTimer = 2f;
        }
    }

    private void checkBonuses() {
        if (bonuses == null) return;

        for (int i = 0; i < bonusCount; i++) {
            if (bonusActive[i] && player.getBounds().overlaps(bonuses[i])) {
                bonusActive[i] = false;
                collectedBonuses++;
                if (collectedBonuses % 3 == 0 && lives < 3) {
                    lives++;
                }
            }
        }
    }

    private void checkTraps() {
        int playerCellX = (int)(player.x / cellSize);
        int playerCellY = (int)(player.y / cellSize);

        if (playerCellX >= 0 && playerCellX < currentMaze[0].length &&
            playerCellY >= 0 && playerCellY < currentMaze.length) {
            if (currentMaze[playerCellY][playerCellX] == 3 && !invincible) {
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    gameOverTimer = 2f;
                } else {
                    invincible = true;
                    invincibleTimer = 1.5f;
                    respawnPlayer();
                }
            }
        }
    }

    private boolean canMoveTo(float x, float y, float width, float height) {
        int leftCell = (int)(x / cellSize);
        int rightCell = (int)((x + width) / cellSize);
        int bottomCell = (int)(y / cellSize);
        int topCell = (int)((y + height) / cellSize);
        int mazeHeight = currentMaze.length;
        int mazeWidth = currentMaze[0].length;

        if (leftCell < 0 || rightCell >= mazeWidth ||
            bottomCell < 0 || topCell >= mazeHeight) {
            return false;
        }

        if (currentMaze[topCell][leftCell] == 1) return false;
        if (currentMaze[topCell][rightCell] == 1) return false;
        if (currentMaze[bottomCell][leftCell] == 1) return false;
        if (currentMaze[bottomCell][rightCell] == 1) return false;

        return true;
    }

    private void drawGame() {
        gameCamera.update();
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int mazeHeight = currentMaze.length;
        int mazeWidth = currentMaze[0].length;

        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                if (currentMaze[y][x] == 1) {
                    shapeRenderer.setColor(Color.BROWN);
                    shapeRenderer.rect(x * cellSize, y * cellSize, cellSize, cellSize);
                } else if (currentMaze[y][x] == 2) {
                    float pulse = (float)(Math.sin(pulseTime * 5) * 0.3 + 0.7);
                    shapeRenderer.setColor(new Color(0, pulse, 0, 1));
                    shapeRenderer.rect(x * cellSize, y * cellSize, cellSize, cellSize);
                } else if (currentMaze[y][x] == 3) {
                    float pulse = (float)(Math.sin(pulseTime * 8) * 0.3 + 0.5);
                    shapeRenderer.setColor(new Color(pulse + 0.2f, 0, 0, 1));
                    shapeRenderer.rect(x * cellSize, y * cellSize, cellSize, cellSize);
                    shapeRenderer.setColor(Color.RED);
                    for (int i = 0; i < 4; i++) {
                        shapeRenderer.triangle(
                            x * cellSize + cellSize/2,
                            y * cellSize + cellSize * (0.2f + i * 0.2f),
                            x * cellSize + cellSize * 0.3f,
                            y * cellSize + cellSize * (0.1f + i * 0.2f),
                            x * cellSize + cellSize * 0.7f,
                            y * cellSize + cellSize * (0.1f + i * 0.2f)
                        );
                    }
                } else {
                    shapeRenderer.setColor(Color.LIGHT_GRAY);
                    shapeRenderer.rect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }

        shapeRenderer.end();

        // Рисуем бонусы (звездочки)
        if (bonuses != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < bonusCount; i++) {
                if (bonusActive[i]) {
                    float pulse = (float)(Math.sin(bonusPulseTime * 5 + i) * 0.3 + 0.7);
                    shapeRenderer.setColor(new Color(1, pulse, 0, 1));
                    shapeRenderer.circle(bonuses[i].x + bonuses[i].width / 2,
                        bonuses[i].y + bonuses[i].height / 2,
                        bonuses[i].width / 2);

                    shapeRenderer.setColor(new Color(1, pulse, 0, 0.8f));
                    for (int a = 0; a < 4; a++) {
                        float angle = (float)(bonusPulseTime * 3 + a * Math.PI / 2);
                        float dx = (float)Math.cos(angle) * bonuses[i].width * 0.7f;
                        float dy = (float)Math.sin(angle) * bonuses[i].height * 0.7f;
                        shapeRenderer.rectLine(bonuses[i].x + bonuses[i].width / 2,
                            bonuses[i].y + bonuses[i].height / 2,
                            bonuses[i].x + bonuses[i].width / 2 + dx,
                            bonuses[i].y + bonuses[i].height / 2 + dy,
                            3);
                    }
                }
            }
            shapeRenderer.end();
        }

        // Рисуем игрока с выбранным цветом
        if (player != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (invincible && (int)(pulseTime * 10) % 2 == 0) {
                shapeRenderer.setColor(new Color(1, 1, 1, 0.5f));
            } else {
                shapeRenderer.setColor(playerColor);
            }
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
            shapeRenderer.end();
        }

        if (monster != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            monster.render(shapeRenderer);
            shapeRenderer.end();
        }
    }

    private void drawHeart(float x, float y, float size, boolean filled) {
        shapeRenderer.setColor(filled ? Color.RED : Color.DARK_GRAY);

        shapeRenderer.arc(x - size * 0.35f, y + size * 0.2f, size * 0.4f, 45, 180);
        shapeRenderer.arc(x + size * 0.35f, y + size * 0.2f, size * 0.4f, -45, 180);

        float[] triangleX = {x - size * 0.5f, x + size * 0.5f, x};
        float[] triangleY = {y + size * 0.2f, y + size * 0.2f, y - size * 0.4f};
        shapeRenderer.triangle(triangleX[0], triangleY[0], triangleX[1], triangleY[1], triangleX[2], triangleY[2]);
    }

    private void drawUI() {
        uiCamera.update();

        Vector3 exitScreenPos = gameCamera.project(new Vector3(exitZone.x, exitZone.y, 0));
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(Color.BLACK);
        font.draw(batch, "EXIT", exitScreenPos.x + 1, exitScreenPos.y + cellSize * 0.6f + 1);
        font.setColor(Color.WHITE);
        font.draw(batch, "EXIT", exitScreenPos.x, exitScreenPos.y + cellSize * 0.6f);
        batch.end();

        batch.begin();
        font.setColor(Color.BLACK);
        font.draw(batch, "Level: " + currentLevel + " / 15", 21, screenHeight - 29);
        font.setColor(Color.WHITE);
        font.draw(batch, "Level: " + currentLevel + " / 15", 20, screenHeight - 30);

        font.setColor(Color.BLACK);
        font.draw(batch, "Time: " + timeString, screenWidth - 121, screenHeight - 29);
        font.setColor(Color.CYAN);
        font.draw(batch, "Time: " + timeString, screenWidth - 120, screenHeight - 30);

        font.setColor(Color.BLACK);
        font.draw(batch, "Stars: " + collectedBonuses + " / " + bonusCount, screenWidth / 2 - 80, screenHeight - 29);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Stars: " + collectedBonuses + " / " + bonusCount, screenWidth / 2 - 81, screenHeight - 30);
        batch.end();

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));
        shapeRenderer.rect(pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(pauseButton.x + 15, pauseButton.y + 15, 6, 20);
        shapeRenderer.rect(pauseButton.x + 29, pauseButton.y + 15, 6, 20);
        shapeRenderer.end();

        float heartSize = 30;
        float startX = screenWidth / 2 - (3 * heartSize) / 2;
        float heartY = screenHeight - 70;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 3; i++) {
            boolean hasLife = i < lives;
            drawHeart(startX + i * heartSize, heartY, heartSize, hasLife);
        }
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.BLACK);
        font.draw(batch, "WASD or Arrows to move", screenWidth / 2 - 110, 35);
        font.setColor(Color.WHITE);
        font.draw(batch, "WASD or Arrows to move", screenWidth / 2 - 111, 34);
        batch.end();

        if (gameOver) {
            batch.begin();
            bigFont.setColor(Color.BLACK);
            layout.setText(bigFont, "GAME OVER!");
            bigFont.draw(batch, "GAME OVER!", screenWidth / 2 - layout.width / 2 + 3, screenHeight / 2 - 3);
            bigFont.setColor(Color.RED);
            bigFont.draw(batch, "GAME OVER!", screenWidth / 2 - layout.width / 2, screenHeight / 2);
            batch.end();
        } else if (levelComplete) {
            batch.begin();
            bigFont.setColor(Color.BLACK);
            if (currentLevel < 15) {
                String text = "LEVEL " + currentLevel + " COMPLETE!";
                layout.setText(bigFont, text);
                bigFont.draw(batch, text, screenWidth / 2 - layout.width / 2 + 3, screenHeight / 2 + 50 - 3);
                bigFont.setColor(Color.GREEN);
                bigFont.draw(batch, text, screenWidth / 2 - layout.width / 2, screenHeight / 2 + 50);

                font.setColor(Color.BLACK);
                String nextText = "Next level in 2 seconds...";
                layout.setText(font, nextText);
                font.draw(batch, nextText, screenWidth / 2 - layout.width / 2 + 2, screenHeight / 2 - 30 - 2);
                font.setColor(Color.WHITE);
                font.draw(batch, nextText, screenWidth / 2 - layout.width / 2, screenHeight / 2 - 30);
            } else {
                String text = "VICTORY!";
                layout.setText(bigFont, text);
                bigFont.draw(batch, text, screenWidth / 2 - layout.width / 2 + 3, screenHeight / 2 + 50 - 3);
                bigFont.setColor(Color.YELLOW);
                bigFont.draw(batch, text, screenWidth / 2 - layout.width / 2, screenHeight / 2 + 50);

                font.setColor(Color.BLACK);
                String nextText = "You completed all 15 levels!";
                layout.setText(font, nextText);
                font.draw(batch, nextText, screenWidth / 2 - layout.width / 2 + 2, screenHeight / 2 - 30 - 2);
                font.setColor(Color.WHITE);
                font.draw(batch, nextText, screenWidth / 2 - layout.width / 2, screenHeight / 2 - 30);
            }
            batch.end();
        }
    }

    private void drawPauseMenu() {
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.85f));
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.15f, 0.15f, 0.25f, 1));
        shapeRenderer.rect(screenWidth / 2 - 180, screenHeight / 2 - 220, 360, 440);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(screenWidth / 2 - 178, screenHeight / 2 - 218, 356, 436);
        shapeRenderer.end();

        batch.begin();

        bigFont.setColor(Color.BLACK);
        layout.setText(bigFont, "PAUSED");
        bigFont.draw(batch, "PAUSED", screenWidth / 2 - layout.width / 2 + 3, screenHeight / 2 + 170 - 3);
        bigFont.setColor(Color.YELLOW);
        bigFont.draw(batch, "PAUSED", screenWidth / 2 - layout.width / 2, screenHeight / 2 + 170);

        font.setColor(Color.BLACK);
        layout.setText(font, "Time played: " + timeString);
        font.draw(batch, "Time played: " + timeString, screenWidth / 2 - layout.width / 2 + 2, screenHeight / 2 + 110 - 2);
        font.setColor(Color.CYAN);
        font.draw(batch, "Time played: " + timeString, screenWidth / 2 - layout.width / 2, screenHeight / 2 + 110);

        font.setColor(Color.BLACK);
        layout.setText(font, "Current Level: " + currentLevel + " / 15");
        font.draw(batch, "Current Level: " + currentLevel + " / 15", screenWidth / 2 - layout.width / 2 + 2, screenHeight / 2 + 80 - 2);
        font.setColor(Color.WHITE);
        font.draw(batch, "Current Level: " + currentLevel + " / 15", screenWidth / 2 - layout.width / 2, screenHeight / 2 + 80);

        font.setColor(Color.BLACK);
        layout.setText(font, "Lives: " + lives + " / 3");
        font.draw(batch, "Lives: " + lives + " / 3", screenWidth / 2 - layout.width / 2 + 2, screenHeight / 2 + 50 - 2);
        font.setColor(Color.RED);
        font.draw(batch, "Lives: " + lives + " / 3", screenWidth / 2 - layout.width / 2, screenHeight / 2 + 50);

        font.setColor(Color.BLACK);
        layout.setText(font, "Stars collected: " + collectedBonuses + " / " + bonusCount);
        font.draw(batch, "Stars collected: " + collectedBonuses + " / " + bonusCount, screenWidth / 2 - layout.width / 2 + 2, screenHeight / 2 + 20 - 2);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Stars collected: " + collectedBonuses + " / " + bonusCount, screenWidth / 2 - layout.width / 2, screenHeight / 2 + 20);

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.2f, 0.7f, 0.2f, 1));
        shapeRenderer.rect(resumeButton.x, resumeButton.y, resumeButton.width, resumeButton.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(resumeButton.x + 2, resumeButton.y + 2, resumeButton.width - 4, resumeButton.height - 4);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.BLACK);
        layout.setText(font, "RESUME");
        font.draw(batch, "RESUME", resumeButton.x + resumeButton.width / 2 - layout.width / 2 + 2, resumeButton.y + 38 - 2);
        font.setColor(Color.GREEN);
        font.draw(batch, "RESUME", resumeButton.x + resumeButton.width / 2 - layout.width / 2, resumeButton.y + 38);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.6f, 0.4f, 0.1f, 1));
        shapeRenderer.rect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(menuButton.x + 2, menuButton.y + 2, menuButton.width - 4, menuButton.height - 4);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.BLACK);
        layout.setText(font, "MAIN MENU");
        font.draw(batch, "MAIN MENU", menuButton.x + menuButton.width / 2 - layout.width / 2 + 2, menuButton.y + 38 - 2);
        font.setColor(Color.YELLOW);
        font.draw(batch, "MAIN MENU", menuButton.x + menuButton.width / 2 - layout.width / 2, menuButton.y + 38);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.7f, 0.2f, 0.2f, 1));
        shapeRenderer.rect(pauseMenuButton.x, pauseMenuButton.y, pauseMenuButton.width, pauseMenuButton.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(pauseMenuButton.x + 2, pauseMenuButton.y + 2, pauseMenuButton.width - 4, pauseMenuButton.height - 4);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.BLACK);
        layout.setText(font, "EXIT GAME");
        font.draw(batch, "EXIT GAME", pauseMenuButton.x + pauseMenuButton.width / 2 - layout.width / 2 + 2, pauseMenuButton.y + 38 - 2);
        font.setColor(Color.RED);
        font.draw(batch, "EXIT GAME", pauseMenuButton.x + pauseMenuButton.width / 2 - layout.width / 2, pauseMenuButton.y + 38);

        font.setColor(Color.GRAY);
        layout.setText(font, "Press ESC to resume");
        font.draw(batch, "Press ESC to resume", screenWidth / 2 - layout.width / 2, screenHeight / 2 - 130);
        batch.end();
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        float worldWidth = 10;
        float worldHeight = 10;
        cellSize = Math.min(screenWidth / worldWidth, screenHeight / worldHeight);

        gameCamera.setToOrtho(false, worldWidth * cellSize, worldHeight * cellSize);
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        gameCamera.update();

        uiCamera.setToOrtho(false, screenWidth, screenHeight);
        uiCamera.update();

        pauseButton.set(screenWidth - 70, 20, 50, 50);
        resumeButton.set(screenWidth / 2 - 120, screenHeight / 2 + 80, 240, 60);
        menuButton.set(screenWidth / 2 - 120, screenHeight / 2, 240, 60);
        pauseMenuButton.set(screenWidth / 2 - 120, screenHeight / 2 - 80, 240, 60);

        if (exitZone != null) {
            exitZone.set(exitX * cellSize, exitY * cellSize, cellSize, cellSize);
        }
        if (player != null) {
            int playerCellX = (int)(player.x / cellSize);
            int playerCellY = (int)(player.y / cellSize);
            player = new Player(playerCellX * cellSize + (cellSize * 0.1f),
                playerCellY * cellSize + (cellSize * 0.1f),
                cellSize * 0.8f,
                cellSize * 0.8f);
        }
        if (bonuses != null) {
            for (int i = 0; i < bonusCount; i++) {
                if (bonuses[i] != null && bonusActive[i]) {
                    int bonusCellX = (int)(bonuses[i].x / cellSize);
                    int bonusCellY = (int)(bonuses[i].y / cellSize);
                    bonuses[i].set(bonusCellX * cellSize + cellSize * 0.25f,
                        bonusCellY * cellSize + cellSize * 0.25f,
                        cellSize * 0.5f, cellSize * 0.5f);
                }
            }
        }
        if (monster != null) {
            int monsterCellX = (int)(monster.x / cellSize);
            int monsterCellY = (int)(monster.y / cellSize);
            float currentSpeed = monster.getSpeed();
            monster = new Monster(monsterCellX * cellSize + (cellSize * 0.1f),
                monsterCellY * cellSize + (cellSize * 0.1f),
                cellSize * 0.8f,
                cellSize * 0.8f,
                currentSpeed);
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
        font.dispose();
        bigFont.dispose();
    }
}
