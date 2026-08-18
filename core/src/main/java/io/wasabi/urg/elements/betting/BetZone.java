package io.wasabi.urg.elements.betting;

import java.util.List;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

import io.wasabi.urg.elements.game.Tile;

/**
 * A single snap-target on the betting table: a hit region tied to the set of
 * tiles it covers.
 * Every bet type (straight, split, corner, colour, dozen, ...) is represented
 * uniformly as a
 * BetZone so the drag controller never needs to special-case bet types — it
 * just hit-tests a
 * flat list of these.
 */
public class BetZone {
    private final BetType type;
    private final List<Tile> coveredTiles;
    private final Polygon hitArea; // supports non-rectangular corner/split regions
    private final Vector2 chipAnchor; // where a chip visually snaps/stacks

    public BetZone(BetType type, List<Tile> coveredTiles, Polygon hitArea, Vector2 chipAnchor) {
        this.type = type;
        this.coveredTiles = coveredTiles;
        this.hitArea = hitArea;
        this.chipAnchor = chipAnchor;
    }

    public boolean contains(Vector2 point) {
        return hitArea.contains(point.x, point.y);
    }

    /**
     * Distance from a point to this zone's chip anchor — used for "nearest zone"
     * snapping.
     */
    public float distanceTo(Vector2 point) {
        return chipAnchor.dst(point);
    }

    public BetType getType() {
        return type;
    }

    public List<Tile> getCoveredTiles() {
        return coveredTiles;
    }

    public Polygon getHitArea() {
        return hitArea;
    }

    public Vector2 getChipAnchor() {
        return chipAnchor;
    }
}
