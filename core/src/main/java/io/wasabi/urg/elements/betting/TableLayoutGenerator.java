package io.wasabi.urg.elements.betting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
 * <b>Duplicate numbers:</b> the roguelike layer can add a second physical tile
 * with a number that's already on the wheel (e.g. to bias probability toward
 * it). That must not create a second betting pocket — every zone here is built
 * per distinct NUMBER, not per Tile, so duplicates share one grid cell/zone
 * whose {@link BetZone#getCoveredTiles()} includes every physical tile with
 * that number. A bet on it wins if the ball lands on ANY of them, and payouts
 * are unaffected (still one bet, one stake, the normal multiplier).
 * </p>
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
        standard.sort(Comparator.comparingInt(Tile::getNumber));

        // Group by number so duplicate tiles (see class javadoc) collapse onto one
        // grid cell. groupByNumber uses a LinkedHashMap, so key order follows the
        // ascending sort above.
        Map<Integer, List<Tile>> tilesByNumber = groupByNumber(standard);
        List<Integer> uniqueNumbers = new ArrayList<>(tilesByNumber.keySet());

        int rows = TableLayoutConfig.ROWS;
        int columns = (int) Math.ceil(uniqueNumbers.size() / (float) rows);

        Map<Integer, GridPoint2> numberPositions = assignGridPositions(uniqueNumbers, rows);

        List<BetZone> zones = new ArrayList<>();
        zones.addAll(buildStraightZones(numberPositions, tilesByNumber, originX, originY, rows));
        zones.addAll(buildHorizontalSplitZones(numberPositions, tilesByNumber, columns, rows, originX, originY));
        zones.addAll(buildVerticalSplitZones(numberPositions, tilesByNumber, columns, rows, originX, originY));
        zones.addAll(buildStreetZones(numberPositions, tilesByNumber, columns, rows, originX, originY));
        zones.addAll(buildCornerZones(numberPositions, tilesByNumber, columns, rows, originX, originY));
        zones.addAll(buildSixLineZones(numberPositions, tilesByNumber, columns, rows, originX, originY));
        zones.addAll(buildColumnZones(numberPositions, tilesByNumber, columns, rows, originX, originY));
        zones.addAll(buildDozenZones(uniqueNumbers, tilesByNumber, columns, rows, originX, originY));
        zones.addAll(buildOutsideCategoryZones(standard, uniqueNumbers, tilesByNumber, originX, originY));
        zones.addAll(buildZeroZones(zeros, rows, originX, originY));

        Rectangle bounds = new Rectangle(
                originX,
                originY - TableLayoutConfig.OUTSIDE_BOX_GAP - TableLayoutConfig.OUTSIDE_BOX_HEIGHT,
                TableLayoutConfig.ZERO_COLUMN_WIDTH + columns * TableLayoutConfig.CELL_WIDTH
                        + TableLayoutConfig.COLUMN_STRIP_WIDTH,
                rows * TableLayoutConfig.CELL_HEIGHT + TableLayoutConfig.DOZEN_STRIP_HEIGHT
                        + TableLayoutConfig.OUTSIDE_BOX_HEIGHT + TableLayoutConfig.OUTSIDE_BOX_GAP);

        // The public API still keys grid positions by Tile (see BettingTableLayout) —
        // every physical tile shares its number's one cell with any duplicates.
        Map<Tile, GridPoint2> tilePositions = new HashMap<>();
        for (Tile tile : standard) {
            tilePositions.put(tile, numberPositions.get(tile.getNumber()));
        }

        return new BettingTableLayout(tilePositions, zeros, zones, bounds, columns, rows, originX, originY);
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

    /** Groups tiles by number, preserving each number's first-seen order. */
    private Map<Integer, List<Tile>> groupByNumber(List<Tile> tiles) {
        Map<Integer, List<Tile>> byNumber = new LinkedHashMap<>();
        for (Tile tile : tiles) {
            byNumber.computeIfAbsent(tile.getNumber(), n -> new ArrayList<>()).add(tile);
        }
        return byNumber;
    }

    /** Every physical tile across the given numbers — the covered-tiles list for a zone. */
    private List<Tile> tilesFor(Map<Integer, List<Tile>> tilesByNumber, List<Integer> numbers) {
        List<Tile> combined = new ArrayList<>();
        for (Integer number : numbers) {
            combined.addAll(tilesByNumber.get(number));
        }
        return combined;
    }

    private Map<Integer, GridPoint2> assignGridPositions(List<Integer> uniqueNumbers, int rows) {
        Map<Integer, GridPoint2> positions = new HashMap<>();
        for (int i = 0; i < uniqueNumbers.size(); i++) {
            // Each run of `rows` consecutive numbers fills one column bottom-up (1 at the
            // bottom, matching a real table), then moves to the next column to the right.
            int col = i / rows;
            int row = rows - 1 - (i % rows);
            positions.put(uniqueNumbers.get(i), new GridPoint2(col, row));
        }
        return positions;
    }

    private float cellX(int col, float originX) {
        return TableGeometry.cellX(col, originX);
    }

    private float cellY(int row, int rows, float originY) {
        return TableGeometry.cellY(row, rows, originY);
    }

    private Integer numberAt(Map<Integer, GridPoint2> numberPositions, int col, int row) {
        for (Map.Entry<Integer, GridPoint2> entry : numberPositions.entrySet()) {
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

    private List<BetZone> buildStraightZones(Map<Integer, GridPoint2> numberPositions,
            Map<Integer, List<Tile>> tilesByNumber, float originX, float originY, int rows) {
        List<BetZone> zones = new ArrayList<>();
        for (Map.Entry<Integer, GridPoint2> entry : numberPositions.entrySet()) {
            GridPoint2 p = entry.getValue();
            float x = cellX(p.x, originX);
            float y = cellY(p.y, rows, originY);
            Vector2 anchor = new Vector2(x + TableLayoutConfig.CELL_WIDTH / 2f,
                    y + TableLayoutConfig.CELL_HEIGHT / 2f);
            zones.add(new BetZone(BetType.STRAIGHT, tilesFor(tilesByNumber, Collections.singletonList(entry.getKey())),
                    rectPolygon(x, y, TableLayoutConfig.CELL_WIDTH, TableLayoutConfig.CELL_HEIGHT),
                    anchor));
        }
        return zones;
    }

    private List<BetZone> buildHorizontalSplitZones(Map<Integer, GridPoint2> numberPositions,
            Map<Integer, List<Tile>> tilesByNumber, int columns, int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float w = TableLayoutConfig.SPLIT_HIT_WIDTH;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns - 1; col++) {
                Integer a = numberAt(numberPositions, col, row);
                Integer b = numberAt(numberPositions, col + 1, row);
                if (a == null || b == null)
                    continue;

                float borderX = cellX(col + 1, originX);
                float y = cellY(row, rows, originY);
                Vector2 anchor = new Vector2(borderX, y + TableLayoutConfig.CELL_HEIGHT / 2f);
                zones.add(new BetZone(BetType.SPLIT, tilesFor(tilesByNumber, Arrays.asList(a, b)),
                        rectPolygon(borderX - w / 2f, y, w, TableLayoutConfig.CELL_HEIGHT), anchor));
            }
        }
        return zones;
    }

    private List<BetZone> buildVerticalSplitZones(Map<Integer, GridPoint2> numberPositions,
            Map<Integer, List<Tile>> tilesByNumber, int columns, int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float h = TableLayoutConfig.SPLIT_HIT_WIDTH;
        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows - 1; row++) {
                Integer a = numberAt(numberPositions, col, row);
                Integer b = numberAt(numberPositions, col, row + 1);
                if (a == null || b == null)
                    continue;

                float x = cellX(col, originX);
                float borderY = cellY(row, rows, originY); // bottom of `row` == top of `row+1`
                Vector2 anchor = new Vector2(x + TableLayoutConfig.CELL_WIDTH / 2f, borderY);
                zones.add(new BetZone(BetType.SPLIT, tilesFor(tilesByNumber, Arrays.asList(a, b)),
                        rectPolygon(x, borderY - h / 2f, TableLayoutConfig.CELL_WIDTH, h), anchor));
            }
        }
        return zones;
    }

    private List<BetZone> buildStreetZones(Map<Integer, GridPoint2> numberPositions,
            Map<Integer, List<Tile>> tilesByNumber, int columns, int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float stripH = TableLayoutConfig.STREET_STRIP_HEIGHT;
        float y = originY - stripH;
        for (int col = 0; col < columns; col++) {
            List<Integer> colNumbers = new ArrayList<>();
            for (int row = 0; row < rows; row++) {
                Integer n = numberAt(numberPositions, col, row);
                if (n == null)
                    break;
                colNumbers.add(n);
            }
            if (colNumbers.size() != rows)
                continue; // ragged/incomplete column — no street bet

            float x = cellX(col, originX);
            float width = TableLayoutConfig.CELL_WIDTH;
            Vector2 anchor = new Vector2(x + width / 2f, y + stripH / 2f);
            zones.add(new BetZone(BetType.STREET, tilesFor(tilesByNumber, colNumbers),
                    rectPolygon(x, y, width, stripH), anchor));
        }
        return zones;
    }

    private List<BetZone> buildCornerZones(Map<Integer, GridPoint2> numberPositions,
            Map<Integer, List<Tile>> tilesByNumber, int columns, int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float size = TableLayoutConfig.CORNER_HIT_SIZE;
        for (int row = 0; row < rows - 1; row++) {
            for (int col = 0; col < columns - 1; col++) {
                Integer a = numberAt(numberPositions, col, row);
                Integer b = numberAt(numberPositions, col + 1, row);
                Integer c = numberAt(numberPositions, col, row + 1);
                Integer d = numberAt(numberPositions, col + 1, row + 1);
                if (a == null || b == null || c == null || d == null)
                    continue;

                float crossX = cellX(col + 1, originX);
                float crossY = cellY(row, rows, originY);
                Vector2 anchor = new Vector2(crossX, crossY);
                zones.add(new BetZone(BetType.CORNER, tilesFor(tilesByNumber, Arrays.asList(a, b, c, d)),
                        rectPolygon(crossX - size / 2f, crossY - size / 2f, size, size), anchor));
            }
        }
        return zones;
    }

    private List<BetZone> buildSixLineZones(Map<Integer, GridPoint2> numberPositions,
            Map<Integer, List<Tile>> tilesByNumber, int columns, int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float w = TableLayoutConfig.SIX_LINE_HIT_WIDTH;
        float stripH = TableLayoutConfig.STREET_STRIP_HEIGHT;
        float y = originY - stripH;
        for (int col = 0; col < columns - 1; col++) {
            List<Integer> combined = new ArrayList<>();
            boolean complete = true;
            for (int c = col; c <= col + 1 && complete; c++) {
                for (int row = 0; row < rows; row++) {
                    Integer n = numberAt(numberPositions, c, row);
                    if (n == null) {
                        complete = false;
                        break;
                    }
                    combined.add(n);
                }
            }
            if (!complete)
                continue;

            // Sits in the same bottom-edge band as the street strips, straddling the
            // border between the two columns it covers.
            float borderX = cellX(col + 1, originX);
            Vector2 anchor = new Vector2(borderX, y + stripH / 2f);
            zones.add(new BetZone(BetType.SIX_LINE, tilesFor(tilesByNumber, combined),
                    rectPolygon(borderX - w / 2f, y, w, stripH), anchor));
        }
        return zones;
    }

    // ---------------------------------------------------------------------------------------
    // Outside bets
    // ---------------------------------------------------------------------------------------

    private List<BetZone> buildColumnZones(Map<Integer, GridPoint2> numberPositions,
            Map<Integer, List<Tile>> tilesByNumber, int columns, int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        float stripX = cellX(columns, originX); // right edge of the grid
        float stripW = TableLayoutConfig.COLUMN_STRIP_WIDTH;
        for (int row = 0; row < rows; row++) {
            List<Integer> rowNumbers = new ArrayList<>();
            for (Map.Entry<Integer, GridPoint2> entry : numberPositions.entrySet()) {
                if (entry.getValue().y == row)
                    rowNumbers.add(entry.getKey());
            }
            if (rowNumbers.isEmpty())
                continue;

            float y = cellY(row, rows, originY);
            Vector2 anchor = new Vector2(stripX + stripW / 2f, y + TableLayoutConfig.CELL_HEIGHT / 2f);
            zones.add(new BetZone(BetType.COLUMN, tilesFor(tilesByNumber, rowNumbers),
                    rectPolygon(stripX, y, stripW, TableLayoutConfig.CELL_HEIGHT), anchor));
        }
        return zones;
    }

    private List<BetZone> buildDozenZones(List<Integer> uniqueNumbers, Map<Integer, List<Tile>> tilesByNumber,
            int columns, int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        int groupSize = TableLayoutConfig.DOZEN_GROUP_SIZE;
        float stripY = originY + rows * TableLayoutConfig.CELL_HEIGHT; // top edge of the grid
        float stripH = TableLayoutConfig.DOZEN_STRIP_HEIGHT;

        // Fixed-size blocks of 12 distinct numbers, like a real table. A trailing
        // partial block (unique number count not a multiple of 12) intentionally gets
        // no dozen zone — see architecture notes on scaling.
        int fullBlocks = uniqueNumbers.size() / groupSize;
        int columnsPerBlock = groupSize / rows; // assumes ROWS divides GROUP_SIZE evenly

        for (int block = 0; block < fullBlocks; block++) {
            int fromCol = block * columnsPerBlock;
            int toCol = fromCol + columnsPerBlock - 1;
            List<Integer> blockNumbers = uniqueNumbers.subList(block * groupSize, (block + 1) * groupSize);

            float leftX = cellX(fromCol, originX);
            float rightX = cellX(toCol, originX) + TableLayoutConfig.CELL_WIDTH;
            float width = rightX - leftX;
            Vector2 anchor = new Vector2(leftX + width / 2f, stripY + stripH / 2f);
            zones.add(new BetZone(BetType.DOZEN, tilesFor(tilesByNumber, blockNumbers),
                    rectPolygon(leftX, stripY, width, stripH), anchor));
        }
        return zones;
    }

    private List<BetZone> buildOutsideCategoryZones(List<Tile> standard, List<Integer> uniqueNumbers,
            Map<Integer, List<Tile>> tilesByNumber, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();

        // Colour/parity are per-tile attributes, so bucketing every physical tile
        // (duplicates included) already covers every duplicate correctly — no need to
        // go through tilesByNumber here.
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

        // "High/low" generalised to rank-based halves of the UNIQUE numbers in play
        // (not raw physical tile count, which duplicate pockets would skew) so this
        // still works as the roguelike layer changes what numbers exist.
        int half = uniqueNumbers.size() / 2;
        List<Tile> low = tilesFor(tilesByNumber, uniqueNumbers.subList(0, half));
        List<Tile> high = tilesFor(tilesByNumber, uniqueNumbers.subList(half, uniqueNumbers.size()));

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

    private List<BetZone> buildZeroZones(List<Tile> zeros, int rows, float originX, float originY) {
        List<BetZone> zones = new ArrayList<>();
        if (zeros.isEmpty())
            return zones;

        // Grouped by number for the same reason as the main grid — e.g. two physical
        // "0" tiles get one slot, covering both, instead of two duplicate zero cells.
        Map<Integer, List<Tile>> zerosByNumber = groupByNumber(zeros);
        List<Integer> uniqueZeroNumbers = new ArrayList<>(zerosByNumber.keySet());

        float slotHeight = (rows * TableLayoutConfig.CELL_HEIGHT) / uniqueZeroNumbers.size();
        for (int i = 0; i < uniqueZeroNumbers.size(); i++) {
            List<Tile> coveredTiles = zerosByNumber.get(uniqueZeroNumbers.get(i));
            float x = originX;
            float y = originY + rows * TableLayoutConfig.CELL_HEIGHT - (i + 1) * slotHeight;
            Vector2 anchor = new Vector2(x + TableLayoutConfig.ZERO_COLUMN_WIDTH / 2f, y + slotHeight / 2f);
            zones.add(new BetZone(BetType.STRAIGHT, new ArrayList<>(coveredTiles),
                    rectPolygon(x, y, TableLayoutConfig.ZERO_COLUMN_WIDTH, slotHeight), anchor));
        }
        return zones;
    }
}
