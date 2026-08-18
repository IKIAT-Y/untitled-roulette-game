package io.wasabi.urg.elements.betting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.wasabi.urg.elements.game.Tile;

/**
 * Builds a {@link BettingTableLayout} (grid positions + every bet zone) from
 * whatever tiles
 * currently exist. Call {@link #generate} again any time the tile list changes
 * — the roguelike
 * layer can add/remove/renumber tiles freely and the whole table regenerates
 * from scratch.
 *
 * Pure data in, data out — no rendering calls in here, so this is unit-testable
 * without libGDX
 * needing a graphics context.
 *
 * <p>
 * <b>Note on Tile:</b> Tile currently only exposes {@link Tile#getNumber()} —
 * no colour or
 * zero/type flag. {@link #getColor} and {@link #isZeroTile} derive that here
 * rather than on
 * Tile itself, mirroring the parity logic Tile already bakes into its texture
 * (even → red,
 * odd → black) but correcting zero to GREEN for betting purposes. If Tile grows
 * a real
 * colour/type field later, these two methods are the only place that needs to
 * change.
 * </p>
 */
public class TableLayoutGenerator {

    public BettingTableLayout generate(List<Tile> tiles, float originX, float originY) {
        List<Tile> standard = new ArrayList<>();
        List<Tile> zeros = new ArrayList<>();
        for (Tile tile : tiles) {
            if (isZeroTile(tile)) {
                zeros.add(tile);
            } else {
                standard.add(tile);
            }
        }
        // Sort ascending by number so grid fill order matches a real table (1,2,3 /
        // 4,5,6 / ...).
        // Duplicate numbers (a roguelike tile duplicate) are stable-sorted by original
        // list order.
        standard.sort(Comparator.comparingInt(Tile::getNumber));

        int rows = TableLayoutConfig.ROWS;
        int columns = (int) Math.ceil(standard.size() / (float) rows);

        Map<Tile, GridPoint2> positions = assignGridPositions(standard, rows);

        List<BetZone> zones = new ArrayList<>();
        zones.addAll(buildStraightZones(positions, originX, originY, rows));
        zones.addAll(buildHorizontalSplitZones(positions, columns, rows, originX, originY));
        zones.addAll(buildVerticalSplitZones(positions, columns, rows, originX, originY));
        zones.addAll(buildStreetZones(positions, columns, rows, originX, originY));
        zones.addAll(buildCornerZones(positions, columns, rows, originX, originY));
        zones.addAll(buildSixLineZones(positions, columns, rows, originX, originY));
        zones.addAll(buildColumnZones(positions, columns, rows, originX, originY));
        zones.addAll(buildDozenZones(standard, columns, rows, originX, originY));
        zones.addAll(buildOutsideCategoryZones(standard, columns, rows, originX, originY));
        zones.addAll(buildZeroZones(zeros, columns, rows, originX, originY));

        Rectangle bounds = new Rectangle(
                originX,
                originY - TableLayoutConfig.OUTSIDE_BOX_GAP - TableLayoutConfig.OUTSIDE_BOX_HEIGHT,
                TableLayoutConfig.ZERO_COLUMN_WIDTH + columns * TableLayoutConfig.CELL_WIDTH
                        + TableLayoutConfig.COLUMN_STRIP_WIDTH,
                rows * TableLayoutConfig.CELL_HEIGHT + TableLayoutConfig.DOZEN_STRIP_HEIGHT
                        + TableLayoutConfig.OUTSIDE_BOX_HEIGHT + TableLayoutConfig.OUTSIDE_BOX_GAP);

        return new BettingTableLayout(positions, zeros, zones, bounds, columns, rows, originX, originY);
    }

    // ---------------------------------------------------------------------------------------
    // Tile classification (see class javadoc — this is the seam to update if Tile
    // changes)
    // ---------------------------------------------------------------------------------------

    public boolean isZeroTile(Tile tile) {
        return tile.getNumber() == 0;
    }

    public PocketColor getColor(Tile tile) {
        if (isZeroTile(tile)) {
            return PocketColor.GREEN;
        }
        return tile.getNumber() % 2 == 0 ? PocketColor.RED : PocketColor.BLACK;
    }

    // ---------------------------------------------------------------------------------------
    // Grid geometry helpers
    // ---------------------------------------------------------------------------------------

    private Map<Tile, GridPoint2> assignGridPositions(List<Tile> standard, int rows) {
        Map<Tile, GridPoint2> positions = new HashMap<>();
        for (int i = 0; i < standard.size(); i++) {
            // Each run of `rows` consecutive numbers fills one column bottom-up (1 at the
            // bottom, matching a real table), then moves to the next column to the right.
            int col = i / rows;
            int row = rows - 1 - (i % rows);
            positions.put(standard.get(i), new GridPoint2(col, row));
        }
        return positions;
    }

    private float cellX(int col, float originX) {
        return TableGeometry.cellX(col, originX);
    }

    private float cellY(int row, int rows, float originY) {
        return TableGeometry.cellY(row, rows, originY);
    }

    private Tile tileAt(Map<Tile, GridPoint2> positions, int col, int row) {
        for (Map.Entry<Tile, GridPoint2> entry : positions.entrySet()) {
            GridPoint2 p = entry.getValue();
            if (p.x == col && p.y == row) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Polygon rectPolygon(float x, float y, float w, float h) {
        return new Polygon(new float[] { x, y, x + w, y, x + w, y + h, x, y + h });
    }

    // ---------------------------------------------------------------------------------------
    // Inside bets
    // ---------------------------------------------------------------------------------------

    private List<BetZone> buildStraightZones(Map<Tile, GridPoint2> positions,
            float originX, float originY, int rows) {
        List<BetZone> zones = new ArrayList<>();
        for (Map.Entry<Tile, GridPoint2> entry : positions.entrySet()) {
            Tile tile = entry.getKey();
            GridPoint2 p = entry.getValue();
            float x = cellX(p.x, originX);
            float y = cellY(p.y, rows, originY);
            Vector2 anchor = new Vector2(x + TableLayoutConfig.CELL_WIDTH / 2f,
                    y + TableLayoutConfig.CELL_HEIGHT / 2f);
            zones.add(new BetZone(BetType.STRAIGHT, Collections.singletonList(tile),
                    rectPolygon(x, y, TableLayoutConfig.CELL_WIDTH, TableLayoutConfig.CELL_HEIGHT),
                    anchor));
        }
        return zones;
    }

    private List<BetZone> buildHorizontalSplitZones(Map<Tile, GridPoint2> positions, int columns,
            int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float w = TableLayoutConfig.SPLIT_HIT_WIDTH;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns - 1; col++) {
                Tile a = tileAt(positions, col, row);
                Tile b = tileAt(positions, col + 1, row);
                if (a == null || b == null)
                    continue;

                float borderX = cellX(col + 1, originX);
                float y = cellY(row, rows, originY);
                Vector2 anchor = new Vector2(borderX, y + TableLayoutConfig.CELL_HEIGHT / 2f);
                zones.add(new BetZone(BetType.SPLIT, java.util.Arrays.asList(a, b),
                        rectPolygon(borderX - w / 2f, y, w, TableLayoutConfig.CELL_HEIGHT), anchor));
            }
        }
        return zones;
    }

    private List<BetZone> buildVerticalSplitZones(Map<Tile, GridPoint2> positions, int columns,
            int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float h = TableLayoutConfig.SPLIT_HIT_WIDTH;
        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows - 1; row++) {
                Tile a = tileAt(positions, col, row);
                Tile b = tileAt(positions, col, row + 1);
                if (a == null || b == null)
                    continue;

                float x = cellX(col, originX);
                float borderY = cellY(row, rows, originY); // bottom of `row` == top of `row+1`
                Vector2 anchor = new Vector2(x + TableLayoutConfig.CELL_WIDTH / 2f, borderY);
                zones.add(new BetZone(BetType.SPLIT, java.util.Arrays.asList(a, b),
                        rectPolygon(x, borderY - h / 2f, TableLayoutConfig.CELL_WIDTH, h), anchor));
            }
        }
        return zones;
    }

    private List<BetZone> buildStreetZones(Map<Tile, GridPoint2> positions, int columns, int rows,
            float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float stripH = TableLayoutConfig.STREET_STRIP_HEIGHT;
        float y = originY - stripH;
        for (int col = 0; col < columns; col++) {
            List<Tile> colTiles = new ArrayList<>();
            for (int row = 0; row < rows; row++) {
                Tile t = tileAt(positions, col, row);
                if (t == null)
                    break;
                colTiles.add(t);
            }
            if (colTiles.size() != rows)
                continue; // ragged/incomplete column — no street bet

            float x = cellX(col, originX);
            float width = TableLayoutConfig.CELL_WIDTH;
            Vector2 anchor = new Vector2(x + width / 2f, y + stripH / 2f);
            zones.add(new BetZone(BetType.STREET, colTiles, rectPolygon(x, y, width, stripH), anchor));
        }
        return zones;
    }

    private List<BetZone> buildCornerZones(Map<Tile, GridPoint2> positions, int columns, int rows,
            float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float size = TableLayoutConfig.CORNER_HIT_SIZE;
        for (int row = 0; row < rows - 1; row++) {
            for (int col = 0; col < columns - 1; col++) {
                Tile a = tileAt(positions, col, row);
                Tile b = tileAt(positions, col + 1, row);
                Tile c = tileAt(positions, col, row + 1);
                Tile d = tileAt(positions, col + 1, row + 1);
                if (a == null || b == null || c == null || d == null)
                    continue;

                float crossX = cellX(col + 1, originX);
                float crossY = cellY(row, rows, originY);
                Vector2 anchor = new Vector2(crossX, crossY);
                zones.add(new BetZone(BetType.CORNER, java.util.Arrays.asList(a, b, c, d),
                        rectPolygon(crossX - size / 2f, crossY - size / 2f, size, size), anchor));
            }
        }
        return zones;
    }

    private List<BetZone> buildSixLineZones(Map<Tile, GridPoint2> positions, int columns, int rows,
            float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float w = TableLayoutConfig.SIX_LINE_HIT_WIDTH;
        float stripH = TableLayoutConfig.STREET_STRIP_HEIGHT;
        float y = originY - stripH;
        for (int col = 0; col < columns - 1; col++) {
            List<Tile> combined = new ArrayList<>();
            boolean complete = true;
            for (int c = col; c <= col + 1 && complete; c++) {
                for (int row = 0; row < rows; row++) {
                    Tile t = tileAt(positions, c, row);
                    if (t == null) {
                        complete = false;
                        break;
                    }
                    combined.add(t);
                }
            }
            if (!complete)
                continue;

            // Sits in the same bottom-edge band as the street strips, straddling the
            // border between the two columns it covers.
            float borderX = cellX(col + 1, originX);
            Vector2 anchor = new Vector2(borderX, y + stripH / 2f);
            zones.add(new BetZone(BetType.SIX_LINE, combined,
                    rectPolygon(borderX - w / 2f, y, w, stripH), anchor));
        }
        return zones;
    }

    // ---------------------------------------------------------------------------------------
    // Outside bets
    // ---------------------------------------------------------------------------------------

    private List<BetZone> buildColumnZones(Map<Tile, GridPoint2> positions, int columns, int rows,
            float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float stripX = cellX(columns, originX); // right edge of the grid
        float stripW = TableLayoutConfig.COLUMN_STRIP_WIDTH;
        for (int row = 0; row < rows; row++) {
            List<Tile> rowTiles = new ArrayList<>();
            for (Map.Entry<Tile, GridPoint2> entry : positions.entrySet()) {
                if (entry.getValue().y == row)
                    rowTiles.add(entry.getKey());
            }
            if (rowTiles.isEmpty())
                continue;

            float y = cellY(row, rows, originY);
            Vector2 anchor = new Vector2(stripX + stripW / 2f, y + TableLayoutConfig.CELL_HEIGHT / 2f);
            zones.add(new BetZone(BetType.COLUMN, rowTiles,
                    rectPolygon(stripX, y, stripW, TableLayoutConfig.CELL_HEIGHT), anchor));
        }
        return zones;
    }

    private List<BetZone> buildDozenZones(List<Tile> standardSorted, int columns, int rows,
            float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        int groupSize = TableLayoutConfig.DOZEN_GROUP_SIZE;
        float stripY = originY + rows * TableLayoutConfig.CELL_HEIGHT; // top edge of the grid
        float stripH = TableLayoutConfig.DOZEN_STRIP_HEIGHT;

        // Fixed-size blocks of 12, like a real table. A trailing partial block (pocket
        // count not
        // a multiple of 12) intentionally gets no dozen zone — see architecture notes
        // on scaling.
        int fullBlocks = standardSorted.size() / groupSize;
        int columnsPerBlock = groupSize / rows; // assumes ROWS divides GROUP_SIZE evenly

        for (int block = 0; block < fullBlocks; block++) {
            int fromCol = block * columnsPerBlock;
            int toCol = fromCol + columnsPerBlock - 1;
            List<Tile> blockTiles = standardSorted.subList(block * groupSize, (block + 1) * groupSize);

            float leftX = cellX(fromCol, originX);
            float rightX = cellX(toCol, originX) + TableLayoutConfig.CELL_WIDTH;
            float width = rightX - leftX;
            Vector2 anchor = new Vector2(leftX + width / 2f, stripY + stripH / 2f);
            zones.add(new BetZone(BetType.DOZEN, new ArrayList<>(blockTiles),
                    rectPolygon(leftX, stripY, width, stripH), anchor));
        }
        return zones;
    }

    private List<BetZone> buildOutsideCategoryZones(List<Tile> standard, int columns, int rows,
            float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();

        List<Tile> red = new ArrayList<>();
        List<Tile> black = new ArrayList<>();
        List<Tile> odd = new ArrayList<>();
        List<Tile> even = new ArrayList<>();
        for (Tile t : standard) {
            if (getColor(t) == PocketColor.RED)
                red.add(t);
            if (getColor(t) == PocketColor.BLACK)
                black.add(t);
            if (t.getNumber() % 2 != 0)
                odd.add(t);
            else
                even.add(t);
        }

        // "High/low" generalised to rank-based halves rather than the hardcoded
        // 1-18/19-36
        // range, so this still works when the roguelike layer changes what numbers
        // exist.
        List<Tile> byNumber = new ArrayList<>(standard);
        byNumber.sort(Comparator.comparingInt(Tile::getNumber));
        int half = byNumber.size() / 2;
        List<Tile> low = new ArrayList<>(byNumber.subList(0, half));
        List<Tile> high = new ArrayList<>(byNumber.subList(half, byNumber.size()));

        float y = originY - TableLayoutConfig.OUTSIDE_BOX_GAP - TableLayoutConfig.OUTSIDE_BOX_HEIGHT;
        float x = originX;
        float boxW = TableLayoutConfig.OUTSIDE_BOX_WIDTH;
        float boxH = TableLayoutConfig.OUTSIDE_BOX_HEIGHT;
        float gap = TableLayoutConfig.OUTSIDE_BOX_GAP;

        Object[][] categories = {
                { BetType.LOW, low }, { BetType.EVEN, even }, { BetType.RED, red },
                { BetType.BLACK, black }, { BetType.ODD, odd }, { BetType.HIGH, high }
        };

        for (int i = 0; i < categories.length; i++) {
            BetType type = (BetType) categories[i][0];
            @SuppressWarnings("unchecked")
            List<Tile> covered = (List<Tile>) categories[i][1];
            if (covered.isEmpty())
                continue;

            float boxX = x + i * (boxW + gap);
            Vector2 anchor = new Vector2(boxX + boxW / 2f, y + boxH / 2f);
            zones.add(new BetZone(type, covered, rectPolygon(boxX, y, boxW, boxH), anchor));
        }
        return zones;
    }

    private List<BetZone> buildZeroZones(List<Tile> zeros, int columns, int rows,
            float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        if (zeros.isEmpty())
            return zones;

        float slotHeight = (rows * TableLayoutConfig.CELL_HEIGHT) / zeros.size();
        for (int i = 0; i < zeros.size(); i++) {
            Tile tile = zeros.get(i);
            float x = originX;
            float y = originY + rows * TableLayoutConfig.CELL_HEIGHT - (i + 1) * slotHeight;
            Vector2 anchor = new Vector2(x + TableLayoutConfig.ZERO_COLUMN_WIDTH / 2f, y + slotHeight / 2f);
            zones.add(new BetZone(BetType.STRAIGHT, Collections.singletonList(tile),
                    rectPolygon(x, y, TableLayoutConfig.ZERO_COLUMN_WIDTH, slotHeight), anchor));
        }
        return zones;
    }
}
