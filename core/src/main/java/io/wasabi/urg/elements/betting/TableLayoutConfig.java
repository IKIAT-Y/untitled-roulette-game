package io.wasabi.urg.elements.betting;

public final class TableLayoutConfig {
    // Grid height is fixed like a real table (3 rows of numbers); columns grow
    // sideways as pockets are added/removed, giving the classic horizontal table
    // (1,2,3 / 4,5,6 / ... reading up each column, columns left-to-right).
    public static final int ROWS = 3;
    public static final float CELL_WIDTH = 32f;
    public static final float CELL_HEIGHT = 32f;
    public static final float ZERO_COLUMN_WIDTH = 32f;
    public static final float CHIP_SNAP_RADIUS = 10f;
    public static final int DOZEN_GROUP_SIZE = 12; // columns per dozen block — scale or keep fixed, see notes
    public static final int COLUMN_GROUP_STRIDE = 3; // every 3rd pocket forms a "column" bet

    // TODO: consider making these configurable, or at least scale with the table
    // size. For now, they are fixed to match the default cell size.

    // Hit-box sizing for the "line" style bets that live on cell borders rather
    // than on a
    // cell itself.
    public static final float SPLIT_HIT_WIDTH = 10f; // width of the strip straddling a border
    public static final float CORNER_HIT_SIZE = 12f; // side length of the square at a 4-way join
    public static final float STREET_STRIP_HEIGHT = 12f; // strip hanging off the bottom edge of a column
    public static final float SIX_LINE_HIT_WIDTH = 8f; // strip straddling a column/column border

    public static final float COLUMN_STRIP_WIDTH = 20f; // "2 to 1" boxes to the right of the grid
    public static final float DOZEN_STRIP_HEIGHT = 24f; // dozen boxes above the grid

    public static final float OUTSIDE_BOX_WIDTH = 48f; // red/black/odd/even/high/low row
    public static final float OUTSIDE_BOX_HEIGHT = 20f;
    public static final float OUTSIDE_BOX_GAP = 4f;
}