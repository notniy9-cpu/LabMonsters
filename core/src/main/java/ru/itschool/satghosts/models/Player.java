package ru.itschool.satghosts.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    public float x, y;
    public float width, height;
    public Rectangle bounds;

    public Player(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void update(float delta) {
        bounds.set(x, y, width, height);
    }

    public void move(float dx, float dy) {
        x += dx;
        y += dy;
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y, width, height);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
