package io.wasabi.urg.elements.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Ball {

    public enum State {
        SPINNING,   // on outer track, no inward movement
        DROPPING,   // still spinning moving inwards
        BOUNCING,   // free physics against frets on inner wheel
        SETTLING,   // almost stopped
        STOPPED     // fully stopped in pocket
    }

    private final World world;
    private final Body ball;
    private final float radius;

    // Used to track when to switch states
    private final Vector2 wheelCenter;
    private float outerTrackRadius;
    private float innerWheelRadius;

    // Used during Spin state
    private State state = State.STOPPED;
    private float tangentialSpeed = 0f;
    private float decelerationFactor = 0.999f;
    private float dropSpeedThreshold = 300f;
    private float settleSpeedThreshold = 5f;
    private float settleTimeRequired = 0.5f;
    private float settleTimer = 0f;

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
     * @param initialSpeed      initial spin speed, radians/sec
     * @param outerTrackRadius  radius of the outer track the ball starts on
     * @param innerWheelRadius  radius of the inner wheel surface it will drop onto
     */
    public void launch(float startAngleRad, float initialSpeed,
                       float outerTrackRadius, float innerWheelRadius) {
        this.outerTrackRadius = outerTrackRadius;
        this.innerWheelRadius = innerWheelRadius;
        this.tangentialSpeed = initialSpeed;
        this.settleTimer = 0f;

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
    public void update(float delta) {
        System.out.println(state.toString());
        switch (state) {
            case SPINNING:
                updateSpinning(delta);
                break;
            case DROPPING:
                updateDropping(delta);
                break;
            case BOUNCING:
                updateBouncing(delta);
                break;
            case SETTLING:
                updateSettling(delta);
                break;
            case STOPPED:
                return;
        }

        // It says timeStep should not vary, but this works?
        world.step(delta, 6, 2);
    }

    /**
     * Calculates a directional vector from the center of the wheel table to the ball.
     * Does so by getting the balls relative position by subtracting its absolute position by the
     * wheel center and the normalizing it.
     */
    private Vector2 radialDirFromCenter() {
        return new Vector2(ball.getPosition()).sub(wheelCenter).nor();
    }

    private void updateSpinning(float delta) {
        Vector2 radialDir = radialDirFromCenter();
        Vector2 tangentDir = new Vector2(-radialDir.y, radialDir.x);

        // Hold the ball on the circular track via centripetal force
        float radius = outerTrackRadius;
        float centripetalMag = ball.getMass() * tangentialSpeed * tangentialSpeed / radius;
        ball.applyForceToCenter(radialDir.scl(-centripetalMag), true);

        // Decay speed
        tangentialSpeed *= decelerationFactor;

        ball.setLinearVelocity(tangentDir.scl(tangentialSpeed));

        // Switch state once it drops below a certain speed
        if (tangentialSpeed <= dropSpeedThreshold) {
            state = State.DROPPING;
        }
    }

    private void updateDropping(float delta) {
        Vector2 toCenter = new Vector2(wheelCenter).sub(ball.getPosition());
        float distFromCenter = toCenter.len();
        Vector2 radialInward = toCenter.cpy().nor();
        Vector2 tangentDir = new Vector2(radialInward.y, -radialInward.x);

        // Used to apply one more centripetal force so it doesn't go flying away
        float centripetalMag = ball.getMass() * tangentialSpeed * tangentialSpeed / distFromCenter;

        // Extra inward pull force
        float extraInwardPull = ball.getMass() * 2000f;

        ball.applyForceToCenter(radialInward.scl(centripetalMag + extraInwardPull), true);

        tangentialSpeed *= (float) Math.pow(decelerationFactor, delta); // Fast decay

        // Actual ball velocity
        Vector2 currentVel = ball.getLinearVelocity();

        // Velocity if the ball were to move in a straight line towards the center
        Vector2 desiredTangentVel = tangentDir.scl(tangentialSpeed);

        // Linear interpolation of currentVel (90%) and desiredTangentVel (10%)
        ball.setLinearVelocity(
            currentVel.x * 0.9f + desiredTangentVel.x * 0.1f,
            currentVel.y * 0.9f + desiredTangentVel.y * 0.1f
        );

        if (distFromCenter <= innerWheelRadius - (2 * radius)) {
            state = State.BOUNCING;
        }
    }

    private void updateBouncing(float delta) {
        Vector2 toCenter = new Vector2(wheelCenter).sub(ball.getPosition());
        Vector2 radialInward = toCenter.cpy().nor();

        // Apply inwards force to simulate gravity from slope
        float bowlPullMag = ball.getMass() * 1000f;
        ball.applyForceToCenter(radialInward.scl(bowlPullMag), true);

        // Damp the linear velocity of the ball
        float bounceDampingPerSecond = 0.25f;
        float dampingThisFrame = 1f - (float) Math.pow(1f - bounceDampingPerSecond, delta);
        Vector2 vel = ball.getLinearVelocity();
        ball.setLinearVelocity(
            vel.x * (1f - dampingThisFrame),
            vel.y * (1f - dampingThisFrame)
        );

        // When reaching a low enough speed switch to settle state
        float speed = ball.getLinearVelocity().len();
        if (speed <= settleSpeedThreshold) {
            settleTimer = 0f;
            state = State.SETTLING;
        }
    }

    private void updateSettling(float delta) {
        // While settling apply large damping to put it to a complete stop
        float bounceDampingPerSecond = 0.9f;
        float dampingThisFrame = 1f - (float) Math.pow(1f - bounceDampingPerSecond, delta);
        Vector2 vel = ball.getLinearVelocity();
        ball.setLinearVelocity(
            vel.x * (1f - dampingThisFrame),
            vel.y * (1f - dampingThisFrame)
        );

        // After a fixed amount of time assume fully settled
        settleTimer += delta;
        if (settleTimer >= settleTimeRequired) {
            finalizeStop();
        }
    }

    private void finalizeStop() {
        ball.setLinearVelocity(0f, 0f);
        ball.setAngularVelocity(0f);
        ball.setType(BodyType.StaticBody);
        state = State.STOPPED;
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

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.circle(
            ball.getPosition().x,
            ball.getPosition().y,
            radius
        );
        shapeRenderer.end();
    }

    public void dispose() {
        world.destroyBody(ball);
    }
}
