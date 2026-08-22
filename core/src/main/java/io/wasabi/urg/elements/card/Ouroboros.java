package io.wasabi.urg.elements.card;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.managers.RendererManager;

public class Ouroboros extends Card {
    private static final Color ARROW_COLOR = new Color(0.1f, 0.9f, 1f, 1f);
    private static final String TOOLTIP_TYPE = "OUROBOROS 3X";
    private static final float ARROW_WIDTH = 7f;
    private final Texture lineTexture;
    private Tile boostedTile;

    public Ouroboros() {
        super(Rarity.UNCOMMON);
        tooltip.setTitle("Ouroboros");
        tooltip.setDescription("The tile landed on gives [RED]3x [BLACK]payout on the next spin");

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        lineTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        return winningTile == boostedTile ? 3f : 1f;
    }

    @Override
    public void afterSpinEffect() {
        triggerDisplay();
        clearBoostedTile();
        boostedTile = Roulette.getInstance().getRunState().getLastTile();
        if (boostedTile != null) {
            boostedTile.getTooltip().addType(TOOLTIP_TYPE, Color.WHITE, ARROW_COLOR);
        }
    }

    @Override
    public void roundEndEffect() {
        clearBoostedTile();
    }

    @Override
    public void removedEffect() {
        clearBoostedTile();
    }

    @Override
    public void render() {
        super.render();
        if (boostedTile == null) {
            return;
        }

        PolygonRegion region = boostedTile.getRegion();
        if (region == null) {
            return;
        }

        float[] vertices = region.getVertices();
        int lastOuter = vertices.length - 2;
        Vector2 outerMiddle = new Vector2(
                (vertices[2] + vertices[lastOuter]) / 2f,
                (vertices[3] + vertices[lastOuter + 1]) / 2f);
        Vector2 outward = new Vector2(outerMiddle).sub(boostedTile.getPosition()).nor();
        Vector2 tip = new Vector2(outerMiddle).mulAdd(outward, 3f);
        Vector2 tail = new Vector2(tip).mulAdd(outward, 34f);
        Vector2 arrowBase = new Vector2(tip).mulAdd(outward, 12f);
        Vector2 perpendicular = new Vector2(-outward.y, outward.x).scl(7f);

        SpriteBatch batch = RendererManager.getInstance().getSpriteBatch();
        batch.setColor(ARROW_COLOR);
        drawLine(batch, tail, tip);
        drawLine(batch, tip, new Vector2(arrowBase).add(perpendicular));
        drawLine(batch, tip, new Vector2(arrowBase).sub(perpendicular));
        batch.setColor(Color.WHITE);
    }

    private void clearBoostedTile() {
        if (boostedTile != null) {
            boostedTile.getTooltip().removeType(TOOLTIP_TYPE);
            boostedTile = null;
        }
    }

    private void drawLine(SpriteBatch batch, Vector2 start, Vector2 end) {
        float length = start.dst(end);
        float rotation = MathUtils.atan2(end.y - start.y, end.x - start.x)
                * MathUtils.radiansToDegrees;
        batch.draw(lineTexture, start.x, start.y - ARROW_WIDTH / 2f,
                0f, ARROW_WIDTH / 2f, length, ARROW_WIDTH,
                1f, 1f, rotation, 0, 0, 1, 1, false, false);
    }
}
