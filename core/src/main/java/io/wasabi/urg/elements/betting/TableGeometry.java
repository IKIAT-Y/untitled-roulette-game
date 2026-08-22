package io.wasabi.urg.elements.betting;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Grid-to-world math shared between {@link TableLayoutGenerator} (which builds
 * zones) and
 * anything that needs to re-derive a cell's on-screen rectangle later (the
 * renderer).
 */
public final class TableGeometry {
    private TableGeometry() {
    }

    /** x of the left edge of a grid column, in world space. */
    public static float cellX(int col, float originX) {
        return originX + TableLayoutConfig.ZERO_COLUMN_WIDTH + col * TableLayoutConfig.CELL_WIDTH;
    }

    /**
     * y of the bottom edge of a grid row, in world space. Row 0 is the topmost row.
     */
    public static float cellY(int row, int rows, float originY) {
        return originY + (rows - 1 - row) * TableLayoutConfig.CELL_HEIGHT;
    }

    public static Rectangle cellRect(int col, int row, int rows, float originX, float originY) {
        return new Rectangle(cellX(col, originX), cellY(row, rows, originY),
                TableLayoutConfig.CELL_WIDTH, TableLayoutConfig.CELL_HEIGHT);
    }

    public static Vector2 cellCenter(int col, int row, int rows, float originX, float originY) {
        Rectangle r = cellRect(col, row, rows, originX, originY);
        return new Vector2(r.x + r.width / 2f, r.y + r.height / 2f);
    }
}
