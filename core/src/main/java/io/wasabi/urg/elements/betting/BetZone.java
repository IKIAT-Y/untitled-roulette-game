package io.wasabi.urg.elements.betting;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.List;

import io.wasabi.urg.elements.game.Tile;

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

    public float distanceTo(Vector2 point) {
        return 0.0f;
    }
}