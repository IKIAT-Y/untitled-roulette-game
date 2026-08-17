package io.wasabi.urg.elements.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.managers.RendererManager;

public class BettingTable extends GameObject {
    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final ShapeRenderer SHAPE_RENDERER = RENDERER_MANAGER.getShapeRenderer();

    private float posX;
    private float posY;

    public void setPosition(float x, float y) {
        this.posX = x;
        this.posY = y;
    }

    @Override
    public void render() {
        ShapeRenderer shapeRenderer = SHAPE_RENDERER;

        // placeholder render function

    }

    @Override
    public void dispose() {

    }
}
