package io.wasabi.urg.elements.card;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.managers.RendererManager;

public class Ouroboros extends Card {
    private static final Color ARROW_COLOR = new Color(0.1f, 0.9f, 1f, 1f);
    private static final String TOOLTIP_TYPE = "OUROBOROS 3X";
    private Tile boostedTile;

    public Ouroboros() {
        super(Rarity.UNCOMMON);
        tooltip.setTitle("Ouroboros");
        tooltip.setDescription("The tile landed on gives [RED]3x [BLACK]payout on the next spin");
    }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        return winningTile == boostedTile ? 3f : 1f;
    }

    @Override
    public void afterSpinEffect() {
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
    public void drawTileOutline(Tile tile) {
        if (tile != boostedTile) {
            return;
        }

        PolygonRegion region = tile.getRegion();
        if (region == null) {
            return;
        }

        float[] vertices = region.getVertices();
        int lastOuter = vertices.length - 2;
        Vector2 outerMiddle = new Vector2(
                (vertices[2] + vertices[lastOuter]) / 2f,
                (vertices[3] + vertices[lastOuter + 1]) / 2f);
        Vector2 outward = new Vector2(outerMiddle).sub(tile.getPosition()).nor();
        Vector2 tip = new Vector2(outerMiddle).mulAdd(outward, 3f);
        Vector2 tail = new Vector2(tip).mulAdd(outward, 34f);
        Vector2 arrowBase = new Vector2(tip).mulAdd(outward, 12f);
        Vector2 perpendicular = new Vector2(-outward.y, outward.x).scl(7f);

        ShapeRenderer renderer = RendererManager.getInstance().getShapeRenderer();
        Gdx.gl.glLineWidth(7f);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(ARROW_COLOR);
        renderer.line(tail, tip);
        renderer.line(tip, new Vector2(arrowBase).add(perpendicular));
        renderer.line(tip, new Vector2(arrowBase).sub(perpendicular));
        renderer.end();
        Gdx.gl.glLineWidth(1f);
    }

    private void clearBoostedTile() {
        if (boostedTile != null) {
            boostedTile.getTooltip().removeType(TOOLTIP_TYPE);
            boostedTile = null;
        }
    }
}
