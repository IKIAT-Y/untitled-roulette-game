package io.wasabi.urg.elements.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

// AI Generated TEMP class for testing

/**
 * Radial dividers between pockets. Purely the fret bars — containment
 * (stopping the ball escaping the pocket ring) is handled separately
 * by WheelBoundary.
 */
public class Frets {

    private final World world;
    private final Vector2 center;
    private final float innerRadius;
    private final float outerRadius;
    private final int pocketCount;
    private final float thickness;

    private Body body;

    public Frets(World world, Vector2 center, float innerRadius, float outerRadius,
                 int pocketCount, float thickness) {
        this.world = world;
        this.center = center;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.pocketCount = pocketCount;
        this.thickness = thickness;

        build();
    }

    private void build() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(center);
        body = world.createBody(bodyDef);

        float fretRadialLength = outerRadius - innerRadius;
        float fretCenterRadius = (innerRadius + outerRadius) / 2f;
        float angleStep = MathUtils.degreesToRadians * (360f / pocketCount);

        for (int i = 0; i < pocketCount; i++) {
            // One fret per pocket boundary, matching the divider lines
            // drawn by Wheel.render() (angle = i * angleStep, not pocket centers).
            float angle = i * angleStep;

            float fretCenterX = fretCenterRadius * MathUtils.cos(angle);
            float fretCenterY = fretCenterRadius * MathUtils.sin(angle);

            PolygonShape fretShape = new PolygonShape();
            fretShape.setAsBox(
                fretRadialLength / 2f,
                thickness / 2f,
                new Vector2(fretCenterX, fretCenterY),
                angle
            );

            FixtureDef fretFixture = new FixtureDef();
            fretFixture.shape = fretShape;
            fretFixture.friction = 0.6f;
            fretFixture.restitution = 0.5f;

            body.createFixture(fretFixture);
            fretShape.dispose();
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        Vector2 bodyPos = body.getPosition();
        float bodyAngle = body.getAngle();

        for (Fixture fixture : body.getFixtureList()) {
            PolygonShape shape = (PolygonShape) fixture.getShape();
            int vertexCount = shape.getVertexCount();

            Vector2 tmp = new Vector2();
            float[] worldVerts = new float[vertexCount * 2];

            for (int i = 0; i < vertexCount; i++) {
                shape.getVertex(i, tmp);
                tmp.rotateRad(bodyAngle).add(bodyPos);
                worldVerts[i * 2] = tmp.x;
                worldVerts[i * 2 + 1] = tmp.y;
            }

            shapeRenderer.polygon(worldVerts);
        }

        shapeRenderer.end();
    }

    public Body getBody() {
        return body;
    }

    public void dispose() {
        world.destroyBody(body);
    }
}
