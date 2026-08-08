package io.wasabi.urg.managers;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.GameObject;

/** Owns the shared game resources and coordinates all active game objects. */
public final class GameManager {
    private static final GameManager INSTANCE = new GameManager();

    private final Array<GameObject> gameObjects = new Array<>();

    private Roulette game;
    private Screen screen;
    private ShapeRenderer shapeRenderer;
    private World world;
    private boolean initialized;

    private GameManager() {
    }

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public void initialize(Roulette game) {
        if (initialized) {
            return;
        }

        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.world = new World(new Vector2(0f, 0f), true);
        this.initialized = true;
    }

    public void setScreen(Screen screen) {
        requireInitialized();
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    public World getWorld() {
        requireInitialized();
        return world;
    }

    public void addGameObject(GameObject gameObject) {
        requireInitialized();
        if (gameObject == null) {
            throw new IllegalArgumentException("gameObject cannot be null");
        }
        if (!gameObjects.contains(gameObject, true)) {
            gameObjects.add(gameObject);
        }
    }

    public boolean removeGameObject(GameObject gameObject) {
        requireInitialized();
        return gameObjects.removeValue(gameObject, true);
    }

    public int getGameObjectCount() {
        return gameObjects.size;
    }

    public void render(float delta) {
        requireInitialized();

        game.viewport.apply();
        shapeRenderer.setProjectionMatrix(game.viewport.getCamera().combined);

        for (GameObject gameObject : gameObjects) {
            gameObject.update(delta);
        }

        for (GameObject gameObject : gameObjects) {
            gameObject.render(shapeRenderer);
        }
    }

    public void dispose() {
        if (!initialized) {
            return;
        }

        for (GameObject gameObject : gameObjects) {
            gameObject.dispose();
        }
        gameObjects.clear();

        shapeRenderer.dispose();
        world.dispose();

        game = null;
        screen = null;
        shapeRenderer = null;
        world = null;
        initialized = false;
    }

    private void requireInitialized() {
        if (!initialized) {
            throw new IllegalStateException("GameManager must be initialized before use");
        }
    }
}
