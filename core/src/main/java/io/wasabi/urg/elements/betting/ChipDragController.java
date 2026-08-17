package io.wasabi.urg.elements.betting;

import com.badlogic.gdx.InputAdapter;

public class ChipDragController extends InputAdapter {
    private Chip activeChip;
    private BetZone hoveredZone;

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return false;
    }
}