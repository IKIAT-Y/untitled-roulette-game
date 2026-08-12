package io.wasabi.urg.elements.betting;

public final class TableLayoutConfig {
    public static final int COLUMNS = 3; // grid width, like real roulette (1,2,3 / 4,5,6 / ...)
    public static final float CELL_WIDTH = 32f;
    public static final float CELL_HEIGHT = 32f;
    public static final float ZERO_COLUMN_WIDTH = 32f;
    public static final float CHIP_SNAP_RADIUS = 10f;
    public static final int DOZEN_GROUP_SIZE = 12; // rows per dozen block — scale or keep fixed, see notes
    public static final int COLUMN_GROUP_STRIDE = 3; // every 3rd pocket forms a "column" bet
}