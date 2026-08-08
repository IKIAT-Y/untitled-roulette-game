package io.wasabi.urg.elements.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import io.wasabi.urg.elements.GameObject;

// AI Generated TEMP class for testing

/**
 * Circular containment walls for the pocket ring: a solid inner wall (the
 * ball should never reach the center) and an outer wall that starts as a
 * sensor (so the ball can drop in from outside) and is activated to solid
 * once the ball has actually entered, so fret bounces can't escape back out.
 */
public class WheelBoundary extends GameObject {

    private final World world;
    private final Vector2 center;
    private final float innerRadius;
    private final float outerRadius;

    private Body body;
    private Fixture outerFixture;
    private Fixture innerFixture;
    private boolean outerActivated = false;
    private final Ball ball;

    public WheelBoundary(World world, Vector2 center, float innerRadius, float outerRadius, Ball ball) {
        this.world = world;
        this.center = center;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.ball = ball;

        build();
    }

    private void build() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(center);
        body = world.createBody(bodyDef);

        // Outer wall starts as a sensor so the ball can pass through it
        // inward during DROPPING.
        outerFixture = addRing(outerRadius, 0.3f, 0.5f, true);
        innerFixture = addRing(innerRadius, 0.3f, 0.5f, false);
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

    /**
     * Call once per frame, before stepping the ball. Activates the outer
     * wall (sensor -> solid) the first time the ball enters BOUNCING.
     */
    @Override
    public void update(float delta) {
        if (!outerActivated && ball.getState() == Ball.State.BOUNCING) {
            outerFixture.setSensor(false);
            outerActivated = true;
        }
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        Vector2 bodyPos = body.getPosition();
        float bodyAngle = body.getAngle();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(outerFixture.isSensor() ? Color.GREEN : Color.YELLOW);
        drawChain((ChainShape) outerFixture.getShape(), bodyPos, bodyAngle, shapeRenderer);

        shapeRenderer.setColor(Color.CYAN);
        drawChain((ChainShape) innerFixture.getShape(), bodyPos, bodyAngle, shapeRenderer);

        shapeRenderer.end();
    }

    private void drawChain(ChainShape chain, Vector2 bodyPos, float bodyAngle, ShapeRenderer shapeRenderer) {
        int vertexCount = chain.getVertexCount();
        Vector2 tmp = new Vector2();
        Vector2 prev = null;
        Vector2 first = null;

        for (int i = 0; i < vertexCount; i++) {
            chain.getVertex(i, tmp);
            Vector2 world = new Vector2(tmp).rotateRad(bodyAngle).add(bodyPos);

            if (first == null) first = new Vector2(world);
            if (prev != null) {
                shapeRenderer.line(prev.x, prev.y, world.x, world.y);
            }
            prev = world;
        }

        // close the loop
        if (prev != null && first != null) {
            shapeRenderer.line(prev.x, prev.y, first.x, first.y);
        }
    }

    public Body getBody() {
        return body;
    }

    @Override
    public void dispose() {
        world.destroyBody(body);
    }
}
