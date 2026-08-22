package io.wasabi.urg.elements.betting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;

import io.wasabi.urg.elements.game.Tile;

/**
 * The output of {@link TableLayoutGenerator#generate}. Holds every grid
 * position and bet
 * zone for the CURRENT set of tiles. Rebuilt from scratch whenever the tile
 * list changes —
 * treat instances as disposable snapshots, not something to mutate in place.
 */
public class BettingTableLayout {
    private final Map<Tile, GridPoint2> gridPositions;
    private final List<Tile> zeroTiles;
    private final List<BetZone> betZones;
    private final Rectangle tableBounds;
    private final int columns;
    private final int rows;
    private final float originX;
    private final float originY;

    public BettingTableLayout(Map<Tile, GridPoint2> gridPositions, List<Tile> zeroTiles,
            List<BetZone> betZones, Rectangle tableBounds,
            int columns, int rows, float originX, float originY) {
        this.gridPositions = gridPositions;
        this.zeroTiles = zeroTiles;
        this.betZones = betZones;
        this.tableBounds = tableBounds;
        this.columns = columns;
        this.rows = rows;
        this.originX = originX;
        this.originY = originY;
    }

    /**
     * Re-derives a tile's cell rectangle for rendering, using the same math the
     * generator
     * used to place its zones — see {@link TableGeometry}. Returns null for
     * zero-type tiles,
     * which don't live in the main grid (their bounds are only encoded in their
     * BetZone).
     */
    public Rectangle getTileBounds(Tile tile) {
        GridPoint2 p = gridPositions.get(tile);
        if (p == null)
            return null;
        return TableGeometry.cellRect(p.x, p.y, rows, originX, originY);
    }

    public GridPoint2 getGridPosition(Tile tile) {
        return gridPositions.get(tile);
    }

    public Map<Tile, GridPoint2> getGridPositions() {
        return gridPositions;
    }

    public List<Tile> getZeroTiles() {
        return zeroTiles;
    }

    public List<BetZone> getBetZones() {
        return betZones;
    }

    public List<BetZone> getZonesOfType(BetType type) {
        List<BetZone> result = new ArrayList<>();
        for (BetZone zone : betZones) {
            if (zone.getType() == type) {
                result.add(zone);
            }
        }
        return result;
    }

    public Rectangle getTableBounds() {
        return tableBounds;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }
}
