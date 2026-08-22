package io.wasabi.urg.elements.game;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.util.tweens.Tween;

public class Wheel {
    private static final Roulette GAME = Roulette.getInstance();
    private static final RunState RUN_STATE = GAME.getRunState();

    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final ShapeRenderer SHAPE_RENDERER = RENDERER_MANAGER.getShapeRenderer();
    private static final SpriteBatch SPRITE_BATCH = RENDERER_MANAGER.getSpriteBatch();

    private static final int[] WHEEL_NUMBER_ORDER = new int[] {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22,
            18, 29, 7, 28, 12, 35, 3, 26 };

    private final World world;
    private final Texture wheelBackground;

    private Vector2 position = new Vector2();
    private float rotation; // in Degrees
    private float radius;
    private float tileSize;
    private final SpinButton spinButton;
    private SpinButton.State spinButtonState = SpinButton.State.NO_BET;

    private final Body body;

    private boolean showConsumeZone = false;

    private Tween wheelVelocityTween;
    private Tween tweenY;

    private final List<Tile> tiles = RUN_STATE.getTiles();

    public Wheel(World world, Vector2 position) {
        this.world = world;
        this.position = position;
        this.wheelBackground = new Texture(Gdx.files.internal("ui/WheelBack.png"));
        this.spinButton = new SpinButton(position, 160f);

        // Testing
        radius = 200f;
        tileSize = 50;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KinematicBody;
        bodyDef.position.set(position);
        body = this.world.createBody(bodyDef);

        reset();

        addRing(radius, 5.0f, 0f, false);

        update();
    }

    public void reset() {
        for (Tile tile : tiles) {
            tile.dispose();
        }

        tiles.clear();

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
            type.setNumber(WHEEL_NUMBER_ORDER[i]);
            Tile tile = new Tile(world, type, position, radius, tileSize);
            tiles.add(tile);
        }
    }

    public void setPosition(Vector2 vec) {
        this.position.x = vec.x;
        this.position.y = vec.y;

        body.setTransform(position, 0);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setRotation(float rot) {
        this.rotation = rot;
    }

    public void rotateBy(float deltaDegrees) {
        if (isSpinning()) {
            return;
        }
        setRotation(rotation + deltaDegrees * 0.0174532925f);
    }

    public void setSize(float radius, float tileSize) {
        this.radius = radius;
        this.tileSize = tileSize;
    }

    public void setShowConsumeZone(boolean show) {
        this.showConsumeZone = show;
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

    public void updateSpinButton(SpinButton.State state, OrthographicCamera camera) {

        spinButtonState = state;

        spinButton.setPosition(position);
        spinButton.update(state, camera);
    }

    public void render(float delta) {
        // placeholder render function
        float r1 = radius;

        if (wheelVelocityTween != null) {
            body.setAngularVelocity(wheelVelocityTween.update(delta));
            setRotation(body.getAngle());
        }

        if (tweenY != null && !tweenY.isComplete()) {
            this.position.y = tweenY.update(delta);
        }

        // draw wheel background at the middle of the screen
        SPRITE_BATCH.begin();
        SPRITE_BATCH.draw(
                wheelBackground,
                position.x - wheelBackground.getWidth() / 2f,
                position.y - wheelBackground.getHeight() / 2f);
        SPRITE_BATCH.end();

        for (Tile tile : tiles) {
            tile.render();
        }

        update();

        SHAPE_RENDERER.begin(ShapeType.Line);
        SHAPE_RENDERER.setColor(1f, 1f, 1f, 1f);
        Gdx.gl.glLineWidth(2);
        SHAPE_RENDERER.circle(position.x, position.y, r1);
        SHAPE_RENDERER.end();

        spinButton.setPosition(position);

        spinButton.draw(spinButtonState, SHAPE_RENDERER, SPRITE_BATCH,
                FontManager.getInstance().getFontByName("Placeholder"));

        if (showConsumeZone) {
            SHAPE_RENDERER.begin(ShapeType.Filled);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            SHAPE_RENDERER.setColor(1f, 0.9f, 0.2f, 0.75f);
            SHAPE_RENDERER.circle(position.x, position.y, radius + tileSize * 2f);
            SHAPE_RENDERER.end();

            SPRITE_BATCH.begin();
            FontManager.getInstance().getFontByName("Terminus64PXBold")
                    .draw(SPRITE_BATCH, "Consume", position.x - 100f, position.y + 20f);
            SPRITE_BATCH.end();
        }
    }

    public void shiftOutOfScreen() {
        float targetY = -1500;
        tweenY = new Tween(1f, position.y, targetY, Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
    }

    public void shiftIntoScreen() {
        float targetY = 0;
        tweenY = new Tween(1f, position.y, targetY, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
    }

    public boolean containsPoint(Vector2 point) {
        return point.dst2(position) <= (radius + tileSize) * (radius + tileSize);
    }

    /**
     * Spins the wheel for a set amount of time, with given initial speed.
     * 
     * @param duration     The spin time
     * @param initialSpeed The initial speed
     */
    public void spin(float duration, float initialSpeed) {
        wheelVelocityTween = new Tween(duration, initialSpeed, 0, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
    }

    public void dispose() {
        wheelBackground.dispose();
        world.destroyBody(body);
    }

        pubic B

    public List<Tile> getTi
        es() { retur
         pub

    

           wheelVe locityTween = null;

        tweenY = null;
        
    

    }
        
    

    public void resetTileMultipliers() {
        for (Tile tile : tiles) {
     

        }

    
        
    


     

        float outerRadius = radius + tileSize * 2f; // matches

        }

        float pointAngle = normalizeAngle(MathUtils.atan2(local.y, local.x));

        float ang = getBaseTileAngle();
        float angc = rotation;

        for (Tile tile : tiles) {
            float sweep = ang * MathUtils.degreesToRadians * tile.getSize();
            float startAngle = normalizeAngle(angc);
            float endAngle = normalizeAngle(angc + sweep);

            if (isAngleInRange(pointAngle, startAngle, endAngle)) {
                return tile;
            }

            angc += sweep;
        }

        return null;
    }

    private float normalizeAngle(float angle) {
        float twoPi = MathUtils.PI2;
        angle %= twoPi;
        if (angle < 0) {
            angle += twoPi;
        }
        return angle;
    }

    private boolean isAngleInRange(float angle, float start, float end) {
        if (start <= end) {
            return angle >= start && angle <= end;
        }
        return angle >= start || angle <= end; // wraps past 0
    }
}

    

    

