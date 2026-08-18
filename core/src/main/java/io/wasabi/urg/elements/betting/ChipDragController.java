package io.wasabi.urg.elements.betting;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import io.wasabi.urg.elements.game.BettingTable;

/**
 * Input handling only — no rendering, no payout math. Translates screen touches
 * into world
 * points via the camera, then delegates every actual state change to
 * {@link BettingTable} so
 * this class stays a thin adapter.
 */
public class ChipDragController extends InputAdapter {
    private final BettingTable table;
    private final OrthographicCamera camera;

    private Chip activeChip;
    private BetZone hoveredZone;

    private final Vector3 unprojected = new Vector3();

    public ChipDragController(BettingTable table, OrthographicCamera camera) {
        this.table = table;
        this.camera = camera;
    }

    private Vector2 screenToWorld(int x, int y) {
        unprojected.set(x, y, 0);
        camera.unproject(unprojected);
        return new Vector2(unprojected.x, unprojected.y);
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        Vector2 point = screenToWorld(x, y);

        Chip trayChip = table.getTrayChipAt(point);
        if (trayChip != null) {
            activeChip = table.beginDragFromTray(trayChip.getDenomination(), point);
            return true;
        }

        Chip placedChip = table.getPlacedChipAt(point);
        if (placedChip != null) {
            activeChip = table.beginDragFromPlaced(placedChip);
            return activeChip != null;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (activeChip == null)
            return false;

        Vector2 point = screenToWorld(x, y);
        activeChip.setPosition(point.x, point.y);
        hoveredZone = table.findNearestZone(point, TableLayoutConfig.CHIP_SNAP_RADIUS);
        table.setHoveredZone(hoveredZone);
        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (activeChip == null)
            return false;

        if (hoveredZone != null) {
            table.placeBet(hoveredZone, activeChip);
        } else {
            table.discardChip(activeChip);
        }

        activeChip = null;
        hoveredZone = null;
        table.setHoveredZone(null);
        return true;
    }

    public BetZone getHoveredZone() {
        return hoveredZone;
    }

    public Chip getActiveChip() {
        return activeChip;
    }
}
