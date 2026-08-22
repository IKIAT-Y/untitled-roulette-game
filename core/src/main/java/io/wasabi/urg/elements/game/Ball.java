package io.wasabi.urg.elements.game;

import java.util.ArrayList;
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
import io.wasabi.urg.elements.betting.Bet;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.util.tweens.Tween;

public class Ball extends GameObject {

    public enum State {
        SPINNING, // on outer track, no inward movement
        DROPPING, // still spinning moving inwards
        BOUNCING, // free physics against frets on inner wheel
        SETTLING, // almost stopped
        STOPPED // fully stopped in pocket
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
    private float innerWheelRadius;
    private float outerTrackRadius;

    // Used for fixed spinning in SPINNING AND DROPPING states
    private float currentAngleRad = 0f;
    private float currentRadius = 0f;

    // Used during Spin state
    private State state = State.STOPPED;
    private float tangentialSpeed = 0f;
    private boolean freeSpin = false;

    private Tween dropTween;
    private float settleTimer = 0f;
    private float lowSpeedTimer = 0f;

    // Visibility
    private boolean visible = false;

    // Used to cap the frame time between frames so a huge lag spike doesn't kill
    // the physics
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
     * @param startAngleRad    starting angle around wheelCenter, radians
     * @param initialSpeed     initial tangential (linear) speed, units/sec — note
     *                         this is
     *                         NOT radians/sec, it's divided by radius internally to
     *                         get
     *                         angular velocity
     * @param outerTrackRadius radius of the outer track the ball starts on
     * @param innerWheelRadius radius of the inner wheel surface it will drop onto
     */
    public void launch(float startAngleRad, float initialSpeed, float outerTrackRadius, float innerWheelRadius) {
        freeSpin = false;
        launchBall(startAngleRad, initialSpeed, outerTrackRadius, innerWheelRadius);
    }

    public void launchFree(float startAngleRad, float initialSpeed,
            float outerTrackRadius, float innerWheelRadius) {
        freeSpin = true;
        launchBall(startAngleRad, initialSpeed, outerTrackRadius, innerWheelRadius);
    }

    private void launchBall(float startAngleRad, float initialSpeed,
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
                wheelCenter.y + outerTrackRadius * (float) Math.sin(startAngleRad));
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

        // System.out.println(state.toString());
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
     * Advances BOUNCING/SETTLING physics in fixed-size chunks, accumulating
     * leftover real
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
     * Used to set position of the ball from the wheel center using angle in radian
     * and distance
     */
    private void setPositionFromPolar(float angleRad, float radialDist) {
        Vector2 pos = new Vector2(
                wheelCenter.x + radialDist * (float) Math.cos(angleRad),
                wheelCenter.y + radialDist * (float) Math.sin(angleRad));
        ball.setTransform(pos, 0f);
    }

    private void applyTangentialDamping() {
        Vector2 radialDir = new Vector2(wheelCenter).sub(ball.getPosition()).nor();
        Vector2 tangentDir = new Vector2(-radialDir.y, radialDir.x);

        Vector2 vel = ball.getLinearVelocity();
        float radialComp = vel.dot(radialDir);
        float tangentComp = vel.dot(tangentDir);

        float tangentialDampingPerSecond = 0.6f;
        float dampingThisFrame = 1f - (float) Math.pow(1f - tangentialDampingPerSecond, FIXED_TIMESTEP);
        tangentComp *= (1f - dampingThisFrame);

        Vector2 newVel = radialDir.scl(radialComp).add(tangentDir.scl(tangentComp));
        ball.setLinearVelocity(newVel);
    }

    private void updateSpinning(float delta) {
        float angularVelocity = tangentialSpeed / currentRadius;
        currentAngleRad += angularVelocity * delta;

        setPositionFromPolar(currentAngleRad, currentRadius);

        float decelerationFactor = 0.5f;
        tangentialSpeed *= (float) Math.pow(decelerationFactor, delta);

        // Speed to enter DROPPING state
        float dropSpeedThreshold = 400f;
        if (tangentialSpeed <= dropSpeedThreshold) {
            float targetRadius = innerWheelRadius - (2 * radius);
            // tune this — how long the drop takes
            float dropDuration = 0.6f;
            dropTween = new Tween(dropDuration, currentRadius, targetRadius,
                    Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
            state = State.DROPPING;
        }
    }

    private void updateDropping(float delta) {
        float angularVelocity = tangentialSpeed / currentRadius;
        currentAngleRad += angularVelocity * delta;

        float previousRadius = currentRadius;
        currentRadius = dropTween.update(delta);
        // derived each frame, used for exit velocity
        float dropRadialSpeed = (previousRadius - currentRadius) / delta; // inward = positive

        setPositionFromPolar(currentAngleRad, currentRadius);

        float dropDecelerationFactor = 0.2f;
        tangentialSpeed *= (float) Math.pow(dropDecelerationFactor, delta);

        if (dropTween.isComplete() || currentRadius <= innerWheelRadius - (2 * radius)) {
            Vector2 tangentDir = new Vector2(
                    -(float) Math.sin(currentAngleRad),
                    (float) Math.cos(currentAngleRad));
            Vector2 radialOutDir = new Vector2(
                    (float) Math.cos(currentAngleRad),
                    (float) Math.sin(currentAngleRad));
            Vector2 exitVelocity = tangentDir.scl(tangentialSpeed)
                    .add(radialOutDir.scl(-dropRadialSpeed));
            ball.setLinearVelocity(exitVelocity);

            state = State.BOUNCING;
            SoundManager.getInstance().playSound("bounce1");
        }
    }

    private void updateBouncing() {
        Vector2 toCenter = new Vector2(wheelCenter).sub(ball.getPosition());
        Vector2 radialInward = toCenter.cpy().nor();

        float bowlPullMag = ball.getMass() * 700f;
        ball.applyForceToCenter(radialInward.scl(bowlPullMag), true);

        float bounceDampingPerSecond = 0.25f;
        float dampingThisFrame = 1f - (float) Math.pow(1f - bounceDampingPerSecond, Ball.FIXED_TIMESTEP);
        Vector2 vel = ball.getLinearVelocity();
        ball.setLinearVelocity(
                vel.x * (1f - dampingThisFrame),
                vel.y * (1f - dampingThisFrame));

        applyTangentialDamping();

        float speed = ball.getLinearVelocity().len();

        // Speed to enter SETTLE state
        float settleSpeedThreshold = 50f;
        if (speed <= settleSpeedThreshold) {
            lowSpeedTimer += FIXED_TIMESTEP;

            // must stay slow this long before settling
            float lowSpeedTimeRequired = 0.5f;
            if (lowSpeedTimer >= lowSpeedTimeRequired) {
                settleTimer = 0f;
                state = State.SETTLING;
            }
        } else {
            lowSpeedTimer = 0f;
        }
    }

    private void updateSettling() {
        float speed = ball.getLinearVelocity().len();

        // Got knocked again (e.g. rolled off a fret) — go back to real bounce physics
        // speed that means "still actually bouncing"
        float bounceResumeThreshold = 60f;
        if (speed > bounceResumeThreshold) {
            lowSpeedTimer = 0f;
            state = State.BOUNCING;
            return;
        }

        float bounceDampingPerSecond = 0.99f;
        float dampingThisFrame = 1f - (float) Math.pow(1f - bounceDampingPerSecond, Ball.FIXED_TIMESTEP);

        applyTangentialDamping();

        Vector2 vel = ball.getLinearVelocity();
        ball.setLinearVelocity(
                vel.x * (1f - dampingThisFrame),
                vel.y * (1f - dampingThisFrame));

        settleTimer += Ball.FIXED_TIMESTEP;
        float settleTimeRequired = 0.5f;
        if (settleTimer >= settleTimeRequired) {
            finalizeStop();
        }
    }

    private void finalizeStop() {
        ball.setLinearVelocity(0f, 0f);
        ball.setAngularVelocity(0f);
        ball.setType(BodyType.DynamicBody);
        state = State.STOPPED;

        List<Bet> savedBets = new ArrayList<>(Roulette.getInstance().getRunState().getActiveBets());

        Tile tile = getLandedTile();
        if (tile != null) {
            System.out.println("Landed on tile: " + tile.getNumber());
            System.out.println("Tile multiplier: " + tile.getBetMultiplier());
        }

        Roulette.getInstance().getGameScreen().getWheel().resetWheelTweens();

        Roulette.getInstance().getRunState().setLastTile(tile);

        Roulette.getInstance().getRunState().resolveActiveBets();

        Roulette.getInstance().getRoundManager().recordSpin(freeSpin);

        boolean freeSpinRequested = Roulette.getInstance().getRunState().consumeFreeSpinRequest();
    
        if (Roulette.getInstance().getRunState().getChips() >= Roulette.getInstance().getRoundManager().getCurrentConfig().getQuota()) {
            return;
        }

        if (freeSpinRequested) {
            Roulette.getInstance().getRunState().getActiveBets().addAll(savedBets);
            Roulette.getInstance().getGameScreen().freeSpin();
        }
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
        if (visible) {
            SHAPE_RENDERER.begin(ShapeType.Filled);
            SHAPE_RENDERER.circle(
                    ball.getPosition().x,
                    ball.getPosition().y,
                    radius);
            SHAPE_RENDERER.end();
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void dispose() {
        world.destroyBody(ball);
    }}

    

    
    
        
    
        
    