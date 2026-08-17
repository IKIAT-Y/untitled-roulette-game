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
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.state.RunState;

public class Wheel {
    private static final Roulette GAME = Roulette.getInstance();
    private static final RunState RUN_STATE = GAME.getRunState();

    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final ShapeRenderer SHAPE_RENDERER = RENDERER_MANAGER.getShapeRenderer();

    private final World world;

    private Vector2 position = new Vector2();
    private float rotation; // in Degrees
    private float radius;
    private float tileSize;
    private static final float STARTING_SPEED = 3.0f;
    private float speed;

    private final Body body;
    private final Fixture innerFixture;

    // placeholder, i think this should be actually stored inside the Player's class
    public List<Tile> tiles = RUN_STATE.getTiles();

    public Wheel(World world, Vector2 position) {
        this.world = world;
        this.position = position;
        speed = STARTING_SPEED;

        // Testing
        radius = 80f;
        tileSize = 25;

        for (int i = 0; i < 37; i++) {
            Tile tile = new Tile(world, i, position, radius, tileSize);
            //tile.setSize(0.5f + MathUtils.random.nextFloat());
            tiles.add(tile);
        }

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(position);
        body = this.world.createBody(bodyDef);

        innerFixture = addRing(radius, 0.3f, 0.5f, false);

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

    public void render() {
        // placeholder render function
        float r1 = radius;
        float r2 = radius + tileSize;

        setRotation(rotation - speed);
        speed *= 0.98;

        for (Tile tile : tiles) {
            tile.render();
        }

        SHAPE_RENDERER.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        SHAPE_RENDERER.circle(position.x, position.y, r1);
        SHAPE_RENDERER.circle(position.x, position.y, r2);
        SHAPE_RENDERER.end();
    }
    
    public boolean containsPoint(Vector2 point) {
        return point.dst2(position) <= (radius + tileSize) * (radius + tileSize);
    }

    public void spin() {
        speed = STARTING_SPEED;
    }

    public void dispose() {
        world.destroyBody(body);
    }


}
