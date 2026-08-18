package io.wasabi.urg.elements.game;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.tiles.DefaultTile;
import io.wasabi.urg.elements.tiles.TileType;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.util.tweens.Tween;

public class Wheel {
    private static final Roulette GAME = Roulette.getInstance();
    private static final RunState RUN_STATE = GAME.getRunState();

    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final ShapeRenderer SHAPE_RENDERER = RENDERER_MANAGER.getShapeRenderer();

    private static final int[] WHEEL_NUMBER_ORDER = new int[] {
        0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};

    private final World world;

    private Vector2 position = new Vector2();
    private float rotation; // in Degrees
    private float radius;
    private float tileSize;

    private final Body body;

    private Tween wheelVelocityTween;

    private final List<Tile> tiles = RUN_STATE.getTiles();

    public Wheel(World world, Vector2 position) {
        this.world = world;
        this.position = position;

        // Testing
        radius = 80f;
        tileSize = 25;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KinematicBody;
        bodyDef.position.set(position);
        body = this.world.createBody(bodyDef);

        for (int i = 0; i < WHEEL_NUMBER_ORDER.length; i++) {
            TileType type = new DefaultTile();
            if (i % 2 == 0) {
                if (i == 0) {
                    type.setColour(TileType.TileColour.GREEN);
                } else {
                    type.setColour(TileType.TileColour.BLACK);
                }
            } else {
                type.setColour(TileType.TileColour.RED);
            }
            Tile tile = new Tile(world, type, WHEEL_NUMBER_ORDER[i], position, radius, tileSize);
            tiles.add(tile);
        }

        addRing(radius, 0.3f, 0.5f, false);

        body.setAngularVelocity(-10f);

        update();
    }

    public void setPosition(Vector2 vec) {
        this.position.x = vec.x;
        this.position.y = vec.y;

        body.setTransform(position, 0);
        update();
    }

    public void setRotation(float rot) {
        this.rotation = rot;
        update();
    }

    public void setSize(float radius, float tileSize) {
        this.radius = radius;
        this.tileSize = tileSize;
        update();
    }

    private float getBaseTileAngle() {
        float ang;
        float total = 0;

        for (Tile tile : tiles) {
            total += tile.getSize();
        }
        ang = 360f / total;

        return ang;
    }

    private Fixture addRing(float radius, float friction, float restitution, boolean startAsSensor) {
        int segments = 64;
        Vector2[] points = new Vector2[segments];
        for (int i = 0; i < segments; i++) {
            float angle = i * (2f * MathUtils.PI / segments);
            points[i] = new Vector2(radius * MathUtils.cos(angle), radius * MathUtils.sin(angle));
        }

        ChainShape chain = new ChainShape();
        chain.createLoop(points);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = chain;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;
        fixtureDef.isSensor = startAsSensor;

        Fixture fixture = body.createFixture(fixtureDef);
        chain.dispose();
        return fixture;
    }

    private void update() {
        float ang = getBaseTileAngle();
        float angc = rotation;

        for (Tile tile : tiles) {
            tile.setDegrees(ang);
            tile.setRotation(angc);
            angc += ang * MathUtils.degreesToRadians * tile.getSize();
        }
    }

    public void render(float delta) {
        // placeholder render function
        float r1 = radius;
        float r2 = radius + tileSize;

        if (wheelVelocityTween != null) {
            body.setAngularVelocity(wheelVelocityTween.update(delta));
            setRotation(body.getAngle());
        }

        for (Tile tile : tiles) {
            tile.render();
        }

        SHAPE_RENDERER.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        SHAPE_RENDERER.circle(position.x, position.y, r1);
        SHAPE_RENDERER.circle(position.x, position.y, r2);
        SHAPE_RENDERER.end();
    }

    /**
     * Spins the wheel for a set amount of time, with given initial speed.
     * @param duration The spin time
     * @param initialSpeed The initial speed
     */
    public void spin(float duration, float initialSpeed) {
        wheelVelocityTween = new Tween(duration, initialSpeed, 0, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
    }

    public void dispose() {
        world.destroyBody(body);
    }

    public Body getBody() { return body; }
}
