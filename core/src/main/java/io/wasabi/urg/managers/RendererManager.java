package io.wasabi.urg.managers;

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.Roulette;

public class RendererManager {
    private static final RendererManager INSTANCE = new RendererManager();

    private Roulette game;

    private boolean initialized;

    // Renderers
    private ShapeRenderer shapeRenderer;
    private PolygonSpriteBatch polyBatch;

    private RendererManager() {}

    public static RendererManager getInstance() {
        return INSTANCE;
    }

    public void initialize(Roulette game) {
        if (initialized) return;
        initialized = true;

        this.game = game;

        shapeRenderer = new ShapeRenderer();
        polyBatch = new PolygonSpriteBatch();
    }

    public void applyViewport(Viewport viewport) {
        viewport.apply();
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        polyBatch.setProjectionMatrix(viewport.getCamera().combined);

        // repeat this for every renderer we have :skull:
    }

    // Get functions
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
    public PolygonSpriteBatch getPolygonSpriteBatch() { return polyBatch; }
}
