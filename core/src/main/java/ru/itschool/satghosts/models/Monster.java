package ru.itschool.satghosts.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Monster {
    public float x, y;
    public float width, height;
    public Rectangle bounds;
    private Vector2 target;
    private float speed;

    public Monster(float x, float y, float width, float height, float speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.bounds = new Rectangle(x, y, width, height);
        this.target = new Vector2();
    }

    public float getSpeed() {
        return speed;
    }

    public void update(float delta, Player player, int[][] maze, float cellSize) {
        target.set(player.x, player.y);
        Vector2 direction = new Vector2(target.x - x, target.y - y);
        float length = direction.len();

        if (length > 0) {
            direction.scl(speed * delta * cellSize / length);

            float newX = x + direction.x;
            if (canMoveTo(newX, y, maze, cellSize)) {
                x = newX;
            }

            float newY = y + direction.y;
            if (canMoveTo(x, newY, maze, cellSize)) {
                y = newY;
            }
        }

        bounds.set(x, y, width, height);
    }

    private boolean canMoveTo(float newX, float newY, int[][] maze, float cellSize) {
        int leftCell = (int)(newX / cellSize);
        int rightCell = (int)((newX + width) / cellSize);
        int bottomCell = (int)(newY / cellSize);
        int topCell = (int)((newY + height) / cellSize);
        int mazeHeight = maze.length;
        int mazeWidth = maze[0].length;

        if (leftCell < 0 || rightCell >= mazeWidth ||
            bottomCell < 0 || topCell >= mazeHeight) {
            return true;
        }

        if (maze[topCell][leftCell] == 1) return false;
        if (maze[topCell][rightCell] == 1) return false;
        if (maze[bottomCell][leftCell] == 1) return false;
        if (maze[bottomCell][rightCell] == 1) return false;

        return true;
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);
        // Рисуем глаза монстра
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(x + width * 0.3f, y + height * 0.7f, width * 0.15f);
        shapeRenderer.circle(x + width * 0.7f, y + height * 0.7f, width * 0.15f);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(x + width * 0.3f, y + height * 0.7f, width * 0.08f);
        shapeRenderer.circle(x + width * 0.7f, y + height * 0.7f, width * 0.08f);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
