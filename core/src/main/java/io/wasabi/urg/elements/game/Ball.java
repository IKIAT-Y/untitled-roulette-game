package io.wasabi.urg.elements.game;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.state.RunState;

public class Ball extends GameObject {

    public enum State {
        SPINNING,   // on outer track, no inward movement
        DROPPING,   // still spinning moving inwards
        BOUNCING,   // free physics against frets on inner wheel
        SETTLING,   // almost stopped
        STOPPED     // fully stopped in pocket
    }

    private static final Roulette GAME = Roulette.getInstance();
    private static final RunState RUN_STATE = GAME.getRunState();

    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final ShapeRenderer SHAPE_RENDERER = RENDERER_MANAGER.getShapeRenderer();

    private final World world;
    private final Body ball;
    private final float radius;

    // Used to track when to switch states
    private final Vector2 wheelCenter;
    private float outerTrackRadius;
    private float innerWheelRadius;

    // Used for fixed spinning in SPINNING AND DROPPING states
    private float currentAngleRad = 0f;
    private float currentRadius = 0f;

    // How fast the ball moves radially inward during DROPPING
    private float radialDropSpeed = 250f;

    // Used during Spin state
    private State state = State.STOPPED;
    private float tangentialSpeed = 0f;

    private float decelerationFactor = 0.5f;
    private float dropDecelerationFactor = 0.2f;

    // Speed to enter DROPPING state
    private float dropSpeedThreshold = 400f;

    // Speed to enter SETTLE state
    private float settleSpeedThreshold = 10f;
    private float settleTimeRequired = 0.5f;
    private float settleTimer = 0f;

    // Used to cap the frame time between frames so a huge lag spike doesn't kill the physics
    private static final float MAX_DELTA = 1f / 20f;

    // Framed timeStep for physics steps
    private static final float FIXED_TIMESTEP = 1f / 60f;
    private static final int MAX_STEPS_PER_FRAME = 8; // avoid spiral of death on big lag spikes

    // Keeps track of real time that has passed that hasn't been simulated yet
    private float physicsAccumulator = 0f;

    public Ball(World world, float ballRadius, Vector2 wheelCenter) {
        this.world = world;
        this.radius = ballRadius;
        this.wheelCenter = wheelCenter;

        BodyDef ballDef = new BodyDef();
        ballDef.type = BodyType.DynamicBody;
        ballDef.linearDamping = 0.05f;
        ballDef.angularDamping = 0.1f;
        ballDef.bullet = true;
        this.ball = world.createBody(ballDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(ballRadius);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 0.1f;
        fd.friction = 0.6f;
        fd.restitution = 0.8f;
        ball.createFixture(fd);

        shape.dispose();
    }

    /**
     * Launches the ball into a spin on the outer track.
     *
     * @param startAngleRad     starting angle around wheelCenter, radians
     * @param initialSpeed      initial tangential (linear) speed, units/sec — note this is
     *                          NOT radians/sec, it's divided by radius internally to get
     *                          angular velocity
     * @param outerTrackRadius  radius of the outer track the ball starts on
     * @param innerWheelRadius  radius of the inner wheel surface it will drop onto
     */
    public void launch(float startAngleRad, float initialSpeed,
                       float outerTrackRadius, float innerWheelRadius) {
        this.outerTrackRadius = outerTrackRadius;
        this.innerWheelRadius = innerWheelRadius;
        this.tangentialSpeed = initialSpeed;
        this.settleTimer = 0f;
        this.currentAngleRad = startAngleRad;
        this.currentRadius = outerTrackRadius;
        this.physicsAccumulator = 0f;

        Vector2 startPos = new Vector2(
            wheelCenter.x + outerTrackRadius * (float) Math.cos(startAngleRad),
            wheelCenter.y + outerTrackRadius * (float) Math.sin(startAngleRad)
        );
        ball.setTransform(startPos, 0f);
        ball.setLinearVelocity(0f, 0f);
        ball.setAngularVelocity(0f);

        state = State.SPINNING;
    }

    /**
     * Updates the ball using different behaviour depending on the state it is in
     */
    @Override
    public void update(float delta) {
        // Clamp so a lag spike doesn't destroy the simulation
        float dt = Math.min(delta, MAX_DELTA);

        //System.out.println(state.toString());

        switch (state) {
            case SPINNING:
                updateSpinning(dt);
                world.step(dt, 6, 2);
                break;
            case DROPPING:
                updateDropping(dt);
                world.step(dt, 6, 2);
                break;
            case BOUNCING:
            case SETTLING:
                stepPhysicsFixed(dt);
                break;
            case STOPPED:
                world.step(dt, 6, 2);
                break;
        }
    }

    /**
     * Advances BOUNCING/SETTLING physics in fixed-size chunks, accumulating leftover real
     * time between calls.
     */
    private void stepPhysicsFixed(float dt) {
        physicsAccumulator += dt;

        int steps = 0;
        while (physicsAccumulator >= FIXED_TIMESTEP && steps < MAX_STEPS_PER_FRAME) {
            if (state == State.BOUNCING) {
                updateBouncing();
            } else if (state == State.SETTLING) {
                updateSettling();
            } else {
                break;
            }
            world.step(FIXED_TIMESTEP, 6, 2);
            physicsAccumulator -= FIXED_TIMESTEP;
            steps++;
        }
    }

    /**
     * Used to set position of the ball from the wheel center using angle in radian and distance
     */
    private void setPositionFromPolar(float angleRad, float radialDist) {
        Vector2 pos = new Vector2(
            wheelCenter.x + radialDist * (float) Math.cos(angleRad),
            wheelCenter.y + radialDist * (float) Math.sin(angleRad)
        );
        ball.setTransform(pos, 0f);
    }

    private void updateSpinning(float delta) {
        // Angular velocity from the current tangential speed and radius.
        float angularVelocity = tangentialSpeed / currentRadius;
        currentAngleRad += angularVelocity * delta;

        setPositionFromPolar(currentAngleRad, currentRadius);

        // Used power of delta because value is being multiplied
        tangentialSpeed *= (float) Math.pow(decelerationFactor, delta);

        // Switch state once it drops below a certain speed
        if (tangentialSpeed <= dropSpeedThreshold) {
            state = State.DROPPING;
        }
    }

    private void updateDropping(float delta) {
        // Same as SPINNING state but radius decrease over time
        float angularVelocity = tangentialSpeed / currentRadius;
        currentAngleRad += angularVelocity * delta;
        currentRadius -= radialDropSpeed * delta;

        setPositionFromPolar(currentAngleRad, currentRadius);

        // Different deceleration factor to SPINNING state
        tangentialSpeed *= (float) Math.pow(dropDecelerationFactor, delta);

        if (currentRadius <= innerWheelRadius - (2 * radius)) {
            // Here we calculate the velocity the ball would have
            // at this position and give it the ball before switching
            // to using Box2D for bouncing physics
            Vector2 tangentDir = new Vector2(
                -(float) Math.sin(currentAngleRad),
                (float) Math.cos(currentAngleRad)
            );
            Vector2 radialOutDir = new Vector2(
                (float) Math.cos(currentAngleRad),
                (float) Math.sin(currentAngleRad)
            );
            Vector2 exitVelocity = tangentDir.scl(tangentialSpeed)
                .add(radialOutDir.scl(-radialDropSpeed));
            ball.setLinearVelocity(exitVelocity);

            state = State.BOUNCING;
            SoundManager.getInstance().playSound("bounce1");
        }
    }

    private void updateBouncing() {
        Vector2 toCenter = new Vector2(wheelCenter).sub(ball.getPosition());
        Vector2 radialInward = toCenter.cpy().nor();

        // Apply inwards force to simulate gravity from slope
        float bowlPullMag = ball.getMass() * 1000f;
        ball.applyForceToCenter(radialInward.scl(bowlPullMag), true);

        // Damp the linear velocity of the ball
        float bounceDampingPerSecond = 0.25f;
        float dampingThisFrame = 1f - (float) Math.pow(1f - bounceDampingPerSecond, Ball.FIXED_TIMESTEP);
        Vector2 vel = ball.getLinearVelocity();
        ball.setLinearVelocity(
            vel.x * (1f - dampingThisFrame),
            vel.y * (1f - dampingThisFrame)
        );

        System.out.println("Bouncing speed: " + ball.getLinearVelocity().len());

        // When reaching a low enough speed switch to settle state
        float speed = ball.getLinearVelocity().len();
        if (speed <= settleSpeedThreshold) {
            settleTimer = 0f;
            state = State.SETTLING;
        }
    }

    private void updateSettling() {
        // While settling apply large damping to put it to a complete stop
        float bounceDampingPerSecond = 0.99f;
        float dampingThisFrame = 1f - (float) Math.pow(1f - bounceDampingPerSecond, Ball.FIXED_TIMESTEP);
        Vector2 vel = ball.getLinearVelocity();
        ball.setLinearVelocity(
            vel.x * (1f - dampingThisFrame),
            vel.y * (1f - dampingThisFrame)
        );

        System.out.println("Settling speed: " + ball.getLinearVelocity().len());

        // After a fixed amount of time assume fully settled
        settleTimer += Ball.FIXED_TIMESTEP;
        if (settleTimer >= settleTimeRequired) {
            finalizeStop();
        }
    }

    private void finalizeStop() {
        ball.setLinearVelocity(0f, 0f);
        ball.setAngularVelocity(0f);
        ball.setType(BodyType.DynamicBody);
        state = State.STOPPED;

        Tile tile = getLandedTile();
        if (tile != null) {
            System.out.println("Landed on tile: " + tile.getNumber());
            System.out.println("Tile multiplier: " + tile.getBetMultiplier());
        }

        Roulette.getInstance().getRunState().setLastTile(tile);
        Roulette.getInstance().getRoundManager().recordSpin();
    }

    private Tile getLandedTile() {
        List<Tile> tiles = RUN_STATE.getTiles();

        int segments = 16;
        float[] circVerts = new float[segments * 2];
        float ang = 360f / segments;
        float x = ball.getPosition().x;
        float y = ball.getPosition().y;

        for (int i = 0; i < segments; i++) {
            int v = i * 2;
            float rad = i * ang * MathUtils.degreesToRadians;
            circVerts[v] = x + radius * MathUtils.cos(rad);
            circVerts[v + 1] = y + radius * MathUtils.sin(rad);
        }

        Polygon circPoly = new Polygon(circVerts);

        for (Tile t : tiles) {
            PolygonRegion region = t.getRegion();
            float[] verts = region.getVertices();
            Polygon poly = new Polygon(verts);

            if (Intersector.overlapConvexPolygons(poly, circPoly)) {
                return t;
            }
        }

        return null;
    }

    public State getState() {
        return state;
    }

    public World getWorld() {
        return world;
    }

    public Body getBody() {
        return ball;
    }

    @Override
    public void render() {
        SHAPE_RENDERER.begin(ShapeType.Filled);
        SHAPE_RENDERER.circle(
            ball.getPosition().x,
            ball.getPosition().y,
            radius
        );
        SHAPE_RENDERER.end();
    }

    @Override
    public void dispose() {
        world.destroyBody(ball);
    }
}
