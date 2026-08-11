package io.wasabi.urg.elements.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import io.wasabi.urg.managers.RendererManager;

public class Tile {

    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final PolygonSpriteBatch POLY_BATCH = RENDERER_MANAGER.getPolygonSpriteBatch();

    private World world;

    private int number;
    private float size = 1; // multiplier

    private Vector2 position;
    private float degrees;
    private float rotation = 0.0f;
    private float radius;
    private float height;
    private float numHeight;

    private Texture tex;
    private Texture fretTex;
    private PolygonRegion region;
    private PolygonRegion fretRegion;

    private Body body; // for frets
    private Fixture fret;

    public Tile(World world, int number, Vector2 position, float radius, float height) {
        this.world = world;

        this.number = number;
        this.position = position;
        this.radius = radius;
        this.height = height;
        this.numHeight = height;

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        if (number % 2 == 0) {
            pix.setColor(0xFF0000FF);
        } else {
            pix.setColor(0x000000FF);
        }
        pix.fill();

        tex = new Texture(pix);

        Pixmap fretPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        fretPix.setColor(0xFFFFFFFF);
        fretPix.fill();
        fretTex = new Texture(fretPix);

        build();
        update();
    }

    private void build() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        body = this.world.createBody(bodyDef);
        body.setTransform(position, 0);

        PolygonShape fretShape = new PolygonShape();
        fretShape.setAsBox(
            height,
            1,
            new Vector2(0, 0),
            0
        );

        FixtureDef fretFixture = new FixtureDef();
        fretFixture.shape = fretShape;
        fretFixture.friction = 0.6f;
        fretFixture.restitution = 0.5f;

        fret = body.createFixture(fretFixture);
        fretShape.dispose();
    }

    private void update() {
        float x = position.x;
        float y = position.y;
        float r1 = radius;
        float r2 = radius + height + numHeight;
        float radians = degrees * MathUtils.degreesToRadians * size;

        int segments = Math.max(1, (int) (3 * (float) Math.cbrt(r2)));
        float radInc = radians / segments;

        float rot = rotation;

        float[] vertices = new float[segments * 4];
        short[] tris = new short[segments * 11];

        Vector2 fretPos = new Vector2(
                x + (r1 + height) * MathUtils.cos(rot),
                y + (r1 + height) * MathUtils.sin(rot)
        );
        body.setTransform(fretPos, rot);

        for (int i = 0; i < segments; i++) {
            int v = i * 4;
            int ind = i * 2;
            int t = i * 6;
            vertices[v] = x + r1 * MathUtils.cos(rot);
            vertices[v+1] = y + r1 * MathUtils.sin(rot);
            vertices[v+2] = x + r2 * MathUtils.cos(rot);
            vertices[v+3] = y + r2 * MathUtils.sin(rot);

            if (i < segments - 1) {
                tris[t] = (short) ind;
                tris[t + 1] = (short) (ind + 2);
                tris[t + 2] = (short) (ind + 3);
                tris[t + 3] = (short) ind;
                tris[t + 4] = (short) (ind + 3);
                tris[t + 5] = (short) (ind + 1);
            }

            rot += radInc;
        }
        region = new PolygonRegion(new TextureRegion(tex), vertices, tris);

        PolygonShape shape = (PolygonShape) fret.getShape();
        int vertexCount = shape.getVertexCount();

        Vector2 tmp = new Vector2();
        float[] fretVerts = new float[vertexCount * 2];

        for (int i = 0; i < vertexCount; i++) {
            shape.getVertex(i, tmp);
            tmp.rotateRad(body.getAngle()).add(body.getPosition());
            fretVerts[i * 2] = tmp.x;
            fretVerts[i * 2 + 1] = tmp.y;
        }
        short[] fretIndices = {
            0, 1, 2,
            0, 2, 3,
        };

        fretRegion = new PolygonRegion(new TextureRegion(fretTex), fretVerts, fretIndices);
    }

    public void render() {
        POLY_BATCH.begin();
        POLY_BATCH.draw(region, 0, 0);
        POLY_BATCH.draw(fretRegion, 0, 0);
        POLY_BATCH.end();
    }

    public float getSize() { return size; }

    public void setPosition(Vector2 position) {
        this.position = position;
        update();
    }
    public void setDegrees(float degrees) {
        this.degrees = degrees;
        update();
    }
    public void setRotation(float rotation) {
        this.rotation = rotation;
        update();
    }
    public void setRadius(float radius) {
        this.radius = radius;
        update();
    }
    public void setSize(float size) {
        this.size = size;
        update();
    }
}
